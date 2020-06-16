package com.caidao.controller.front.flatcar;

import com.caidao.common.ResponseEntity;
import com.caidao.param.ActivityQueryParam;
import com.caidao.pojo.ApprovalReason;
import com.caidao.pojo.Car;
import com.caidao.pojo.Platform;
import com.caidao.service.ApprovalReasonService;
import com.caidao.service.PlatformService;
import com.caidao.service.SysCarService;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-23
 */
@RestController
@RequestMapping("/flatcar/plan")
public class PlatformController {

    @Autowired
    private ApprovalReasonService approvalReasonService;

    @Autowired
    private PlatformService platformService;

    @Autowired
    private SysCarService sysCarService;

    public static final Logger logger =  LoggerFactory.getLogger(PlatformController.class);

    /**
     * 保存一个平板车计划任务流程
     * @param platform
     * @return 流程实例Id
     */
    @ApiOperation("保存一个平板车计划流程")
    @PostMapping("/savePlatformTasks")
    public ResponseEntity<Map<String, Object>> savePlatformTasks(@RequestBody Platform platform){

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

        platformService.getPlanOwnerGroupTask(taskId);
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
    public ResponseEntity<Void> platform2OtherUser(String taskId, String username){

        platformService.flatCarPlan2OtherUser(taskId,username);
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
        platformService.backPlanOwner2GroupTask(taskId);
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

        //获取待条件的审核列表
        List<Map<String, Object>> approvalList = platformService.getApprovalList(param);
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

        List<Map<String, Object>> list = platformService.getDeptUserTaskList(param);
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

        List<HistoricTaskInstance> list = platformService.getUserHistoryTaskList(param);
        return ResponseEntity.ok(list);
    }

    /**
     * 获得可编制的任务
     * @return
     */
    @ApiOperation("获得可编制的任务")
    @GetMapping("/getOrganizationTasks")
    public ResponseEntity<Map<String,Object>> getOrganizationTasks(){
        Map<String, Object> map = new HashMap<>(2);
        //获得空闲的车辆
        List<Car> carList = sysCarService.getFreeCarList();
        //获得审批到可以编制的任务
        List<Platform> organizationList = platformService.getPlatformOrganizationTasks();
        map.put("freeCars",carList);
        map.put("organizationTasks",organizationList);
        return ResponseEntity.ok(map);
    }

}