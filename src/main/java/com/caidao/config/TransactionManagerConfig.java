package com.caidao.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 该类是让事务生效
 * @author tom
 * @since 2020-05-30
 */
@EnableTransactionManagement
@Configuration
public class TransactionManagerConfig {

    /**
     * 其中 dataSource 框架会自动为我们注入
     */
    @Bean(name = "platformTransactionManager")
    public PlatformTransactionManager txManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public Object testBean(@Qualifier("platformTransactionManager") PlatformTransactionManager platformTransactionManager) {
        return new Object();
    }


}