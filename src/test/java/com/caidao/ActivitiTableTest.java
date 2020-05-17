package com.caidao;

import com.sun.xml.internal.bind.v2.TODO;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ActivitiTableTest {

    //创建28张表
    @Test
    public void getActivitiTable(){
        //TODO 不知道为什么 从配置文件中导入数据库找不到合适的驱动  以后再研究
        //创建ProcessEngineConfiguration对象
        ProcessEngineConfiguration resource = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");

        //创建ProcessEngine对象
        ProcessEngine engine = resource.buildProcessEngine();

        System.out.println(engine);
    }
}
