package com.caidao.activiti;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author tom
 */
public class DeploymentService {

    @Value("${activiti.bpmnPublish}")
    private String flatCarPLanTaskBpmn;

    @Value("${activiti.pngPublish}")
    private String flatCarPLanTaskPng;


    @Autowired
    private RepositoryService repositoryService;

    public void publishProcess(){
        DeploymentBuilder deployment = repositoryService.createDeployment();

        deployment.addClasspathResource(flatCarPLanTaskBpmn)
                .addClasspathResource(flatCarPLanTaskPng).deploy();
    }

    public static void main(String[] args) {

    }
}
