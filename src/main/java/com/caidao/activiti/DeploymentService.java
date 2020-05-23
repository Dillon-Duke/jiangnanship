package com.caidao.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author tom
 */
@Component
public class DeploymentService {

    @Value("${bpmnPublish}")
    private String flatCarPlanTaskBpmn;

    @Value("${pngPublish}")
    private String flatCarPlanTaskPng;

    @Value("${name}")
    private String name;


    @Autowired
    private RepositoryService repositoryService;

    public void publishProcess(){
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment
                .name(name)
                .addClasspathResource(flatCarPlanTaskBpmn)
                .addClasspathResource(flatCarPlanTaskPng).deploy();

        System.out.println(deploy);
    }

}
