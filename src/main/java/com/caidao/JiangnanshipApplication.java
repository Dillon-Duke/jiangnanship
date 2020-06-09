package com.caidao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * @author tom
 * @since 2020-05-12
 */

/** 开启数据加密注解 */
@MapperScan("com.caidao.mapper")
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class JiangnanshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiangnanshipApplication.class, args);
    }

}
