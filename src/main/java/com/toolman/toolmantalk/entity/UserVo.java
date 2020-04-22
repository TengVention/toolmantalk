package com.toolman.toolmantalk.entity;

import java.util.Date;

public class UserVo {
    private int id;
    //用户名
    private String username;
    //邮箱
    private String email;
    //类型
    private int type;
    //状态
    private int status;
    //头像
    private String headerUrl;
    //创建时间
    private Date createTime;
    //手机号
    private String phone;
    public UserVo(int id, String username) {
        this.id=id;
        this.username=username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHeaderUrl() {
        return headerUrl;
    }

    public void setHeaderUrl(String headerUrl) {
        this.headerUrl = headerUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", headerUrl='" + headerUrl + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
