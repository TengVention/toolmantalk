package com.toolman.toolmantalk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ToolManTalkApplication {

    @PostConstruct
    public void init() {
        //解决netty启动冲突的问题(Redis和elasticsearch底层都是netty，redis配置的时候以加配置好了netty)
        //问题出现的原因：Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        SpringApplication.run(ToolManTalkApplication.class, args);
    }
}
