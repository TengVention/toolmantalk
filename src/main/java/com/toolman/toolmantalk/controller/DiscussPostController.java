package com.toolman.toolmantalk.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import com.toolman.toolmantalk.entity.DiscussPost;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.DiscussPostService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 帖子分页数据（用于首页显示）
     * @param start
     * @param size
     * @return
     */
    @GetMapping("/discussposts")
    @ExcludeInterceptor
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

    /**
     * 发布帖子
     * @param post
     * @return
     */
    @PostMapping("/addPost")
    public Object addDiscussPost(@RequestBody DiscussPost post){
        User user = hostHolder.getUser();
        if (user == null){
            return Result.fail("您还没有登录!");
        }
        post.setUserId(user.getId());
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        //报错的情况，将统一来处理
        return Result.success("发布成功!");

    }

    /**
     * 获取帖子详情
     * @param id
     * @param start
     * @param size
     * @return
     */
    @ExcludeInterceptor
    @GetMapping("/discussposts/{id}")
    public Object getDiscussPost(@PathVariable("id") int id,
                                 @RequestParam(value = "start", defaultValue = "1") int start,
                                 @RequestParam(value = "size", defaultValue = "7") int size){
        //获取帖子
        DiscussPost post = discussPostService.findDiscussPostById(id);
        //获取帖子作者
        User user = userService.findUserById(post.getUserId());

        Map<String,Object> map = new HashMap<>();
        map.put("user",user);
        map.put("post",post);
        return Result.success(map);
    }





/*测试用可删除*/
    /*页面跳转 部分*/
    @RequestMapping(value="/listPost", method= RequestMethod.GET)
    public ModelAndView listPost(){
        ModelAndView mv = new ModelAndView("listPost");
        return mv;
    }

    @RequestMapping(value="/postDetail", method= RequestMethod.GET)
    public ModelAndView postDetail(){
        ModelAndView mv = new ModelAndView("postDetail");
        return mv;
    }

}
