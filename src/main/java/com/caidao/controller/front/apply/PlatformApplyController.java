package com.caidao.controller.front.apply;

import com.caidao.common.MyResponseEntity;
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
    public MyResponseEntity<Map<String, String>> savePlatformTasks(@RequestBody PlatformApply platformApply){
        Map<String, String> flatCarPlan = platformApplyService.saveFlatCarPlan(platformApply);
        return MyResponseEntity.ok(flatCarPlan);
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param id
     * @return 流程实例Id
     */
    @ApiOperation("删除一个平板车计划流程")
    @DeleteMapping("/deletePlatformTask/{id}")
    public MyResponseEntity<String> deletePlatformTask(@PathVariable("id") String id,String reason){
        Boolean remove = platformApplyService.removePlanById(id,reason);
        if (remove) {
            return MyResponseEntity.ok("未提交任务删除成功");
        }
        return MyResponseEntity.error("未提交任务删除失败");
    }

    /**
     * 开始一个平板车任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @ApiOperation("开始一个平板车流程")
    @PostMapping("/startPlatformTask")
    public MyResponseEntity<Map<String, String>> startPlatformTask(@RequestBody PlatformApply platformApply){

        Map<String, String> flatCarPlan = platformApplyService.startPlanTasks(platformApply);
        return MyResponseEntity.ok(flatCarPlan);
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户来拾取组任务")
    @GetMapping("/getPlatformOwnerGroupTask")
    public MyResponseEntity<String> getPlatformOwnerGroupTask(String taskId){
        String massage = platformApplyService.getPlanOwnerGroupTask(taskId);
        return MyResponseEntity.ok(massage);
    }

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    @ApiOperation("完成带意见的审批")
    @PostMapping("/completeApprovalWithOpinion")
    public MyResponseEntity<String> completeApprovalWithOpinion(@RequestBody PlatformReason platformReason){
        String newTaskId = platformReasonService.completeApprovalWithOpinion(platformReason);
        return MyResponseEntity.ok(newTaskId);
    }

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    @ApiOperation("完成不带意见的审批")
    @GetMapping("/completeApprovalWithNoOpinion/{taskId}")
    public MyResponseEntity<String> completeApprovalWithNoOpinion(@PathVariable("taskId") String taskId){
        String newTaskId = platformReasonService.completeApprovalWithNoOpinion(taskId);
        return MyResponseEntity.ok(newTaskId);
    }

    /**
     * 司机完成任务的执行
     * @param carPlatformApples
     * @param taskId
     * @return
     */
    @ApiOperation("司机完成任务的执行")
    @PostMapping("/driverCompleteTask")
    public MyResponseEntity<String> driverCompleteTask (@RequestBody List<CarPlatformApply> carPlatformApples, String taskId) {
        String newTaskId = platformApplyService.driverCompleteTask(carPlatformApples, taskId);
        return MyResponseEntity.ok(newTaskId);
    }

    /**
     * 部门评价人员进行评价
     * @param taskId
     * @return
     */
    @ApiOperation("部门评价人员进行评价")
    @GetMapping("/departmentEvaluate/{taskId}")
    public MyResponseEntity<Boolean> departmentEvaluate (@PathVariable("taskId") String taskId) {
        boolean evaluate = platformApplyService.departmentEvaluate(taskId);
        return MyResponseEntity.ok(evaluate);
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @ApiOperation("任务指派给别人")
    @PostMapping("/platform2OtherUser")
    public MyResponseEntity<Void> platform2OtherUser(String taskId, String username){
        platformApplyService.flatCarPlan2OtherUser(taskId,username);
        return MyResponseEntity.ok().build();
    }

    /**
     * 用户归还组任务
     * @param taskId
     * @return
     */
    @ApiOperation("用户归还组任务")
    @GetMapping("/backPlatformOwner2GroupTask")
    public MyResponseEntity<Void> backPlatformOwner2GroupTask(String taskId){
        platformApplyService.backPlanOwner2GroupTask(taskId);
        return MyResponseEntity.ok().build();
    }

    /**
     * 获取所有用户的任务列表
     * @param param
     * @return
     */
    @ApiOperation("获取所有用户的任务列表")
    @PostMapping("/getAllDeptUserTaskList")
    public MyResponseEntity<List<Map<String, Object>>> getAllDeptUserTaskList(@RequestBody ActivityQueryParam param) {

        //获取待条件的审核列表
        List<Map<String, Object>> approvalList = platformApplyService.getApprovalList(param);
        return MyResponseEntity.ok(approvalList);
    }

    /**
     * 获取用户的任务列表
     * @param param
     * @return
     */
    @ApiOperation("获取用户的任务列表")
    @PostMapping("/getDeptUserTaskList")
    public MyResponseEntity<List<Map<String, Object>>> getDeptUserTaskList(@RequestBody ActivityQueryParam param) {
        List<Map<String, Object>> list = platformApplyService.getDeptUserTaskList(param);
        return MyResponseEntity.ok(list);
    }

    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    @ApiOperation("获取用户的历史任务")
    @PostMapping("/getUserHistoryTaskList")
    public MyResponseEntity<List<HistoricTaskInstance>> getUserHistoryTaskList(@RequestBody ActivityQueryParam param) {

        List<HistoricTaskInstance> list = platformApplyService.getUserHistoryTaskList(param);
        return MyResponseEntity.ok(list);
    }

    /**
     * 获得可编制的任务
     * @return
     */
    @ApiOperation("获得可编制的任务")
    @GetMapping("/getOrganizationTasks")
    public MyResponseEntity<Map<String,Object>> getOrganizationTasks(){
        Map<String, Object> map = new HashMap<>(2);
        //获得空闲的车辆
        List<Car> carList = carService.getFreeCarList();
        //获得审批到可以编制的任务
        List<PlatformApply> organizationList = platformApplyService.getPlatformOrganizationTasks();
        map.put("freeCars",carList);
        map.put("organizationTasks",organizationList);
        return MyResponseEntity.ok(map);
    }

    /**
     * 车辆与任务做绑定 多车与一个任务作为绑定
     * @param carPlatformApply
     * @return
     */
    @ApiOperation("车辆与任务做一个绑定")
    @PostMapping("/saveOrBindTaskWithCar")
    public MyResponseEntity<Boolean> saveOrBindTaskWithCar(@RequestBody List<CarPlatformApply> carPlatformApply){
        Boolean result = carService.saveOrBindTaskWithCar(carPlatformApply);
        return MyResponseEntity.ok(result);
    }

    /**
     * 用户车辆绑定
     * @param deptUserCar
     * @return
     */
    @ApiOperation("用户车辆绑定")
    @PostMapping("/userBindCar")
    public MyResponseEntity<Boolean> userBindCar(@RequestBody List<DeptUserCar> deptUserCar){
        Boolean bindCar = deptUserService.userBindCar(deptUserCar);
        return MyResponseEntity.ok(bindCar);
    }

    /**
     * 用户车辆解绑
     * @param ids
     * @return
     */
    @ApiOperation("用户车辆解绑")
    @DeleteMapping
    public MyResponseEntity<Boolean> userNnBindCar(@RequestBody List<Integer> ids){
        Boolean result = deptUserService.userNnBindCar(ids);
        return MyResponseEntity.ok(result);
    }

    /**
     * 查询用户是否有取消权限以及任务执行状态
     * @param businessKey
     * @return
     */
    @ApiOperation("查询用户是否有取消权限")
    @GetMapping("/startCancelApplyTask/{businessKey}")
    public MyResponseEntity<Map<String, String>> startCancelApplyTask (@PathVariable("businessKey") Integer businessKey) {
        Map<String, String> map = platformApplyService.startCancelApplyTask(businessKey);
        return MyResponseEntity.ok(map);
    }

    /**
     * 开始一个取消任务申请
     * @param param
     * @return
     */
    @ApiOperation("开始一个取消任务申请")
    @PostMapping("/cancelApplyTaskStart")
    public MyResponseEntity<Map<String, String>> cancelApplyTaskStart (@RequestBody FlatCarCancelParam param) {
        Map<String, String> map = platformReasonService.cancelApplyTaskStart(param);
        return MyResponseEntity.ok(map);
    }

    /**
     * 取消任务完成取消任务审批
     * @param param
     * @return
     */
    @ApiOperation("取消任务完成取消任务审批")
    @PostMapping("/completeCancelApplyTask")
    public MyResponseEntity<Boolean> completeCancelApplyTask (@RequestBody FlatCarCancelParam param) {
        Boolean result = platformReasonService.completeCancelApplyTask(param);
        return MyResponseEntity.ok(result);
    }

    /**
     * 取消任务司机接单和执行
     * @param param
     * @return
     */
    @ApiOperation("取消任务司机接单和执行")
    @PostMapping("/cancelApplyTaskDriverWorking")
    public MyResponseEntity<String> cancelApplyTaskDriverWorking (@RequestBody FlatCarCancelParam param) {
        platformReasonService.cancelApplyTaskDriverWorking(param);
        return MyResponseEntity.ok(null);
    }

    /**
     * 司机取消任务完成任务的执行
     * @param param
     * @return
     */
    @ApiOperation("司机取消任务完成任务的执行")
    @PostMapping("/flatcarCancelDriverCompleteTask")
    public MyResponseEntity<String> flatcarCancelDriverCompleteTask (@RequestBody FlatCarCancelParam param) {
        String newTaskId = platformApplyService.flatcarCancelDriverCompleteTask(param);
        return MyResponseEntity.ok(newTaskId);
    }

    /**
     * 取消任务部门评价人员进行评价
     * @param param
     * @return
     */
    @ApiOperation("取消任务部门评价人员进行评价")
    @PostMapping("/flatcarCancelDepartmentEvaluate")
    public MyResponseEntity<Boolean> flatcarCancelDepartmentEvaluate (@RequestBody FlatCarCancelParam param) {
        boolean evaluate = platformApplyService.flatcarCancelDepartmentEvaluate(param);
        return MyResponseEntity.ok(evaluate);
    }

    /**
     * 未开始执行的平板车任务调整
     * @param param
     * @return
     */
    @ApiOperation("未开始执行的平板车任务调整")
    @PostMapping("/flatcarAdjustmentWithNoStart")
    public MyResponseEntity<Boolean> flatcarAdjustmentWithNoStart (@RequestBody FlatCarAdjustmentParam param) {
        boolean evaluate = platformReasonService.flatcarAdjustmentWithNoStart(param);
        return MyResponseEntity.ok(evaluate);
    }

    /**
     * 已开始执行的平板车任务调整
     * @param param
     * @return
     */
    @ApiOperation("已开始执行的平板车任务调整")
    @PostMapping("/flatcarAdjustmentWithStart")
    public MyResponseEntity<Boolean> flatcarAdjustmentWithStart (@RequestBody FlatCarAdjustmentParam param) {
        boolean evaluate = platformReasonService.flatcarAdjustmentWithStart(param);
        return MyResponseEntity.ok(evaluate);
    }

}