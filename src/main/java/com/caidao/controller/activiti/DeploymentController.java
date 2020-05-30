package com.caidao.controller.activiti;

import com.caidao.util.PropertiesReaderUtils;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

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
     * @return
     */
    @ApiOperation("发布工作流程任务")
    @GetMapping("/DeploymentPublish")
    public ResponseEntity<Deployment> DeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("DeploymentZip"));

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("DeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy);
    }

    /**
     * 通过名称查询已经部署的流程
     * @return
     */
    @ApiOperation("通过流程任务的名字查询流程任务")
    @GetMapping("/getLastestDeploymentByName")
    public ResponseEntity<List<ProcessDefinition>> getLastestDeploymentByName( String DeploymentName){

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionName(DeploymentName)
                                                                .orderByProcessDefinitionVersion()
                                                                .desc()
                                                                .list();
        //TODO 之后再看看这个接口是不是需要，字段需要返回哪些，直接返回字段的类型不匹配报错，需要新建实体类转一下
        System.out.println(list);
        return null;
    }

    /**
     * 通过key查询已经部署的流程
     * @return
     */
    @ApiOperation("通过流程任务的key查询流程任务")
    @GetMapping("/getLastestDeploymentByKey")
    public ResponseEntity<ProcessDefinition> getLastestDeploymentByKey(String DeploymentKey){

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        ProcessDefinition flatcarPlan = processDefinitionQuery.processDefinitionKey(DeploymentKey)
                .latestVersion()
                .singleResult();
        //TODO 之后再看看这个接口是不是需要，字段需要返回哪些，直接返回字段的类型不匹配报错，需要新建实体类转一下
        return ResponseEntity.ok(null);
    }

}
