package com.toolman.toolmantalk.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.toolman.toolmantalk.entity.DiscussPost;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.DiscussPostService;
import com.toolman.toolmantalk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
public class DiscussPostController {

    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    UserService userService;

    @GetMapping("/discussposts")
    @CrossOrigin
    public PageInfo<DiscussPost> list(@RequestParam(value = "start", defaultValue = "1") int start,
                                      @RequestParam(value = "size", defaultValue = "7") int size){
        PageHelper.startPage(start, size);
        List<DiscussPost> posts = discussPostService.listDiscussPosts();

        if (posts!=null){
            for (DiscussPost post : posts){
                User user = userService.findUserById(post.getUserId());
                post.setUser(user);
            }
        }

        PageInfo<DiscussPost> page = new PageInfo<>(posts,5);//5表示导航分页最多有5个，像 [1,2,3,4,5] 这样
        return page;
    }





/*测试用可删除*/
    /*页面跳转 部分*/
    @RequestMapping(value="/listPost", method= RequestMethod.GET)
    public ModelAndView listPost(){
        ModelAndView mv = new ModelAndView("listPost");
        return mv;
    }

}
