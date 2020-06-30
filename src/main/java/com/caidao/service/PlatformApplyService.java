package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.ActivityQueryParam;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.PlatformApply;
import org.activiti.engine.history.HistoricTaskInstance;

import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-06-11
 */
public interface PlatformApplyService extends IService<PlatformApply> {

    /**
     * 保存一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    Map<String, Object> saveFlatCarPlan(PlatformApply platformApply);

    /**
     * 开始一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    Map<String, Object> startPlanTasks(PlatformApply platformApply);

    /**
     * 获取用户的所有任务列表
     * @param param
     * @return
     */
    List<Map<String, Object>> getApprovalList(ActivityQueryParam param);

    /**
     * 删除保存的平板车计划任务流程
     * @param id
     * @param reason
     * @return 流程实例Id
     */
    Boolean removePlanById(String id, String reason);

    /**
     * 获取用户的任务列表
     * @param param
     * @return
     */
    List<Map<String, Object>> getDeptUserTaskList(ActivityQueryParam param);

    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    List<HistoricTaskInstance> getUserHistoryTaskList(ActivityQueryParam param);

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    String getPlanOwnerGroupTask(String taskId);

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * @param taskId
     * @param username
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    void flatCarPlan2OtherUser(String taskId, String username);

    /**
     * 用户归还组任务
     * @param taskId
     * @return
     */
    void backPlanOwner2GroupTask(String taskId);

    /**
     * 获得可以编制的任务
     * @return
     */
    List<PlatformApply> getPlatformOrganizationTasks();

    /**
     * 开始一个取消任务申请
     * @param businessKey
     * @return
     */
    Map<String, String> startCancelApplyTask(Integer businessKey);

    /**
     * 部门评价人员进行评价
     * @param taskId
     * @return
     */
    boolean departmentEvaluate(String taskId);

    /**
     * 司机完成任务的执行
     * @param taskId
     * @return
     */
    String driverCompleteTask(String taskId);

    /**
     * 取消任务司机完成任务的执行
     * @param param
     * @return
     */
    String flatcarCancelDriverCompleteTask(FlatCarCancelParam param);

    /**
     * 取消任务部门评价人员进行评价
     * @param param
     * @return
     */
    boolean flatcarCancelDepartmentEvaluate(FlatCarCancelParam param);
}
