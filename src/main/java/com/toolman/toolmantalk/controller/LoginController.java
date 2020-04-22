package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController implements CommunityConstant {

    @Autowired
    UserService userService;
    @Autowired
    AliyunSmsUtils aliyunSmsUtils;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    HostHolder hostHolder;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    static final String verify_code = "user:phone_code";

    /**
     * 登录
     * @param username
     * @param password
     * @param rememberme
     * @return
     */
    @PostMapping("/login")
    @ExcludeInterceptor
    public Object login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam boolean rememberme,
                        HttpServletResponse response){

        //检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password);
        if (map.containsKey("success")){
            User user = userService.findUserByName(username);
            //登录成功
            String token = jwtUtils.createJwt("1",username, new HashMap<>());
            Cookie cookie = new Cookie("token",token);
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return Result.success();
        }
        return map;
    }

    @GetMapping("/logout")
    public Object logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return Result.success("注销成功");
    }



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

    @GetMapping("/info")
    @ResponseBody
    public Result getUserInfo(){
        User user = hostHolder.getUser();
        return Result.success(user);
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

    /*验证码激活*/
    @PostMapping("/sms/checkCode")
    public Object checkCode(@RequestParam String code,
                            @RequestParam String phone){
        boolean result = userService.checkCode(phone,code);
        if (result){
            User user = userService.findUserByPhone(phone);
            if (user==null){
                return Result.fail("该手机号未注册!");
            }
            if (user.getStatus()==1){
                return Result.fail("该账号已激活，请勿重复激活!");
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
