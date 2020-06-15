package com.caidao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.ApprovalReasonMapper;
import com.caidao.mapper.PlatformMapper;
import com.caidao.pojo.ApprovalReason;
import com.caidao.service.ApprovalReasonService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.TaskQuery;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-13
 */
@Service
public class ApprovalReasonServiceImpl extends ServiceImpl<ApprovalReasonMapper, ApprovalReason> implements ApprovalReasonService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private PlatformMapper platformMapper;

    /**
     * 完成审批
     * @param approvalReason
     * @return
     */
    @Override
    @Transactional(rollbackFor = MyException.class)
    public void completeApprovalWithOpinion(ApprovalReason approvalReason) {

        //将审批意见存入数据库
        List<ApprovalReason> reasons = saveApprovalReasons(approvalReason);
        boolean batch = this.saveOrUpdateBatch(reasons);
        if (!batch) {
            throw new MyException("审批失败，请联系管理员");
        }

        //流程的完成
        String[] requests = approvalReason.getRequestId().split(",");
        for (String taskId : requests) {
            taskService.setVariableLocal(taskId,"ApprovalOpinion",approvalReason.getOpinion());
            taskService.complete(taskId);

            //如果不同意，则需要更改平板车计划任务表中审核状态，为不同意状态
            if (approvalReason.getOpinion() == 0) {
                String businessKey = toTaskIdGetBusinessKey(taskId);
                Integer integer = platformMapper.setApprovalOpinion(businessKey);
                if (integer <= 0) {
                    throw new MyException("审批失败，请联系管理员");
                }
            }
        }

    }

    /**
     * 计划任务的完成
     * @param approvalReason
     * @return
     */
    @Override
    public void endFlatCarPlanTask(ApprovalReason approvalReason) {
        //将审批意见存入数据库
        List<ApprovalReason> reasons = saveApprovalReasons(approvalReason);
        boolean batch = this.saveOrUpdateBatch(reasons);
        if (!batch) {
            throw new MyException("审批失败，请联系管理员");
        }

        //流程的完成
        String[] requests = approvalReason.getRequestId().split(",");
        for (String taskId : requests) {
            taskService.complete(taskId);

            //流程完成，状态改为审批完成
            String businessKey = toTaskIdGetBusinessKey(taskId);
            Integer integer = platformMapper.endFlatCarPlanTask(businessKey);
            if (integer <= 0) {
                throw new MyException("审批失败，请联系管理员");
            }
        }
    }

    /**
     * 通过任务Id获取业务Id
     * @param taskId
     * @return
     */
    private String toTaskIdGetBusinessKey(String taskId) {
        String businessKey;
        TaskQuery taskQuery = taskService.createTaskQuery();
        TaskEntity taskEntity = (TaskEntity) taskQuery.taskId(taskId).singleResult();
        HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
        HistoricProcessInstance processInstance = instanceQuery.processInstanceId(taskEntity.getProcessInstanceId()).singleResult();
        if (processInstance.getSuperProcessInstanceId() != null && processInstance.getBusinessKey() == null) {
            processInstance = instanceQuery.processInstanceId(processInstance.getSuperProcessInstanceId()).singleResult();
            businessKey = processInstance.getBusinessKey();
        } else {
            businessKey = processInstance.getBusinessKey();
        }
        return businessKey;
    }

    /**
     * 将一条记录中多个任务id分成多个记录
     */
    @NotNull
    private List<ApprovalReason> saveApprovalReasons(ApprovalReason approvalReason) {
        String[] requestIds = approvalReason.getRequestId().split(",");
        List<ApprovalReason> reasons = new ArrayList<>(requestIds.length);
        for (String requestId : requestIds) {
            ApprovalReason reason = new ApprovalReason();
            reason.setRequestId(requestId);
            reason.setOpinion(approvalReason.getOpinion());
            reason.setReason(approvalReason.getReason());
            reason.setReasionDescription(approvalReason.getReasionDescription());
            reason.setReserve1(approvalReason.getReserve1());
            reason.setReserve2(approvalReason.getReserve2());
            reason.setReserve3(approvalReason.getReserve3());
            reason.setReserve4(approvalReason.getReserve4());
            reason.setReserve5(approvalReason.getReserve5());
            reason.setCreateDate(LocalDateTime.now());
            reason.setCreateId(approvalReason.getCreateId());
            reason.setState(1);
            reasons.add(reason);
        }
        return reasons;
    }
}