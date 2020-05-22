package com.caidao;

import com.caidao.activiti.DeploymentService;
import com.sun.xml.internal.bind.v2.TODO;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ActivitiTableTest {

    @Autowired
    private DeploymentService deploymentService;

    @Test
    void publish(){
        deploymentService.publishProcess();
    }
}
