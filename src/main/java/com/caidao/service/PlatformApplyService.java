package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.ActivityQueryParam;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.PlatformApply;
import com.caidao.pojo.PlatformReason;
import org.activiti.engine.history.HistoricTaskInstance;

import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-06-11
 */
public interface PlatformApplyService extends IService<PlatformApply> {

    /**
     * 开始一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    Map<String, String> saveOrStartPlanTasks(PlatformApply platformApply);

    /**
     * 获取用户的所有任务列表
     * @param username
     * @param taskState
     * @return
     */
    List<Map<String, Object>> getApprovalList(String username, String taskState);

    /**
     * 删除保存的平板车计划任务流程
     * @param platformReason
     * @return 流程实例Id
     */
    Boolean removePlanById(PlatformReason platformReason);


    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    List<HistoricTaskInstance> getUserHistoryTaskList(ActivityQueryParam param);

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * @param taskId
     * @param username
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     */
    void flatCarPlan2OtherUser(String taskId, String username);

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
    boolean driverCompleteTask(String taskId);

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

    /**
     * 通过申请Id获取详细的申请信息
     * @param businessKey
     * @return
     */
    Map<String, Object> getApplyDetailInfoByApplyId(Integer businessKey);

    /**
     * 通过id获取对应的申请单详情
     * @param prsId
     * @return
     */
    PlatformApply getPlatformById(Integer prsId);
}
