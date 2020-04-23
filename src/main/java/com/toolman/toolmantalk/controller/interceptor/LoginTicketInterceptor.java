package com.toolman.toolmantalk.controller.interceptor;

import com.toolman.toolmantalk.service.UserService;
import com.toolman.toolmantalk.util.CookieUtil;
import com.toolman.toolmantalk.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${server.servlet.context-path}")
    private String contextPath;



    //请求处理之前进行调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");

//        //判断凭证非空
//        if (ticket != null){
//            //查询凭证
//            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            // 检查凭证是否有效
//            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
//                //根据凭证拿到的用户id查询用户
//                User user = userService.findUserById(loginTicket.getUserId());
//                //在本次请求中持有用户
//                hostHolder.setUser(user);
//                return true;
//            }
//        }
        return false;
    }

    //进行视图返回渲染之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    //渲染了对应的视图之后执行，这个方法的主要作用是用于进行资源清理的工作。
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        User user = hostHolder.getUser();
//        response.setCharacterEncoding("utf-8");
//        response.setContentType("application/json; charset=utf-8");
//        ServletOutputStream outputStream = response.getOutputStream();
//
//        JSONObject o = new JSONObject();
//        o.put("user",user);
//        outputStream.write(o.toString().getBytes());
//        outputStream.flush();

        hostHolder.clear();

    }
}
