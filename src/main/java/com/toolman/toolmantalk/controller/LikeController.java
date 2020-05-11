package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.LikeService;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @PostMapping("/like")
    public Object like(@RequestBody Map<String, Object> likeMap) {

        String type = (String) likeMap.get("entityType");
        String id = (String) likeMap.get("entityId");
        String userId = (String) likeMap.get("entityUserId");
        int entityType = Integer.parseInt(type);
        int entityId = Integer.parseInt(id);
        int entityUserId = Integer.parseInt(userId);
        User user = hostHolder.getUser();

        //  点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //  点赞数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态 1-已赞 0-未赞
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object>  map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return Result.success(map);
    }

}
