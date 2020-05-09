package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class ForePageController {

    @ExcludeInterceptor
    @GetMapping("/index")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("index");
        return mv;
    }

    @ExcludeInterceptor
    @GetMapping("/login")
    public ModelAndView login() {
        ModelAndView mv = new ModelAndView("fore/login");
        return mv;
    }

    @ExcludeInterceptor
    @GetMapping("/register")
    public ModelAndView register() {
        ModelAndView mv = new ModelAndView("fore/register");
        return mv;
    }

    /*消息列表页面*/
    @GetMapping("/letter/messageList")
    public ModelAndView letterList(){
        ModelAndView mv = new ModelAndView("fore/letter");
        return mv;
    }

    /*消息详情页面*/
    @GetMapping("/letter/detail")
    public ModelAndView letterDetail(){
        ModelAndView mv = new ModelAndView("fore/letter-detail");
        return mv;
    }

    //    前端页面跳转
    /*用户设置页面*/
    @GetMapping("/userSetting")
    public ModelAndView setting(){
        ModelAndView mv = new ModelAndView("fore/setting");
        return mv;
    }

}
