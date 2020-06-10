package com.caidao.config;

import com.caidao.filter.JsonIllegalCharacterFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tom
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean sessionExpireFilter(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JsonIllegalCharacterFilter());
        registrationBean.addUrlPatterns("/app*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
