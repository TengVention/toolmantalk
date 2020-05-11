package com.toolman.toolmantalk.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import com.toolman.toolmantalk.entity.Comment;
import com.toolman.toolmantalk.entity.DiscussPost;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.CommentService;
import com.toolman.toolmantalk.service.DiscussPostService;
import com.toolman.toolmantalk.service.LikeService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.*;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private JwtUtils jwtUtils;

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
                User user = userService.findUserInfoById(post.getUserId());
                post.setUser(user);
                //加入帖子的点赞数量
                post.setLikeCount(likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
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
     * @param discussPostId
     * @param start
     * @param size
     * @return
     */
    @ExcludeInterceptor
    @GetMapping("/discussposts/{discussPostId}")
    public Object getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 @RequestParam(value = "start", defaultValue = "1") int start,
                                 @RequestParam(value = "size", defaultValue = "7") int size,
                                 HttpServletRequest request){
        //获取帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        //获取帖子作者显示信息
        User user = userService.findUserInfoById(post.getUserId());
        //帖子的赞的数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPostId);
        post.setLikeCount(likeCount);

        //如果当前登录了
        if (CookieUtil.getValue(request, "token")!=null){
            String token = CookieUtil.getValue(request, "token");
            Claims claims = jwtUtils.parseJwt(token);
            if (claims == null){
                return Result.fail("请求无效!");
            }
            String username = claims.getSubject();
            User loginUser = userService.findUserInfoByName(username);
            hostHolder.setUser(loginUser);
        }

        //当前用户的点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);

        //pageHelper设置分页
        PageHelper.startPage(start, size);
        //评论
        //拿到当前帖子的所有评论
        List<Comment> comments = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId());
        //如果评论不为空，遍历出来，并且嵌套每个评论的用户显示信息
        if (comments!=null){
            for (Comment comment : comments){
                //用于显示评论的用户信息
                User commentUser = userService.findUserInfoById(comment.getUserId());
                //回复
                //查找评论的所有回复
                List<Comment> replyComments = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                //嵌套进数据中
                comment.setReplyComments(replyComments);
                //评论点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                comment.setLikeCount(likeCount);
                //当前用户点赞状态
                int commentLikeStatuse = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                comment.setLikeStatus(commentLikeStatuse);
                //如果回复list不为空，遍历出来，并且嵌套每个回复的用户显示信息
                if (replyComments!=null){
                    for (Comment replyComment : replyComments){
                        //用于显示回复的用户信息
                        User replyCommentUser = userService.findUserInfoById(replyComment.getUserId());
                        replyComment.setUser(replyCommentUser);
                        //设置回复对象
                        replyComment.setTargetUser(userService.findUserInfoById(replyComment.getTargetId()));
                        //回复点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, replyComment.getId());
                        replyComment.setLikeCount(likeCount);
                        //当前用户点赞状态
                        int replyLikeStatuse = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, replyComment.getId());
                        replyComment.setLikeStatus(replyLikeStatuse);
                    }
                }
                comment.setUser(commentUser);

                //回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                comment.setReplyCount(replyCount);

            }
        }


        PageInfo<Comment> page = new PageInfo<>(comments,5);//5表示导航分页最多有5个，像 [1,2,3,4,5] 这样

        Map<String, Object> map = new HashMap<>();

        map.put("user",user);
        map.put("post",post);
        map.put("page",page);
        map.put("likeStatus",likeStatus);

        return map;
    }



}
