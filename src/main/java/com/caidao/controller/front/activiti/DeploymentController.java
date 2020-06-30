package com.caidao.controller.front.activiti;

import com.caidao.anno.SysLogs;
import com.caidao.param.ActivityQueryParam;
import com.caidao.util.ActivitiObj2MapUtils;
import com.caidao.util.PropertiesReaderUtils;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
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

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 平板车计划流程的发布
     * @return
     */
    @ApiOperation("发布平板车计划任务流程")
    @GetMapping("/flatCarPlanDeploymentPublish")
    public ResponseEntity<String> flatCarPlanDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("flatcarPlanDeploymentZip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("flatcarPlanDeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy.getId());
    }

    /**
     * 平板车临时流程的发布
     * @return
     */
    @ApiOperation("发布平板车临时任务流程")
    @GetMapping("/flatCarTempDeploymentPublish")
    public ResponseEntity<String> flatCarTempDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("flatcarTempDeploymentZip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("flatcarTempDeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy.getId());
    }

    /**
     * 发布平板车快速任务流程
     * @return
     */
    @ApiOperation("发布平板车快速任务流程")
    @GetMapping("/flatCarFastDeploymentPublish")
    public ResponseEntity<String> flatCarFastDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("flatcarFastDeploymentZip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("flatcarFastDeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy.getId());
    }

    /**
     * 平板车衍生流程的发布
     * @return
     */
    @ApiOperation("发布平板车衍生任务流程")
    @GetMapping("/flatCarOtherTempDeploymentPublish")
    public ResponseEntity<String> flatCarOtherTempDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("flatcarOtherTempDeploymentZip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("flatcarOtherTempDeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy.getId());
    }

    /**
     * 平板车取消流程的发布
     * @return
     */
    @ApiOperation("发布平板车取消任务流程")
    @GetMapping("/flatCarCancelDeploymentPublish")
    public ResponseEntity<String> flatCarCancelDeploymentPublish(){

        Map<String, String> map = PropertiesReaderUtils.getMap();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(map.get("flatcarCancelDeploymentZip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        DeploymentBuilder deployment = repositoryService.createDeployment();
        Deployment deploy = deployment.name(map.get("flatcarCancelDeploymentName"))
                .addZipInputStream(zipInputStream)
                .deploy();
        return ResponseEntity.ok(deploy.getId());
    }

    /**
     * 通过流程定义Id挂起所有流程实录
     * @param deploymentId
     * @return
     */
    @ApiOperation("通过流程定义Id挂起所有流程实录")
    @GetMapping("/suspendedDeployment")
    public ResponseEntity<Void> suspendedDeployment(String deploymentId){
        ProcessDefinitionQuery definitionQuery = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition definition = definitionQuery.deploymentId(deploymentId).singleResult();
        boolean suspended = definition.isSuspended();
        if (!suspended){
            repositoryService.suspendProcessDefinitionById(definition.getId(),true,null);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 查询已经部署的流程
     * @return
     */
    @ApiOperation("查询已经部署的流程")
    @GetMapping("/getDeployment")
    public ResponseEntity<List<Map<String, Object>>> getDeployment(){

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<Map<String, Object>> listMap = new ArrayList<>();
        List<ProcessDefinition> list = processDefinitionQuery.orderByProcessDefinitionVersion().desc().list();
        String[] ps = { "id","key","version","deploymentId"};
        for (ProcessDefinition task : list) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }
        return ResponseEntity.ok(listMap);
    }

    /**
     * 通过Id删除平板车已发布的流程
     * @param param
     * @return
     */
    @SysLogs("通过Id删除平板车已发布的流程")
    @ApiOperation("通过Id删除平板车已发布的流程")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFlatCarDeployment(@RequestBody ActivityQueryParam param){

        Assert.notNull(param,"参数不能为空");
        try {
            repositoryService.deleteDeployment(param.getDeploymentId(), param.isForced());
        } catch (RuntimeException e) {
            throw new RuntimeException("有实例正在使用该流程，不能删除");
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 查询正在运行的实例
     * @return
     */
    @ApiOperation("查询正在运行的实例")
    @PostMapping("/getAllInstance")
    public ResponseEntity<List<Map<String, Object>>> getAllInstance(@RequestBody  ActivityQueryParam activityParam){

        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        List<Map<String, Object>> listMap = new ArrayList<>();
        List<ProcessInstance> instances = null;

        //判断是否有查询条件
        if (activityParam.getProcessDefinitionName() == null || activityParam.getProcessDefinitionName() == ""){
            instances = instanceQuery.orderByProcessInstanceId().desc().list();
        } else {
            instances = instanceQuery.orderByProcessInstanceId().processDefinitionName(activityParam.getProcessDefinitionName()).desc().list();
        }
        String[] ps = { "id","businessKey","processInstanceId","startTime","deploymentId"};
        for (ProcessInstance task : instances) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }
        return ResponseEntity.ok(listMap);
    }
}
