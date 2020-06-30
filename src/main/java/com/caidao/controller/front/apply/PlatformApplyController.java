package com.caidao.controller.front.apply;

import com.caidao.common.ResponseEntity;
import com.caidao.param.ActivityQueryParam;
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
     * 保存一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @ApiOperation("保存一个平板车计划流程")
    @PostMapping("/savePlatformTasks")
    public ResponseEntity<Map<String, Object>> savePlatformTasks(@RequestBody PlatformApply platformApply){

        Map<String, Object> flatCarPlan = platformApplyService.saveFlatCarPlan(platformApply);
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

        Boolean remove = platformApplyService.removePlanById(id,reason);
        if (remove) {
            return ResponseEntity.ok("未提交任务删除成功");
        }
        return ResponseEntity.error("未提交任务删除失败");
    }

    /**
     * 开始一个平板车任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @ApiOperation("开始一个平板车流程")
    @PostMapping("/startPlatformTask")
    public ResponseEntity<Map<String, Object>> startPlatformTask(@RequestBody PlatformApply platformApply){

        Map<String, Object> flatCarPlan = platformApplyService.startPlanTasks(platformApply);
        return ResponseEntity.ok(flatCarPlan);
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户来拾取组任务")
    @GetMapping("/getPlatformOwnerGroupTask")
    public ResponseEntity<String> getPlatformOwnerGroupTask(String taskId){
        String massage = platformApplyService.getPlanOwnerGroupTask(taskId);
        return ResponseEntity.ok(massage);
    }

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    @ApiOperation("完成带意见的审批")
    @PostMapping("/completeApprovalWithOpinion")
    public ResponseEntity<String> completeApprovalWithOpinion(@RequestBody PlatformReason platformReason){
        String newTaskId = platformReasonService.completeApprovalWithOpinion(platformReason);
        return ResponseEntity.ok(newTaskId);
    }

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    @ApiOperation("完成不带意见的审批")
    @GetMapping("/completeApprovalWithNoOpinion/{taskId}")
    public ResponseEntity<String> completeApprovalWithNoOpinion(@PathVariable("taskId") String taskId){
        String newTaskId = platformReasonService.completeApprovalWithNoOpinion(taskId);
        return ResponseEntity.ok(newTaskId);
    }

    /**
     * 司机完成任务的执行
     * @param taskId
     * @return
     */
    @ApiOperation("司机完成任务的执行")
    @GetMapping("/driverCompleteTask/{taskId}")
    public ResponseEntity<String> driverCompleteTask (@PathVariable("taskId") String taskId) {
        String newTaskId = platformApplyService.driverCompleteTask(taskId);
        return ResponseEntity.ok(newTaskId);
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

        platformApplyService.flatCarPlan2OtherUser(taskId,username);
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
        platformApplyService.backPlanOwner2GroupTask(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取所有用户的任务列表
     * @param param
     * @return
     */
    @ApiOperation("获取所有用户的任务列表")
    @PostMapping("/getAllDeptUserTaskList")
    public ResponseEntity<List<Map<String, Object>>> getAllDeptUserTaskList(@RequestBody ActivityQueryParam param) {

        //获取待条件的审核列表
        List<Map<String, Object>> approvalList = platformApplyService.getApprovalList(param);
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
        List<Map<String, Object>> list = platformApplyService.getDeptUserTaskList(param);
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

        List<HistoricTaskInstance> list = platformApplyService.getUserHistoryTaskList(param);
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
        List<Car> carList = carService.getFreeCarList();
        //获得审批到可以编制的任务
        List<PlatformApply> organizationList = platformApplyService.getPlatformOrganizationTasks();
        map.put("freeCars",carList);
        map.put("organizationTasks",organizationList);
        return ResponseEntity.ok(map);
    }

    /**
     * 车辆与任务做绑定 多车与一个任务作为绑定
     * @param carId
     * @param taskId
     * @return
     */
    @ApiOperation("车辆与任务做一个绑定")
    @GetMapping("/saveOrBindTaskWithCar")
    public ResponseEntity<Void> saveOrBindTaskWithCar(List<String> carId, String taskId){
        carService.saveOrBindTaskWithCar(carId,taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * 用户车辆绑定
     * @param deptUserCar
     * @return
     */
    @ApiOperation("用户车辆绑定")
    @PostMapping("/userBindCar")
    public ResponseEntity<DeptUserCar> userBindCar(@RequestBody DeptUserCar deptUserCar){
        DeptUserCar userBindCar = deptUserService.userBindCar(deptUserCar);
        return ResponseEntity.ok(userBindCar);
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
    public ResponseEntity<String> completeCancelApplyTask (@RequestBody FlatCarCancelParam param) {
        String taskId = platformReasonService.completeCancelApplyTask(param);
        return ResponseEntity.ok(taskId);
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
    @PostMapping("/flatcarUpdateWithNoStart")
    public ResponseEntity<Boolean> flatcarUpdateWithNoStart (@RequestBody FlatCarCancelParam param) {
        boolean evaluate = platformApplyService.flatcarCancelDepartmentEvaluate(param);
        return ResponseEntity.ok(evaluate);
    }

    /**
     * 已开始执行的平板车任务调整
     * @param param
     * @return
     */
    @ApiOperation("已开始执行的平板车任务调整")
    @PostMapping("/flatcarUpdateWithStart")
    public ResponseEntity<Boolean> flatcarUpdateWithStart (@RequestBody FlatCarCancelParam param) {
        boolean evaluate = platformApplyService.flatcarCancelDepartmentEvaluate(param);
        return ResponseEntity.ok(evaluate);
    }

}