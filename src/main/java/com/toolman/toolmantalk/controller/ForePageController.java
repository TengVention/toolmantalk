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



}
