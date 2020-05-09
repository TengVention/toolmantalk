package com.toolman.toolmantalk.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.toolman.toolmantalk.entity.Message;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.MessageService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import com.toolman.toolmantalk.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@RestController
@RequestMapping("/letter")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //私信列表
    @GetMapping("/list")
    public Object getLetterList(@RequestParam(value = "start", defaultValue = "1") int start,
                                @RequestParam(value = "size", defaultValue = "7") int size) {
        User user = hostHolder.getUser();
        //pageHelper设置分页
        PageHelper.startPage(start, size);

        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId());
        Map<String, Object> map = new HashMap<>();
        //设置每个会话的消息数量
        if (conversationList != null) {
            for (Message message : conversationList) {
                message.setLetterCount(messageService.findLetterCount(message.getConversationId()));
                message.setUnreadCount(messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                message.setTargetUser(userService.findUserInfoById(targetId));
            }
        }
        //查询未读消息总数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);

        //获取分页后的数据
        PageInfo<Message> page = new PageInfo<>(conversationList, 5);
        map.put("letterUnreadCount",letterUnreadCount);
        map.put("page", page);
        return map;
    }

    @GetMapping("/detail/{conversationId}")
    public Object getLetterDetail(@PathVariable("conversationId") String conversationId,
                                  @RequestParam(value = "start", defaultValue = "1") int start,
                                  @RequestParam(value = "size", defaultValue = "7") int size){
        //pageHelper设置分页
        PageHelper.startPage(start, size);

        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId);
        Map<String, Object> map = new HashMap<>();
        if (letterList != null) {
            for (Message message : letterList) {
                message.setFromUser(userService.findUserInfoById(message.getFromId()));
            }
        }

        //私信的目标用户
        map.put("targetUser",getLetterTarget(conversationId));

        //设置消息为已读
        List<Integer> ids = getLetterIds(letterList);
        if (!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        PageInfo<Message> page = new PageInfo<>(letterList, 5);
        map.put("page", page);

        return map;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserInfoById(id1);
        }else {
            return userService.findUserInfoById(id0);
        }
    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    @PostMapping("/send")
    public Object sendLetter(String toName, String content) {
        User targetUser = userService.findUserInfoByName(toName);
        if (targetUser == null){
            return Result.fail("目标用户不存在!");
        }
        if (content.isEmpty()){
            return Result.fail("私信内容不能为空!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(targetUser.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(HtmlUtils.htmlEscape(content));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return Result.success("私信已发送");
    }

}
