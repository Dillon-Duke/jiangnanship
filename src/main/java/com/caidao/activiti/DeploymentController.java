package com.caidao.activiti;

import com.caidao.util.PropertiesReaderUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author tom
 */
@RestController
@RequestMapping("/activiti")
public class DeploymentController {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 平板车计划申请流程的发布
     */
    @GetMapping("/flatcarPlanDeploymentPublish")
    public ResponseEntity<Void> flatcarPlanDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();

        DeploymentBuilder deployment = repositoryService.createDeployment();
        deployment.name(map.get("flatcarPlanDeploymentName"))
                    .addClasspathResource(map.get("flatcarPlanDeploymentBpmn"))
                    .addClasspathResource(map.get("flatcarPlanDeploymentPng")).deploy();
        return ResponseEntity.ok().build();
    }

    /**
     * 获取平板车申请流程的列表
     * @return
     */
    @GetMapping("/getFlatcarPlanDeploymentId")
    public ResponseEntity<List<ProcessDefinition>> getFlatcarPlanDeploymentId(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = definitionQuery.processDefinitionKey(map.get("flatcarPlanApply"))
                                                        .orderByProcessDefinitionVersion()
                                                        .desc()
                                                        .list();
        return ResponseEntity.ok(list);
    }



}
