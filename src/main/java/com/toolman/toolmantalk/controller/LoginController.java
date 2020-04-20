package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.AliyunSmsUtils;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    UserService userService;
    @Autowired
    AliyunSmsUtils aliyunSmsUtils;
    @Autowired
    StringRedisTemplate redisTemplate;

    static final String verify_code = "user:phone_code";

    /**
     * 注册
     * @return
     */
    @PostMapping("/register")
    @CrossOrigin
    public Object register(@RequestBody User user){
        Map<String,Object> map = userService.register(user);
        User registerUser = userService.findUserByName(user.getUsername());
        System.out.println(registerUser.toString());
        if (map == null || map.isEmpty()){
            map.put("userId",registerUser.getId());
            return Result.success(map);
        }
        return map;
    }

    /**
     * 发送短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sms")
    public Object sendCode(@RequestBody User user){
        String code = userService.generateCode(6);
        /*判断手机号是否被使用*/
        if (userService.findUserByPhone(user.getPhone())==null){
            boolean result = userService.sendVerifyCode(user.getPhone(), code);
            if (result){
                User verifyUser = userService.findUserByName(user.getUsername());
                userService.updateUserPhone(verifyUser.getId(), user.getPhone());
                return Result.success("短信发送成功");
            }
            return Result.fail("短信发送失败");
        }
        return Result.fail("手机号已被注册!");
    }

    @PostMapping("/sms/checkCode")
    public Object checkCode(@RequestParam String code,
                            @RequestParam String phone){
        boolean result = userService.checkCode(phone,code);
        if (result){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserByPhone(phone);
            if (user==null){
                return Result.fail("该手机号未注册!");
            }
            userService.updateUserStatus(user.getId(), 1);
            return Result.success("激活成功");
        }
        return Result.fail("验证码不正确!");
    }


    //前端页面跳转控制
    //注册页面
    @GetMapping("/register")
    public ModelAndView register(){
        ModelAndView mv = new ModelAndView("register");
        return mv;
    }
    //激活账号页面
    @GetMapping("/activation/{id}")
    public ModelAndView activation(){
        ModelAndView mv = new ModelAndView("activation");
        return mv;
    }

}
