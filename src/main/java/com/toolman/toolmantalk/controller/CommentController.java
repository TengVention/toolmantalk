package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.Comment;
import com.toolman.toolmantalk.service.CommentService;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/add/{discussPostId}")
    public Object addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        return Result.success("添加成功");
    }

}
