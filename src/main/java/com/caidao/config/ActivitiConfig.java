package com.caidao.config;

import org.activiti.engine.*;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * @author tom
 * @since 2020-5-12
 * 工作流配置，将工作流的5个service交给spring管理
 */

@Configuration
public class ActivitiConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    /**
     * 初始化配置，将创建28张表
     * @return
     */
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(){
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setAsyncExecutorActivate(false);
        configuration.setTransactionManager(platformTransactionManager);
        //数据库没有表的时候自动创建表
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        return configuration;
    }

    /**
     * 注入工作流引擎
     * @return
     */
    @Bean
    public ProcessEngine processEngine(){
        return springProcessEngineConfiguration().buildProcessEngine();
    }

    /**
     * 注入流程控制类
     * @return
     */
    @Bean
    public RepositoryService repositoryService(){
       return processEngine().getRepositoryService();
    }

    /**
     * 注入运行的service
     * @return
     */
    @Bean
    public RuntimeService runtimeService() {
        return processEngine().getRuntimeService();
    }

    /**
     * 注入任务service
     * @return
     */
    @Bean
    public TaskService taskService() {
        return processEngine().getTaskService();
    }

    /**
     * 注入历史记录service
     * @return
     */
    @Bean
    public HistoryService historyService(){
        return processEngine().getHistoryService();
    }

    @Bean
    public ManagementService managementService () {
        return processEngine().getManagementService();
    }

}
