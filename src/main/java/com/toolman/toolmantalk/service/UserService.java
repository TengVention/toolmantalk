package com.toolman.toolmantalk.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.toolman.toolmantalk.dao.LoginTicketMapper;
import com.toolman.toolmantalk.dao.UserMapper;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.util.AliyunSmsUtils;
import com.toolman.toolmantalk.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AliyunSmsUtils aliyunSmsUtils;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private LoginTicketMapper loginTicketMapper;


    static final String verify_code = "user:phone_code";

    /*通过用户名查找用于显示用户的信息*/
    public User findUserInfoByName(String username){
        return userMapper.selectUserInfoByName(username);
    }

    /*通过id查找用于显示用户的信息*/
    public User findUserInfoById(int id){
        return userMapper.selectUserInfoById(id);
    }

    /*通过id查找user*/
    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    /*通过phone查找user*/
    public User findUserByPhone(String phone){
        return userMapper.selectByPhone(phone);
    }

    /*修改用户状态*/
    public int updateUserStatus(int userId, int status){
        return userMapper.updateStatus(userId, status);
    }

    /*更新手机号*/
    public void updateUserPhone(int userId, String phone){
        userMapper.updatePhone(userId, phone);
    }

    /*发送验证码*/
    public Boolean sendVerifyCode(String phone,String code) {

        try {
            SendSmsResponse result = aliyunSmsUtils.sendSms(phone,code);
            if (result.getCode().equals("OK")){
                redisTemplate.opsForValue().set(verify_code+phone,code,50000, TimeUnit.MINUTES);
                return true;
            }else {
                return false;
            }
        } catch (ClientException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*生成随机验证码*/
    public String generateCode(int len){
        len = Math.min(len, 8);
        int min = Double.valueOf(Math.pow(10, len - 1)).intValue();
        int num = new Random().nextInt(
                Double.valueOf(Math.pow(10, len + 1)).intValue() - 1) + min;
        return String.valueOf(num).substring(0,len);
    }

    /*验证码校验*/
    public boolean checkCode(String phone, String verifyCode){
        boolean result = redisTemplate.opsForValue().getOperations().hasKey(verify_code+phone);
        if (result){
            String code = redisTemplate.opsForValue().get(verify_code+phone);
            if (code.equals(verifyCode))
                return true;
        }
        return false;
    }

    /*通过用户名查找用户*/
    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    public Map<String, Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("message", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("message", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("message", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("message", "该账号已存在!");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("message", "该邮箱已被注册!");
            return map;
        }

//        // 验证手机号
//        u = userMapper.selectByPhone(user.getPhone());
//        if (u != null) {
//            map.put("emailMsg", "该手机号已被注册!");
//            return map;
//        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //手机验证码激活

        return map;

    }

    /*登录*/
    public Map<String, Object> login(String username, String password){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("errormessage", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("errormessage", "密码不能为空!");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("errormessage","该账号不存在!");
            return map;
        }

        //验证账号状态
        if (user.getStatus()==0){
            map.put("errormessage", "该账号未激活!");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("errormessage", "密码不正确!");
            return map;
        }
        //登录成功
        map.put("success",0);

        //生成登录凭证
//        LoginTicket loginTicket = new LoginTicket();
//        loginTicket.setUserId(user.getId());
//        loginTicket.setTicket(CommunityUtil.generateUUID());
//        loginTicket.setStatus(0);
//        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

//        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /*注销*/
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

//    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
//    }

    /**
     * 更新头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public Map<String, Object> changPassword(int userId, String oldPassword, String newPassword){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("errorMsg","原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("errorMsg","原密码不能为空!");
            return map;
        }

        User user = userMapper.selectById(userId);

        //验证密码
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("errorMsg","原密码不正确!");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }

}
