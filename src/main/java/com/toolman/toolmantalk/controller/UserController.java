package com.toolman.toolmantalk.controller;

import com.toolman.toolmantalk.entity.User;
import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.CommunityUtil;
import com.toolman.toolmantalk.util.HostHolder;
import com.toolman.toolmantalk.util.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/user")
public class UserController {

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

    @PostMapping("/upload")
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

    /*获得头像*/
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

    /*用户设置页面*/
    @GetMapping("/setting")
    public ModelAndView setting(){
        ModelAndView mv = new ModelAndView("usersetting");
        return mv;
    }

}
