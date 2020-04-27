package com.toolman.toolmantalk.dao;

import com.toolman.toolmantalk.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //按照实体类型查找评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId);

    //按照实体类型查找评论总数
    int selectCountByEntity(int entityType, int entityId);

    //增加评论
    int insertComment(Comment comment);

}
