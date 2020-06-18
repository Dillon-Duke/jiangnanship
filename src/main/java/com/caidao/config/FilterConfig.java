package com.caidao.config;

import com.caidao.filter.SaveAppBaseMsgFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 */
@Configuration
public class FilterConfig {

    /**
     * 配置过滤器
     * order属性:控制过滤器加载顺序：数字越小，加载越早
     * @return
     */
    @Bean
    public FilterRegistrationBean ValidatorFilterRegistration() {
        //新建过滤器注册类
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Map<String,String> map = new HashMap<>(2);
        registration.setInitParameters(map);
        // 添加我们写好的过滤器
        registration.setFilter( new SaveAppBaseMsgFilter());
        // 设置过滤器的URL模式
        registration.addUrlPatterns("/*");
        registration.setOrder(100);
        return registration;
    }

}
