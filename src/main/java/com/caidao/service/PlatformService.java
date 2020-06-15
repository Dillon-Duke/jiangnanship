package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.ActivityQueryParam;
import com.caidao.pojo.Platform;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;

import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-06-11
 */
public interface PlatformService extends IService<Platform> {

    /**
     * 保存一个平板车计划任务流程
     * @param platform
     * @return 流程实例Id
     */
    Map<String, Object> saveFlatCarPlan(Platform platform);

    /**
     * 开始一个平板车计划任务流程
     * @param platform
     * @return 流程实例Id
     */
    Map<String, Object> startPlanTasks(Platform platform);

    /**
     * 获取用户的所有任务列表
     * @param param
     * @param username
     * @return
     */
    List<Map<String, Object>> getApprovalList(ActivityQueryParam param,String username);

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
     * @param username
     * @return
     */
    List<Map<String, Object>> getDeptUserTaskList(ActivityQueryParam param, String username);

    /**
     * 获取用户的历史任务
     * @param param
     * @param username
     * @return
     */
    List<HistoricTaskInstance> getUserHistoryTaskList(ActivityQueryParam param, String username);

    /**
     * 查询个人用户的组任务列表
     * @return
     */
    List<Task> listPlanOwnerGroupTask(ActivityQueryParam param);

    /**
     * 用户拾取组任务
     * @param taskId
     * @param username
     * @return
     */
    void getPlanOwnerGroupTask(String taskId, String username);

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
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

}
