package com.toolman.toolmantalk.controller.interceptor;

import com.toolman.toolmantalk.service.MessageService;
import com.toolman.toolmantalk.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;



}
