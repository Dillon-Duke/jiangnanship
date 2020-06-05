package com.caidao.controller.front.flatcar;

import com.caidao.entity.DeptUser;
import com.caidao.util.ActivitiObj2MapUtils;
import com.caidao.util.PropertiesReaderUtils;
import com.caidao.util.PropertyUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tom
 * @since 2020-06-04
 */

@RestController
@RequestMapping("/flatCar/cancel")
@Slf4j
public class FlatCarCancelTasksController {

    public static final Logger logger =  LoggerFactory.getLogger(FlatCarCancelTasksController.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 任务取消开始
     * @return
     */
    @GetMapping("/planStart")
    @ApiOperation("开始一个取消任务流程")
    public ResponseEntity<String> startPlanCancelTask(){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("{}发起取消任务流程",deptUser.getUsername());

        // TODO 需要绑定的业务主键
        String BusinessKey = "1";

        // TODO 查询计划流程驳运是否开始 需要在自定义表中设置一个字段进行标识
        Map<String, Object> variables = new HashMap<>(2);
        variables.put("startName",deptUser.getUsername());
        variables.put("tranTaskStart","开始");

        //将是否开始驳运放到redis里面
        redisTemplate.opsForValue().set(PropertyUtils.IS_START_TRANS + "tranTaskStart","开始");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PropertiesReaderUtils.getMap().get("flatcarCancelDeploymentId"), BusinessKey, variables);
        return ResponseEntity.ok(processInstance.getProcessInstanceId());
    }

    /**
     * 查询平板车任务审批列表
     * @return
     */
    @ApiOperation("获取当前用户的任务列表")
    @GetMapping("/taskList")
    public ResponseEntity<List<Map<String, Object>>> getUserOwnTask(){

        //TODO 在衍生任务进行时是否需要更新数据库？

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();

        //activiti中获取需要审批的任务列表
        List<Map<String, Object>> listMap = new ArrayList<>();
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> taskList =taskQuery.processDefinitionKey(PropertiesReaderUtils.getMap().get("flatcarCancelDeploymentId"))
                .taskAssignee(deptUser.getUsername())
                .orderByTaskId()
                .desc()
                .list();
        String[] ps = {"id", "name","processInstanceId","owner","assignee"};
        for (Task task : taskList) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }
        return ResponseEntity.ok(listMap);
    }

    public ResponseEntity<Void> takingCancelTasks(String taskId, DeptUser deptUser){

        //TODO 在衍生任务进行时是否需要更新数据库？

        //设置下一个办理人
        Map<String, Object> map = new HashMap<>(1);
        map.put("applyName",deptUser.getUsername());
        taskService.setVariables(taskId,map);
        taskService.complete(taskId);

        return ResponseEntity.ok().build();
    }

    /**
     * 衍生任务的完成
     * @param taskId
     * @return
     */
    @GetMapping("/end")
    @ApiOperation("结束衍生任务")
    public ResponseEntity<Void> endCancelTask(String taskId){
        //TODO 完成数据库状态更新
        taskService.complete(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @ApiOperation("任务指派给别人")
    @PostMapping("/toOtherUser")
    public ResponseEntity<Void> toOtherUser(String taskId, DeptUser deptUser){

        Assert.notNull(taskId,"任务ID不能为空");
        Assert.notNull(deptUser.getUsername(),"用户名称不能为空");
        log.info("任务id为{}交给{}",taskId,deptUser.getUsername());

        taskService.setOwner(taskId, deptUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 查询用户的组任务列表
     * @return
     */
    @ApiOperation("获取用户的组任务")
    @GetMapping("/listflatcarCancelOwnerGroupTask")
    public ResponseEntity<List<Task>> listflatcarCancelOwnerGroupTask(){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("查询用户名为{}的组任务列表",deptUser.getUsername());

        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(PropertiesReaderUtils.getMap().get("flatcarOtherTempDeploymentId"))
                .taskCandidateUser(deptUser.getUsername())
                .list();

        return ResponseEntity.ok(list);
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户来拾取组任务")
    @GetMapping("/getflatcarCancelOwnerGroupTask")
    public ResponseEntity<Void> getflatcarCancelOwnerGroupTask(String taskId){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());

        taskService.claim(taskId,deptUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 用户归还组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户归还组任务")
    @GetMapping("/backflatcarCancelOwnerGroupTask")
    public ResponseEntity<Void> backflatcarCancelOwnerGroupTask(String taskId){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());

        taskService.setAssignee(taskId,null);
        return ResponseEntity.ok().build();
    }

}