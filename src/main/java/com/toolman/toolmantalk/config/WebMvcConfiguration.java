package com.toolman.toolmantalk.config;

import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.CookieUtil;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static Collection<HandlerMethodArgumentResolver> methodArgumentResolverList;

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        if (methodArgumentResolverList!=null){
            argumentResolvers.addAll(methodArgumentResolverList);
        }
    }

    @Override
    protected void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")// 允许跨域访问的路径
                //.allowedOrigins("*")// 允许跨域访问的源
                .allowedMethods("GET","POST","PUT", "OPTIONS", "DELETE", "PATCH")// 允许请求方法
                .allowCredentials(true)// 是否发送cookie
                //.allowedHeaders("*")// 允许头部设置
                .maxAge(3600);// 预检间隔时间
    }

    /**
     * FastJson配置
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(fastJsonHttpMessageConverters());
    }

    @Bean
    public HttpMessageConverter fastJsonHttpMessageConverters() {
        //1. 需要定义一个converter转换消息的对象
        FastJsonHttpMessageConverter fasHttpMessageConverter = new FastJsonHttpMessageConverter();
        //2. 添加fastjson的配置信息，比如:是否需要格式化返回的json的数据
        FastJsonConfig fastJsonConfig = new FastJsonConfig();//3. 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        fasHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        //4. 在converter中添加配置信息
        fasHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        return fasHttpMessageConverter;
    }

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptor = registry.addInterceptor(getSecurityInterceptor());
        ArrayList<String> list = new ArrayList<>();
        list.add("/error");
        list.add("/static/**");
        interceptor.excludePathPatterns(list);
        super.addInterceptors(registry);
    }

    protected class SecurityInterceptor extends HandlerInterceptorAdapter{
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            System.out.println(request.getMethod());
            if ("OPTIONS".equals(request.getMethod())){
                if (response!=null){
                    response.setStatus(200);
                    response.sendError(200,"通过");
                }
                return false;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ExcludeInterceptor excludeInterceptor = handlerMethod.getMethodAnnotation(ExcludeInterceptor.class);
            if (excludeInterceptor == null || !excludeInterceptor.value()){
                //验证cookie中的token
                if (CookieUtil.getValue(request, "token")==null){
                    response.setStatus(401);
                    response.sendError(401, "登录失效,请重新登录");
                    return false;
                }else {
                    String token = CookieUtil.getValue(request, "token");
                    Claims claims = jwtUtils.parseJwt(token);
                    if (claims == null){
                        response.setStatus(401);
                        response.sendError(401, "登录失效,请重新登录");
                        return false;
                    }

                    String username = claims.getSubject();
                    User user = userService.findUserByName(username);
                    hostHolder.setUser(user);
                }

//                if (request.getHeader(CommunityConstant.AUTHORIZATION)==null){
//                    response.setStatus(401);
//                    response.sendError(401, "登录失效,请重新登录");
//                    return false;
//                }else {
//                    String authorization = request.getHeader(CommunityConstant.AUTHORIZATION);
//                    String token = authorization.replace("toolman:","");
//                    Claims claims = jwtUtils.parseJwt(token);
//                    if (claims == null){
//                        response.setStatus(401);
//                        response.sendError(401, "登录失效,请重新登录");
//                        return false;
//                    }
//
//                    String username = claims.getSubject();
//                    User user = userService.findUserByName(username);
//                    hostHolder.setUser(user);
//                }
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            hostHolder.clear();
        }
    }
}
