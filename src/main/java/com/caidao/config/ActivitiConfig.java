package com.caidao.config;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author tom
 * @since 2020-5-12
 */

@Configuration
public class ActivitiConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 初始化配置，将创建28张表
     * @return
     */
    @Bean
    public StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration(){
        StandaloneProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();
        engineConfiguration.setDataSource(dataSource);
        //数据库没有表的时候自动创建表
        engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        engineConfiguration.setAsyncExecutorActivate(false);
        return engineConfiguration;
    }

    @Bean
    public ProcessEngine processEngine(){
        return standaloneProcessEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService(){
       return processEngine().getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() {
        return processEngine().getRuntimeService();
    }

    @Bean
    public TaskService taskService() {
        return processEngine().getTaskService();
    }

    @Bean
    public HistoryService historyService(){
        return processEngine().getHistoryService();
    }
}
