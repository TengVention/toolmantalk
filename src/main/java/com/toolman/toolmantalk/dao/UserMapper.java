package com.toolman.toolmantalk.dao;

import com.toolman.toolmantalk.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    //通过id查找用户
    User selectById(int id);
    //通过用户名查找用户
    User selectByName(String username);
    //通过邮箱查找用户
    User selectByEmail(String email);
    //新增用户
    int insertUser(User user);
    //更新用户状态
    int updateStatus(int id, int status);
    //更新用户头像
    int updateHeader(int id, String headerUrl);
    //更新密码
    int updatePassword(int id, String password);

}
