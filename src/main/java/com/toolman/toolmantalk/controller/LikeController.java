package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.Event;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.event.EventProducer;
import com.toolman.toolmantalk.service.LikeService;
import com.toolman.toolmantalk.util.CommunityConstant;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/like")
    public Object like(@RequestBody Map<String, Object> likeMap) {

        String type = (String) likeMap.get("entityType");
        String id = (String) likeMap.get("entityId");
        String userId = (String) likeMap.get("entityUserId");
        String postid = (String) likeMap.get("postId");
        int entityType = Integer.parseInt(type);
        int entityId = Integer.parseInt(id);
        int entityUserId = Integer.parseInt(userId);
        int postId = Integer.parseInt(postid);
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

        //触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return Result.success(map);
    }

}
