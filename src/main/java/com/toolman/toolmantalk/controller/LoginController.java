package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    UserService userService;

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
