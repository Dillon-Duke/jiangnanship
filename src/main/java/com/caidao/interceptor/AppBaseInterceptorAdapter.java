package com.caidao.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用来记录手机App的
 * @author tom
 * @since 2020-06-11
 */
public class AppBaseInterceptorAdapter implements HandlerInterceptor {

    /** 在业务处理器处理请求之前被调用 */
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String method = req.getMethod();
        return true;
    }

    /** 在业务处理器处理请求完成之后，生成视图之前执行 */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    /** 在DispatcherServlet完全处理完请求之后被调用，可用于清理资源 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
