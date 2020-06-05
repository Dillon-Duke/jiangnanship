package com.caidao.controller.front.flatcar;

import com.caidao.entity.DeptUser;
import com.caidao.param.ActivitiParam;
import com.caidao.util.ActivitiObj2MapUtils;
import com.caidao.util.PropertiesReaderUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author tom
 * @since 2020-06-05
 */

@RestController
@RequestMapping("/flatCar/temp")
@Slf4j
public class FlatCarTempController {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    public static final Logger logger =  LoggerFactory.getLogger(FlatCarTempController.class);

    @ApiOperation("开始一个平板车临时流程")
    @GetMapping("/start")
    public ResponseEntity<String> startTempTasks() throws Exception {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}申请了一个流程",deptUser.getUsername());

        //TODO 新建衍生任务之前需要和主任务进行绑定，绑定规则再说  以后这些需要放到对应的service层中

        //模仿数据库中生成对应的一条数据并拿到数据id
        String BusinessKey = "1";
        Map<String, Object> variables = new HashMap<>(1);
        variables.put("startName",deptUser.getUsername());
        // TODO 需要从后台方面查询是哪一个部门申请的，
        variables.put("dept","驳运部门申请");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PropertiesReaderUtils.getMap().get("flatcarTempDeploymentId"),BusinessKey,variables);
        return ResponseEntity.ok(processInstance.getProcessInstanceId());
    }

    /**
     * 查询平板车任务审批列表
     * @return
     */
    @ApiOperation("获取当前用户的任务列表")
    @GetMapping("/taskList")
    public ResponseEntity<List<Map<String, Object>>> getUserOwnTask() {

        //TODO 在衍生任务进行时是否需要更新数据库？

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();

        //activiti中获取需要审批的任务列表
        List<Map<String, Object>> listMap = new ArrayList<>();
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> taskList =taskQuery.processDefinitionKey(PropertiesReaderUtils.getMap().get("flatcarTempDeploymentId"))
                .taskAssignee(deptUser.getUsername())
                .orderByTaskId()
                .desc()
                .list();
        String[] ps = { "id", "name","processInstanceId","owner","assignee"};
        for (Task task : taskList) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }
        return ResponseEntity.ok(listMap);
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @ApiOperation("任务指派给别人")
    @PostMapping("/toOtherUser")
    public ResponseEntity<Void> toOtherUser(String taskId, String deptUserName) {

        Assert.notNull(taskId,"任务ID不能为空");
        Assert.notNull(deptUserName,"用户名称不能为空");
        log.info("任务id为{}交给{}",taskId,deptUserName);

        taskService.setOwner(taskId, deptUserName);
        return ResponseEntity.ok().build();
    }

    /**
     * 下一个任务是个人的调用流程任务的完成
     * @param taskId
     * @param deptUserName
     * @return
     */
    @GetMapping("/taking")
    @ApiOperation("下一个任务是个人的调用流程任务")
    public ResponseEntity<Void> takingTempTask(String taskId,String deptUserName) {

        //TODO 在衍生任务进行时是否需要更新数据库？

        //设置下一个办理人
        Map<String, Object> map = new HashMap<>(1);
        map.put("applyName",deptUserName);
        taskService.setVariables(taskId,map);
        taskService.complete(taskId);

        return ResponseEntity.ok().build();
    }

    /**
     * 下一个任务是组的调用流程任务
     * @param taskId
     * @param userNameList
     * @return
     */
    @GetMapping("/takingGroup")
    @ApiOperation("下一个任务是组的调用流程任务")
    public ResponseEntity<Void> takingTempGroupTask(String taskId,@RequestParam(value = "userNameList") List<String> userNameList){

        Assert.notNull(userNameList,"组成员不能为空");
        log.info("平板车计划任务中新增组成员{}",userNameList);
        IdentityService service = processEngine.getIdentityService();
        User user = service.newUser("1");
        for (String string : userNameList) {
            if ((user.getFirstName()+user.getLastName()) == string){
                taskService.addCandidateUser(taskId,"1");
            }
            user.setFirstName("lao");
            user.setLastName("wang");
            service.saveUser(user);
            taskService.addCandidateUser(taskId,"2");
        }

        return ResponseEntity.ok().build();
    }

//    @GetMapping("/takingGroup")
//    @ApiOperation("下一个任务是组的调用流程任务")
//    public ResponseEntity<Void> takingTempGroupTask(String taskId,@RequestParam(value = "userNameList") List<String> userNameList){
//
//        Assert.notNull(userNameList,"组成员不能为空");
//        log.info("平板车计划任务中新增组成员{}",userNameList);
//
//        //TODO 在衍生任务进行时是否需要更新数据库？
//        taskService.setVariable(taskId,"canditidateUser",userNameList);
//        taskService.complete(taskId);
//        return ResponseEntity.ok().build();
//    }

    /**
     * 临时任务的完成
     * @param taskId
     * @return
     */
    @GetMapping("/end")
    @ApiOperation("结束临时任务")
    public ResponseEntity<Void> endPlanTask(String taskId){
        //TODO 完成数据库状态更新
        taskService.complete(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询用户的组任务列表
     * @return
     */
    @ApiOperation("获取用户的组任务")
    @GetMapping("/listTempOwnerGroupTask")
    public ResponseEntity<List<Map<String, Object>>> listTempOwnerGroupTask(){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("查询用户名为{}的组任务列表",deptUser.getUsername());

        List<Map<String, Object>> listMap = new ArrayList<>();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(PropertiesReaderUtils.getMap().get("flatcarTempDeploymentId"))
                .taskCandidateUser(deptUser.getUsername())
                .list();

        String[] ps = { "id", "name","processInstanceId"};
        for (Task task : list) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }

        return ResponseEntity.ok(listMap);
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户来拾取组任务")
    @GetMapping("/getTempOwnerGroupTask")
    public ResponseEntity<Void> getTempOwnerGroupTask(String taskId){

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
    @GetMapping("/backTempOwnerGroupTask")
    public ResponseEntity<Void> backTempOwnerGroupTask(String taskId){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());

        taskService.setAssignee(taskId,null);
        return ResponseEntity.ok().build();
    }

    /**
     * 用户历史任务查询
     * @return
     */
    @ApiOperation("用户历史任务查询")
    @PostMapping("/userHistory")
    public ResponseEntity<List<HistoricTaskInstance>> getHistoryTask(@RequestBody ActivitiParam activitiParam){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}查询历史记录",deptUser.getUsername());

        HistoricTaskInstanceQuery instanceQuery = historyService.createHistoricTaskInstanceQuery();

        List<HistoricTaskInstance> list = null;
        if (activitiParam.getTaskName() == null || activitiParam.getTaskName().isEmpty() || activitiParam.getTaskName() == ""){
            list = instanceQuery.taskAssignee(deptUser.getUsername()).orderByHistoricTaskInstanceEndTime().desc().list();
        } else {
            list = instanceQuery.taskAssignee(deptUser.getUsername()).taskName(activitiParam.getTaskName()).orderByHistoricTaskInstanceEndTime().desc().list();
        }
        return ResponseEntity.ok(list);
    }

}
