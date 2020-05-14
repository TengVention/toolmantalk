package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.DiscussPost;
import com.toolman.toolmantalk.entity.Page;
import com.toolman.toolmantalk.service.ElasticsearchService;
import com.toolman.toolmantalk.service.LikeService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.CommunityConstant;
import com.toolman.toolmantalk.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //  search?keyword=xxx
    @GetMapping("/search")
    public Object search(String keyword,
                         @RequestParam(value = "start", defaultValue = "1") int start,
                         @RequestParam(value = "size", defaultValue = "7") int size) {
        if (keyword==null){
            return Result.fail("搜索关键字不能为空!");
        }
        Page page = new Page();
        page.setCurrent(start);
        page.setLimit(size);
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserInfoById(post.getUserId()));
                //点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("discussPosts", discussPosts);
        data.put("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        data.put("page", page);

        return data;
    }

}
