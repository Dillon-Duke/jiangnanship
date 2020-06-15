package com.caidao.controller.front.flatcar;

import com.caidao.common.ResponseEntity;
import com.caidao.param.ActivityQueryParam;
import com.caidao.pojo.ApprovalReason;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.Platform;
import com.caidao.service.ApprovalReasonService;
import com.caidao.service.PlatformService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-23
 */
@RestController
@RequestMapping("/flatcar/plan")
@Slf4j
public class PlatformController {

    @Autowired
    private ApprovalReasonService approvalReasonService;

    @Autowired
    private PlatformService platformService;

    public static final Logger logger =  LoggerFactory.getLogger(PlatformController.class);

    /**
     * 保存一个平板车计划任务流程
     * @param platform
     * @return 流程实例Id
     */
    @ApiOperation("保存一个平板车计划流程")
    @PostMapping("/savePlatformTasks")
    public ResponseEntity<Map<String, Object>> savePlatformTasks(@RequestBody Platform platform){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());

        platform.setCreateId(deptUser.getUserId());
        platform.setApplyName(deptUser.getUsername());
        Map<String, Object> flatCarPlan = platformService.saveFlatCarPlan(platform);
        return ResponseEntity.ok(flatCarPlan);
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param id
     * @return 流程实例Id
     */
    @ApiOperation("删除一个平板车计划流程")
    @DeleteMapping("/deletePlatformTask/{id}")
    public ResponseEntity<String> deletePlatformTask(@PathVariable("id") String id,String reason){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}删除未申请的任务",deptUser.getUsername());

        Boolean remove = platformService.removePlanById(id,reason);
        if (remove) {
            return ResponseEntity.ok("未提交任务删除成功");
        }
        return ResponseEntity.error("未提交任务删除失败");
    }

    /**
     * 开始一个平板车任务流程
     * @param platform
     * @return 流程实例Id
     */
    @ApiOperation("开始一个平板车流程")
    @PostMapping("/startPlatformTask")
    public ResponseEntity<Map<String, Object>> startPlatformTask(@RequestBody Platform platform){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());

        platform.setUpdateId(deptUser.getUserId());
        platform.setApplyName(deptUser.getUsername());
        Map<String, Object> flatCarPlan = platformService.startPlanTasks(platform);
        return ResponseEntity.ok(flatCarPlan);
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户来拾取组任务")
    @GetMapping("/getPlatformOwnerGroupTask")
    public ResponseEntity<Void> getPlatformOwnerGroupTask(String taskId){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());

        platformService.getPlanOwnerGroupTask(taskId,deptUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 完成审批
     * @param approvalReason
     * @return
     */
    @ApiOperation("完成审批")
    @PostMapping("/completeApprovalWithOpinion")
    public ResponseEntity<Void> completeApprovalWithOpinion(@RequestBody ApprovalReason approvalReason){

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(approvalReason,"审批原因不能为空");
        log.info("用户{}完成了任务的审批",deptUser.getUsername());
        approvalReason.setCreateId(deptUser.getCreateId());
        approvalReasonService.completeApprovalWithOpinion(approvalReason);
        return ResponseEntity.ok().build();
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @ApiOperation("任务指派给别人")
    @PostMapping("/platform2OtherUser")
    public ResponseEntity<Void> platform2OtherUser(String taskId, DeptUser deptUser){

        Assert.notNull(taskId,"任务ID不能为空");
        Assert.notNull(deptUser.getUsername(),"用户名称不能为空");
        log.info("任务id为{}交给{}",taskId,deptUser.getUsername());
        platformService.flatCarPlan2OtherUser(taskId,deptUser.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 用户归还组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户归还组任务")
    @GetMapping("/backPlatformOwner2GroupTask")
    public ResponseEntity<Void> backPlatformOwner2GroupTask(String taskId){

        Assert.notNull(taskId,"任务ID不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());
        platformService.backPlanOwner2GroupTask(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 计划任务的完成
     * @param approvalReason
     * @return
     */
    @GetMapping("/endPlatformTask")
    @ApiOperation("平板车计划任务的完成")
    public ResponseEntity<Void> endPlatformTask(@RequestBody ApprovalReason approvalReason){

        String requestId = approvalReason.getRequestId();
        Assert.notNull(requestId,"任务ID不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());
        approvalReasonService.endFlatCarPlanTask(approvalReason);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户的所有任务列表
     * @param param
     * @return
     */
    @ApiOperation("获取用户的所有任务列表")
    @PostMapping("/getAllDeptUserTaskList")
    public ResponseEntity<List<Map<String, Object>>> getAllDeptUserTaskList(@RequestBody ActivityQueryParam param) {

        //默认查询的当前用户的所有审批的列表
        Assert.notNull(param,"参数不能为空");
        log.info("查询用户名为{}的任务列表",param.getUserName());

        //判断是查询自己的正在审批的任务还是别人正在审批的任务
        String username;
        if (param.getUserName() == null || param.getUserName() == ""){
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        } else {
            username = param.getUserName();
        }

        //获取待条件的审核列表
        List<Map<String, Object>> approvalList = platformService.getApprovalList(param, username);
        return ResponseEntity.ok(approvalList);
    }

    /**
     * 获取用户的任务列表
     * @param param
     * @return
     */
    @ApiOperation("获取用户的任务列表")
    @PostMapping("/getDeptUserTaskList")
    public ResponseEntity<List<Map<String, Object>>> getDeptUserTaskList(@RequestBody ActivityQueryParam param) {

        //默认查询的当前用户的需要审批的列表
        Assert.notNull(param,"参数不能为空");
        log.info("查询用户名为{}的任务列表",param.getUserName());

        //判断是查询自己的正在审批的任务还是别人正在审批的任务
        String username;
        if (param.getUserName() == null){
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        } else {
            username = param.getUserName();
        }

        List<Map<String, Object>> list = platformService.getDeptUserTaskList(param, username);
        return ResponseEntity.ok(list);
    }

    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    @ApiOperation("获取用户的历史任务")
    @PostMapping("/getUserHistoryTaskList")
    public ResponseEntity<List<HistoricTaskInstance>> getUserHistoryTaskList(@RequestBody ActivityQueryParam param) {

        Assert.notNull(param,"参数不能为空");
        log.info("查询用户名为{}的历史列表",param.getUserName());
        //默认查询当前用户的所有历史任务
        String username;
        if (param.getUserName() != null && param.getUserName() !=""){
            username = param.getUserName();
        } else {
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        }
        log.info("用户{}查询历史记录",username);

        List<HistoricTaskInstance> list1 = platformService.getUserHistoryTaskList(param, username);
        return ResponseEntity.ok(list1);
    }

    /**
     * 查询个人用户的组任务列表
     * @return
     */
    @ApiOperation("获取个人的组任务")
    @GetMapping("/listPlatformOwnerGroupTask")
    public ResponseEntity<List<Task>> listPlatformOwnerGroupTask(@RequestBody ActivityQueryParam param){

        Assert.notNull(param,"参数不能为空");
        log.info("查询用户名为{}的组任务列表",param.getUserName());

        List<Task> list = platformService.listPlanOwnerGroupTask(param);
        return ResponseEntity.ok(list);
    }
}