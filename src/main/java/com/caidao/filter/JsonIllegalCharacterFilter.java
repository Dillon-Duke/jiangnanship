package com.caidao.filter;

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
            requestWrapper = new RequestWrapper(request);
        }
        if(requestWrapper == null) {
            chain.doFilter(req, resp);
        } else {
            chain.doFilter(requestWrapper, resp);
        }
    }

    /** 过滤器销毁 */
    @Override
    public void destroy() {
    }
}
