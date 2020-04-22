package com.toolman.toolmantalk.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@ConfigurationProperties("jwt.config")
@Component
public class JwtUtils {

    /**
     * 签名私钥
     */
    private String key;
    /**
     * 签名的失效时间
     */
    private Long ttl;

    /**
     * 设置认证token
     * id:登录用户id
     * subject：登录用户名
     */
    public String createJwt(String id, String name, Map<String, Object> map) {
        //1.设置失效时间
        long now = System.currentTimeMillis();
        long exp = now + ttl;
        //2.创建jwtBuilder
        JwtBuilder jwtBuilder = Jwts.builder().setId(id).setSubject(name)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, key);
        //3.根据map设置claims
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }
        jwtBuilder.setExpiration(new Date(exp));
        //4.创建token
        return jwtBuilder.compact();
    }


    /**
     * 解析token字符串获取clamis
     */
    public Claims parseJwt(String token) {
        try {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}
