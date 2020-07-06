package com.caidao.controller.front.apply;

import com.caidao.param.ActivityQueryParam;
import com.caidao.param.FlatCarAdjustmentParam;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.*;
import com.caidao.service.CarService;
import com.caidao.service.DeptUserService;
import com.caidao.service.PlatformApplyService;
import com.caidao.service.PlatformReasonService;
import io.swagger.annotations.ApiOperation;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-23
 */
@RestController
@RequestMapping("/flatcar/plan")
public class PlatformApplyController {

    @Autowired
    private PlatformReasonService platformReasonService;

    @Autowired
    private PlatformApplyService platformApplyService;

    @Autowired
    private CarService carService;

    @Autowired
    private DeptUserService deptUserService;

    public static final Logger logger =  LoggerFactory.getLogger(PlatformApplyController.class);

    /**
     * 通过Id获取申请单的详细信息
     * @param prsId
     * @return 流程实例Id
     */
    @ApiOperation("通过Id获取申请单的详细信息")
    @GetMapping("/getPlatformById/{prsId}")
    public ResponseEntity<PlatformApply> getPlatformById(@PathVariable("prsId") Integer prsId){
        PlatformApply platformById = platformApplyService.getPlatformById(prsId);
        return ResponseEntity.ok(platformById);
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param platformReason
     * @return 流程实例Id
     */
    @ApiOperation("删除一个平板车计划流程")
    @PostMapping("/deletePlatformTask")
    public ResponseEntity<String> deletePlatformTask(@RequestBody PlatformReason platformReason){
        Boolean remove = platformApplyService.removePlanById(platformReason);
        if (remove) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 保存或者开始一个平板车流程
     * @param platformApply
     * @return 流程实例Id
     */
    @ApiOperation("保存或开始一个平板车流程")
    @PostMapping("/saveOrStartPlanTasks")
    public ResponseEntity<Map<String, String>> saveOrStartPlanTasks(@RequestBody PlatformApply platformApply){
        Map<String, String> flatCarPlan = platformApplyService.saveOrStartPlanTasks(platformApply);
        return ResponseEntity.ok(flatCarPlan);
    }

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    @ApiOperation("完成带意见的审批")
    @PostMapping("/completeApprovalWithOpinion")
    public ResponseEntity<Void> completeApprovalWithOpinion(@RequestBody PlatformReason platformReason){
        boolean opinion = platformReasonService.completeApprovalWithOpinion(platformReason);
        if (opinion) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    @ApiOperation("完成不带意见的审批")
    @GetMapping("/completeApprovalWithNoOpinion/{taskId}")
    public ResponseEntity<Void> completeApprovalWithNoOpinion(@PathVariable("taskId") String taskId){
        boolean opinion = platformReasonService.completeApprovalWithNoOpinion(taskId);
        if (opinion) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 获得所有的车辆信息，有车辆任务的显示车辆的任务，没有车辆任务的显示为空闲车辆
     * @return
     */
    @ApiOperation("获得所有的车辆信息，有车辆任务的显示车辆的任务，没有车辆任务的显示为空闲车辆")
    @GetMapping("/getAllCarsWithHaveTasksAndNoTasks/{date}")
    public ResponseEntity<Map<String, Object>> getAllCarsWithHaveTasksAndNoTasks(@PathVariable("date") LocalDateTime date){
        Map<String, Object> cars = carService.getAllCarsWithHaveTasksAndNoTasks(date);
        return ResponseEntity.ok(cars);
    }

    /**
     * 将已经绑定车辆的任务进行排序
     * @return
     */
    @ApiOperation("将已经绑定车辆的任务进行排序")
    @GetMapping("/getAllCarsWithHaveTasksAndNoTasks/{businessKey}")
    public ResponseEntity<Void> sortBindApplyTasks(@PathVariable("businessKey") Integer businessKey){
        carService.sortBindApplyTasks(businessKey);
        return ResponseEntity.ok().build();
    }

    /**
     * 自动绑定车辆与申请的关系
     * @return
     */
    @ApiOperation("将已经绑定车辆的任务进行排序")
    @GetMapping("/autoCompareCarWithApply")
    public ResponseEntity<Void> autoCompareCarWithApply(){
        carService.autoCompareCarWithApply();
        return ResponseEntity.ok().build();
    }

    /**
     * 通过申请Id获取详细的申请信息
     * @return
     */
    @ApiOperation("通过申请Id获取详细的申请信息")
    @GetMapping("/getApplyDetailInfoByApplyId/{businessKey}")
    public ResponseEntity<Map<String, Object>> getApplyDetailInfoByApplyId(@PathVariable("businessKey") Integer businessKey){
        Map<String, Object> applyId = platformApplyService.getApplyDetailInfoByApplyId(businessKey);
        return ResponseEntity.ok(applyId);
    }

    /**
     * 司机完成任务的执行
     * @param taskId
     * @return
     */
    @ApiOperation("司机完成任务的执行")
    @GetMapping("/driverCompleteTask/{taskId}")
    public ResponseEntity<String> driverCompleteTask (@PathVariable("taskId") String taskId) {
        boolean result = platformApplyService.driverCompleteTask(taskId);
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 部门评价人员进行评价
     * @param taskId
     * @return
     */
    @ApiOperation("部门评价人员进行评价")
    @GetMapping("/departmentEvaluate/{taskId}")
    public ResponseEntity<Boolean> departmentEvaluate (@PathVariable("taskId") String taskId) {
        boolean evaluate = platformApplyService.departmentEvaluate(taskId);
        return ResponseEntity.ok(evaluate);
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @ApiOperation("任务指派给别人")
    @PostMapping("/platform2OtherUser")
    public ResponseEntity<Void> platform2OtherUser(String taskId, String username){
        platformApplyService.flatCarPlan2OtherUser(taskId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户的任务列表
     * @param username
     * @param taskState
     * @return
     */
    @ApiOperation("获取用户的任务列表")
    @PostMapping("/getDeptUserTaskList")
    public ResponseEntity<List<Map<String, Object>>> getDeptUserTaskList(String username, String taskState) {
        //获取待条件的审核列表
        List<Map<String, Object>> approvalList = platformApplyService.getApprovalList(username,taskState);
        return ResponseEntity.ok(approvalList);
    }

    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    @ApiOperation("获取用户的历史任务")
    @PostMapping("/getUserHistoryTaskList")
    public ResponseEntity<List<HistoricTaskInstance>> getUserHistoryTaskList(@RequestBody ActivityQueryParam param) {
        List<HistoricTaskInstance> list = platformApplyService.getUserHistoryTaskList(param);
        return ResponseEntity.ok(list);
    }

    /**
     * 获得可编制的任务
     * @return
     */
    @ApiOperation("获得可编制的任务")
    @GetMapping("/getOrganizationTasks")
    public ResponseEntity<List<PlatformApply>> getOrganizationTasks(){
        List<PlatformApply> organizationList = platformApplyService.getPlatformOrganizationTasks();
        return ResponseEntity.ok(organizationList);
    }

    /**
     * 车辆与任务做绑定 多车与一个任务作为绑定
     * @param deptUserCarApplies
     * @return
     */
    @ApiOperation("车辆与任务做一个绑定")
    @PostMapping("/saveOrBindTaskWithCar")
    public ResponseEntity<Boolean> saveOrBindTaskWithCar(@RequestBody List<DeptUserCarApply> deptUserCarApplies){
        Boolean result = carService.saveOrBindTaskWithCar(deptUserCarApplies);
        return ResponseEntity.ok(result);
    }

    /**
     * 用户车辆绑定
     * @param deptUserCarApply
     * @return
     */
    @ApiOperation("用户车辆绑定")
    @PostMapping("/userBindCar")
    public ResponseEntity<Boolean> userBindCar(@RequestBody List<DeptUserCarApply> deptUserCarApply){
        Boolean bindCar = deptUserService.userBindCar(deptUserCarApply);
        return ResponseEntity.ok(bindCar);
    }

    /**
     * 用户车辆解绑
     * @param ids
     * @return
     */
    @ApiOperation("用户车辆解绑")
    @DeleteMapping
    public ResponseEntity<Boolean> userNnBindCar(@RequestBody List<Integer> ids){
        Boolean result = deptUserService.userNnBindCar(ids);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询用户是否有取消权限以及任务执行状态
     * @param businessKey
     * @return
     */
    @ApiOperation("查询用户是否有取消权限")
    @GetMapping("/startCancelApplyTask/{businessKey}")
    public ResponseEntity<Map<String, String>> startCancelApplyTask (@PathVariable("businessKey") Integer businessKey) {
        Map<String, String> map = platformApplyService.startCancelApplyTask(businessKey);
        return ResponseEntity.ok(map);
    }

    /**
     * 开始一个取消任务申请
     * @param param
     * @return
     */
    @ApiOperation("开始一个取消任务申请")
    @PostMapping("/cancelApplyTaskStart")
    public ResponseEntity<Map<String, String>> cancelApplyTaskStart (@RequestBody FlatCarCancelParam param) {
        Map<String, String> map = platformReasonService.cancelApplyTaskStart(param);
        return ResponseEntity.ok(map);
    }

    /**
     * 取消任务完成取消任务审批
     * @param param
     * @return
     */
    @ApiOperation("取消任务完成取消任务审批")
    @PostMapping("/completeCancelApplyTask")
    public ResponseEntity<Boolean> completeCancelApplyTask (@RequestBody FlatCarCancelParam param) {
        Boolean result = platformReasonService.completeCancelApplyTask(param);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消任务司机接单和执行
     * @param param
     * @return
     */
    @ApiOperation("取消任务司机接单和执行")
    @PostMapping("/cancelApplyTaskDriverWorking")
    public ResponseEntity<String> cancelApplyTaskDriverWorking (@RequestBody FlatCarCancelParam param) {
        platformReasonService.cancelApplyTaskDriverWorking(param);
        return ResponseEntity.ok(null);
    }

    /**
     * 司机取消任务完成任务的执行
     * @param param
     * @return
     */
    @ApiOperation("司机取消任务完成任务的执行")
    @PostMapping("/flatcarCancelDriverCompleteTask")
    public ResponseEntity<String> flatcarCancelDriverCompleteTask (@RequestBody FlatCarCancelParam param) {
        String newTaskId = platformApplyService.flatcarCancelDriverCompleteTask(param);
        return ResponseEntity.ok(newTaskId);
    }

    /**
     * 取消任务部门评价人员进行评价
     * @param param
     * @return
     */
    @ApiOperation("取消任务部门评价人员进行评价")
    @PostMapping("/flatcarCancelDepartmentEvaluate")
    public ResponseEntity<Boolean> flatcarCancelDepartmentEvaluate (@RequestBody FlatCarCancelParam param) {
        boolean evaluate = platformApplyService.flatcarCancelDepartmentEvaluate(param);
        return ResponseEntity.ok(evaluate);
    }

    /**
     * 未开始执行的平板车任务调整
     * @param param
     * @return
     */
    @ApiOperation("未开始执行的平板车任务调整")
    @PostMapping("/flatcarAdjustmentWithNoStart")
    public ResponseEntity<Boolean> flatcarAdjustmentWithNoStart (@RequestBody FlatCarAdjustmentParam param) {
        boolean evaluate = platformReasonService.flatcarAdjustmentWithNoStart(param);
        return ResponseEntity.ok(evaluate);
    }

    /**
     * 已开始执行的平板车任务调整
     * @param param
     * @return
     */
    @ApiOperation("已开始执行的平板车任务调整")
    @PostMapping("/flatcarAdjustmentWithStart")
    public ResponseEntity<Boolean> flatcarAdjustmentWithStart (@RequestBody FlatCarAdjustmentParam param) {
        boolean evaluate = platformReasonService.flatcarAdjustmentWithStart(param);
        return ResponseEntity.ok(evaluate);
    }

}