package com.toolman.toolmantalk.util;

import com.toolman.toolmantalk.entity.User;
import org.springframework.stereotype.Component;

/**
 *  获取当前用户信息，用于代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
