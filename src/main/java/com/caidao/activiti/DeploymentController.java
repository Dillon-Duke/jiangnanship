package com.caidao.activiti;

import com.caidao.util.PropertiesReaderUtils;
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
    @GetMapping("/getLastestDeployment")
    public ResponseEntity<ProcessDefinition> getLastestDeployment(){

        Map<String, String> map = PropertiesReaderUtils.getMap();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        ProcessDefinition flatcarPlan = processDefinitionQuery.processDefinitionName(map.get("DeploymentName"))
                                                                .latestVersion()
                                                                .singleResult();

        return ResponseEntity.ok(flatcarPlan);
    }

}
