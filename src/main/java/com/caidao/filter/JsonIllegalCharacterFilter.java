package com.caidao.filter;

import com.caidao.filter.wrapper.JsonRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author tom
 */
public class JsonIllegalCharacterFilter implements Filter {

    /** 过滤器初始化 */
    @Override
    public void init(FilterConfig filterConfig) {
    }

    /** 进行链式过滤 */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if(req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            String method = request.getMethod();
            if ("GET".equals(method)){

            } if ("POST".equals(method)) {
                requestWrapper = new JsonRequestWrapper(request);
                if(requestWrapper == null) {
                    chain.doFilter(req, resp);
                } else {
                    chain.doFilter(requestWrapper, resp);
                }
            } if ("UPDATE".equals(method)) {

            } if ("DELETE".equals(method)) {

            }
        }
    }

    /** 过滤器销毁 */
    @Override
    public void destroy() {
    }
}
