package com.caidao.filter;

import com.caidao.filter.wrapper.DecryptDataRequestWrapper;
import com.caidao.util.PropertyUtils;
import com.caidao.util.RsaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 参数处理验过滤器（针对ajax、表单等请求）
 * 1.获取请求参数
 * 2.对获取到的请求参数进行处理（解密、字符串替、请求参数分类截取等等）
 * 3.把处理后的参数放回到请求列表里面
 * @author tom
 */
@Slf4j
public class DecryptDataFilter implements Filter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /** 需要过滤的地址 */
    private static List<String> urlList = Arrays.asList("/app*/**");

    /** 判断地址是否需要过滤 */
    public boolean isPast(String requestUrl) {
        for (String url : urlList) {
            if (requestUrl.equals(url)) {
                return true;
            }
        }
        return false;
    }

    /** 过滤器初始化 */
    @Override
    public void init(FilterConfig filterConfig){

    }

    /** 进行链式过滤需求 */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        log.info("过滤器执行开始");
        String url = ((HttpServletRequest) req).getRequestURI().substring(((HttpServletRequest)req).getContextPath().length());

        //通过地址对特定的请求进行处理，如果不需要可以不用，如果不用，就会对使用的请求进行过滤
        if (isPast(url)) {
            DecryptDataRequestWrapper baseMsgRequestWrapper = new DecryptDataRequestWrapper(
                    (HttpServletRequest) req);
            // 1.获取需要处理的参数
            String userId = baseMsgRequestWrapper.getParameter("userId");
            String body = baseMsgRequestWrapper.getParameter("body");
            // 2.处理body中的数据，将加密的数据进行解密
            String encrypt = null;
            try {
                encrypt = RsaUtils.encrypt(body, redisTemplate.opsForValue().get(PropertyUtils.APP_USER_PUBLIC_KEY + userId));
            } catch (Exception e) {
                e.printStackTrace();
            }
            baseMsgRequestWrapper.setParameter("body",encrypt);
            // 3.放行，把我们的baseMsgRequestWrapper放到方法当中
            chain.doFilter(baseMsgRequestWrapper, resp);
        } else {
            chain.doFilter(req, resp);
        }
    }

    /** 过滤器销毁 */
    @Override
    public void destroy() {

    }
}
