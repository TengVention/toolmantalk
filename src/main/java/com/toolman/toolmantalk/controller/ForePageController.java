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

    /*页面跳转 部分*/
    @GetMapping("/discuss/listPost")
    public ModelAndView listPost(){
        ModelAndView mv = new ModelAndView("fore/listPost");
        return mv;
    }

    @GetMapping("/discuss/postDetail")
    public ModelAndView postDetail(){
        ModelAndView mv = new ModelAndView("fore/postDetail");
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

    /*用户个人页面*/
    @ExcludeInterceptor
    @GetMapping("/user/profile")
    public ModelAndView profilePage(){
        ModelAndView mv = new ModelAndView("fore/profile");
        return mv;
    }

    @GetMapping("/user/followee")
    public ModelAndView followeePage(){
        ModelAndView mv = new ModelAndView("fore/followee");
        return mv;
    }

    @GetMapping("/user/follower")
    public ModelAndView followerPage(){
        ModelAndView mv = new ModelAndView("fore/follower");
        return mv;
    }

    @GetMapping("letter/notice")
    public ModelAndView noticePage(){
        ModelAndView mv = new ModelAndView("fore/notice");
        return mv;
    }

    @GetMapping("letter/noticeDetail")
    public ModelAndView noticeDetailPage(){
        ModelAndView mv = new ModelAndView("fore/notice-detail");
        return mv;
    }

    @GetMapping("/searchPage")
    public ModelAndView searchPage(){
        ModelAndView mv = new ModelAndView("fore/search");
        return mv;
    }

    @ExcludeInterceptor
    @GetMapping("/denied")
    public ModelAndView getDeniedPage(){
        ModelAndView mv = new ModelAndView("error/404");
        return mv;
    }

}
