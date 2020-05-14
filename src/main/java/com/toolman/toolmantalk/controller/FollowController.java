package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.Event;
import com.toolman.toolmantalk.entity.Page;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.event.EventProducer;
import com.toolman.toolmantalk.service.FollowService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.CommunityConstant;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/follow")
    public Object follow(@RequestBody Map<String, Object> map ) {
        Integer entityType = (Integer) map.get("entityType");
        Integer entityId = (Integer) map.get("entityId");
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);//因为现在只能关注人所以就和entityId一样
        eventProducer.fireEvent(event);

        return Result.success("已关注!");
    }

    @PostMapping("/unfollow")
    public Object unfollow(@RequestBody Map<String, Object> map) {
        Integer entityType = (Integer) map.get("entityType");
        Integer entityId = (Integer) map.get("entityId");
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return Result.success("已取消关注!");
    }

    /*显示某个用户关注的用户*/
    @GetMapping("/followees/{userId}")
    public Object getFollowees(@PathVariable("userId") int userId,
                               @RequestParam(value = "start", defaultValue = "1") int start,
                               @RequestParam(value = "size", defaultValue = "7") int size) {
        User user = userService.findUserInfoById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        Page page = new Page();
        page.setLimit(size);
        page.setCurrent(start);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("user",user);
        map.put("users",userList);
        map.put("page",page);
        return map;
    }

    /*显示某个用户的粉丝*/
    @GetMapping("/followers/{userId}")
    public Object getFollowers(@PathVariable("userId") int userId,
                               @RequestParam(value = "start", defaultValue = "1") int start,
                               @RequestParam(value = "size", defaultValue = "7") int size) {
        User user = userService.findUserInfoById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        Page page = new Page();
        page.setLimit(size);
        page.setCurrent(start);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("user",user);
        map.put("users",userList);
        map.put("page",page);
        return map;
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
