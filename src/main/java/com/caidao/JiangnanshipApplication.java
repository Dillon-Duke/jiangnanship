package com.caidao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;

/**
 * @author tom
 * @since 2020-05-12
 */

//@MapperScan("com.caidao.mapper")
//@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
//public class JiangnanshipApplication extends SpringBootServletInitializer {
//
//    public static void main(String[] args) {
//        SpringApplication.run(JiangnanshipApplication.class, args);
//    }
//
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(JiangnanshipApplication.class);
//    }
//}

@EnableCaching
@MapperScan(value = "com.caidao.mapper",annotationClass = Repository.class)
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class JiangnanshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiangnanshipApplication.class, args);
    }

}
