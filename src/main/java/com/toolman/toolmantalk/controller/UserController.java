package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.annotation.ExcludeInterceptor;
import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.FollowService;
import com.toolman.toolmantalk.service.LikeService;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.*;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${toolmantalk.path.upload}")
    private String uploadPath;

    @Value("${toolmantalk.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private FollowService followService;

    /**
     * 更新头像
     * @param headerImage
     * @return
     */
    @PostMapping("/uploadHeader")
    public Object uploadHeader(MultipartFile headerImage){
        if (headerImage == null){
            return Result.fail("您还没有选择图片!");
        }
        /*文件名*/
        String fileName = headerImage.getOriginalFilename();
        /*后缀名*/
       String suffix = fileName.substring(fileName.lastIndexOf("."));
       if (StringUtils.isBlank(suffix)){
           return Result.fail("上传文件的格式不正确!");
       }

       //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
       //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常!", e);
        }

        //更新当前用户的头像的路径(web访问路径)
        //http://localhost:8080/toolmantalk/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return Result.success("更新头像成功!");
    }

    /**
     * 获取头像
     * @param fileName
     * @param response
     */
    @ExcludeInterceptor
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream()
                ) {
            //缓存区，一次读取1024
            byte[] buffer = new byte[1024];
            //游标
            int b = 0;
            /*如果不是没有读取到字节，就执行while循环*/
            while ((b = fis.read(buffer)) != -1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @PostMapping("/changePassword")
    public Object changePassword(@RequestBody Map<String, Object> loginMap){
        //旧密码
        String oldPassword = (String) loginMap.get("oldPassword");
        //新密码
        String newPassword = (String) loginMap.get("newPassword");
        if (newPassword.equals(oldPassword)){
            return Result.fail("新密码不能和原密码一样!");
        }
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.changPassword(user.getId(), oldPassword, newPassword);

        //判断修改密码是否返回错误信息
        if (map == null || map.isEmpty()) {
            return Result.success("修改密码成功");
        }
        return Result.fail(map);
    }

    @ExcludeInterceptor
    @GetMapping("/profile/{userId}")
    public Object getProfilePage(@PathVariable("userId") int userId,
                                 HttpServletRequest request) {
        User user = userService.findUserInfoById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        //如果当前登录了
        if (CookieUtil.getValue(request, "token")!=null){
            String token = CookieUtil.getValue(request, "token");
            Claims claims = jwtUtils.parseJwt(token);
            if (claims == null){
                return Result.fail("请求无效!");
            }
            String username = claims.getSubject();
            User loginUser = userService.findUserInfoByName(username);
            hostHolder.setUser(loginUser);
        }
        Map<String, Object> map = new HashMap<>();

        map.put("user",user);

        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        map.put("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        map.put("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        map.put("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        map.put("hasFollowed",hasFollowed);

        return map;
    }





//    前端页面跳转
    /*用户设置页面*/
    @GetMapping("/setting")
    public ModelAndView setting(){
        ModelAndView mv = new ModelAndView("usersetting");
        return mv;
    }

}
