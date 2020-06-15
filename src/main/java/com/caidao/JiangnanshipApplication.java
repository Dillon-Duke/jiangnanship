package com.caidao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

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

@MapperScan("com.caidao.mapper")
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class JiangnanshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiangnanshipApplication.class, args);
    }

}
