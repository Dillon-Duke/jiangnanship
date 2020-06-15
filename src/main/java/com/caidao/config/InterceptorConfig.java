package com.caidao.config;

import com.caidao.interceptor.AppBaseInterceptorAdapter;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author tom
 * @since 2020-06-11
 */
//@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport  {

    /** 配置拦截器拦截路径 */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new AppBaseInterceptorAdapter());
        //配置拦截前端的所有访问路径
        registration.addPathPatterns("/app*/**");

        super.addInterceptors(registry);
    }

    /** 配置资源路径拦截 */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        //配置所有的静态资源文件不进行拦截
        registry.addResourceHandler("/**").addResourceLocations("");
        super.addResourceHandlers(registry);
    }

    /** 配置视图类文件拦截 */
    @Override
    protected void configureViewResolvers(ViewResolverRegistry registry) {
        super.configureViewResolvers(registry);
    }
}
