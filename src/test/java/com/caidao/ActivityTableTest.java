package com.caidao;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;


@SpringBootTest
public class ActivityTableTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void test () {

        //3.转化出ZipInputStream流对象
        InputStream is = ActivityTableTest.class.getClassLoader().getResourceAsStream("activiti/flatCarFastTasks.zip");

        //将 inputstream流转化为ZipInputStream流
        ZipInputStream zipInputStream = new ZipInputStream(is);

        //3.进行部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("平板车临时任务流程")
                .deploy();

        //4.输出部署的一些信息
        System.out.println(deployment.getName());
        System.out.println(deployment.getId());

    }

    @Test
    void test1 () {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        RuntimeService runtimeService = processEngine.getRuntimeService();

        Map<String, Object> variables = new HashMap<>( + 2);
        variables.put("startName", "zhangsan");
        variables.put("dept", "驳运部门申请");

        //3.创建流程实例  流程定义的key需要知道 holiday
        ProcessInstance processInstance =  runtimeService.startProcessInstanceByKey("flatCarFastTasks","1233",variables);

        //4.输出实例的相关信息
        System.out.println("流程部署ID"+processInstance.getDeploymentId());//null
        System.out.println("流程定义ID"+processInstance.getProcessDefinitionId());//holiday:1:4
        System.out.println("流程实例ID"+processInstance.getId());//2501
        System.out.println("活动ID"+processInstance.getActivityId());//null


    }
}
