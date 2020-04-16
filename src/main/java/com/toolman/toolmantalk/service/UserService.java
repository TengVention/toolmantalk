package com.toolman.toolmantalk.service;

import com.toolman.toolmantalk.dao.UserMapper;
import com.toolman.toolmantalk.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
