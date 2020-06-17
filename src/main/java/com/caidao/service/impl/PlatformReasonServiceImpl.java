package com.caidao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.PlatformReasonMapper;
import com.caidao.mapper.PlatformMapper;
import com.caidao.pojo.PlatformReason;
import com.caidao.pojo.DeptUser;
import com.caidao.service.PlatformReasonService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
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
@Slf4j
public class PlatformReasonServiceImpl extends ServiceImpl<PlatformReasonMapper, PlatformReason> implements PlatformReasonService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private PlatformMapper platformMapper;

    /**
     * 完成审批 不在platform表中设置是否为驳运计划冗余字段
     * @param platformReason
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void completeApprovalWithOpinion(PlatformReason platformReason) {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(platformReason,"审批原因不能为空");
        log.info("用户{}完成了任务的审批",deptUser.getUsername());
        platformReason.setCreateId(deptUser.getCreateId());

        //将审批意见存入数据库
        List<PlatformReason> reasons = saveApprovalReasons(platformReason);
        boolean batch = this.saveOrUpdateBatch(reasons);
        if (!batch) {
            throw new MyException("审批失败，请联系管理员");
        }

        //判断是否有带审批意见，如果没有，直接是任务完成，流程进入下一步
        Integer opinions = platformReason.getOpinion();
        String opinion;
        TaskQuery taskQuery = taskService.createTaskQuery();
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        if (opinions == 0){
            String[] requests = platformReason.getRequestId().split(",");
            for (String taskId : requests) {
                String businessKey = toTaskIdGetBusinessKey(taskId);
                String instanceId = taskQuery.taskId(taskId).singleResult().getProcessInstanceId();
                taskService.complete(taskId);
                ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
                if (instance == null) {
                    Integer integer = platformMapper.endFlatCarPlanTask(Integer.parseInt(businessKey));
                    if (integer <= 0) {
                        throw new MyException("审批失败，请联系管理员");
                    }
                }
            }
        }

        //带有流程进入意见赋值
         if (opinions == 1){
            opinion = "同意";
        } else {
            opinion = "不同意";
        }
        //循环进行流程完成
        String[] requests = platformReason.getRequestId().split(",");
        for (String taskId : requests) {
            taskService.setVariable(taskId,"ApprovalOpinion",opinion);

            //获取业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            String instanceId = taskQuery.taskId(taskId).singleResult().getProcessInstanceId();
            taskService.complete(taskId);
            ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
            //流程完成后判断是否流程完成，如果完成，则改审批状态为不通过，反之则跳过状态步骤
            if (opinions == 2 && instance == null) {
                Integer integer = platformMapper.setApprovalOpinion(Integer.parseInt(businessKey));
                if (integer <= 0) {
                    throw new MyException("审批失败，请联系管理员");
                }
            }

        }

    }

    /**
     * 完成审批 在platform表中设置是否为驳运计划冗余字段
     * @param platformReason
     * @return
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void completeApprovalWithOpinions(PlatformReason platformReason) {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(platformReason,"审批原因不能为空");
        log.info("用户{}完成了任务的审批",deptUser.getUsername());
        platformReason.setCreateId(deptUser.getCreateId());

        //将审批意见存入数据库
        List<PlatformReason> reasons = saveApprovalReasons(platformReason);
        boolean batch = this.saveOrUpdateBatch(reasons);
        if (!batch) {
            throw new MyException("审批失败，请联系管理员");
        }

        //判断是否有带审批意见，如果没有，直接是任务完成，流程进入下一步
        Integer opinions = platformReason.getOpinion();
        String opinion;
        TaskQuery taskQuery = taskService.createTaskQuery();
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        if (opinions == 0){
            String[] requests = platformReason.getRequestId().split(",");
            for (String taskId : requests) {
                String businessKey = toTaskIdGetBusinessKey(taskId);
                String instanceId = taskQuery.taskId(taskId).singleResult().getProcessInstanceId();
                taskService.complete(taskId);
                ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
                if (instance != null) {
                    String name = taskQuery.processInstanceId(instance.getProcessInstanceId()).singleResult().getName();
                    if (name.equals("编制驳动计划")){
                        platformMapper.remarkOrganization(Integer.parseInt(businessKey));
                    }
                } else {
                    platformMapper.endFlatCarPlanTask(Integer.parseInt(businessKey));
                }
            }
        }

        //带有流程进入意见赋值
        if (opinions == 1){
            opinion = "同意";
        } else {
            opinion = "不同意";
        }
        //循环进行流程完成
        String[] requests = platformReason.getRequestId().split(",");
        for (String taskId : requests) {
            taskService.setVariable(taskId,"ApprovalOpinion",opinion);

            //获取业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            String instanceId = taskQuery.taskId(taskId).singleResult().getProcessInstanceId();
            taskService.complete(taskId);
            ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
            //流程完成后判断是否流程完成，如果完成，则改审批状态为不通过，反之则跳过状态步骤
            if (opinions == 2 && instance == null) {
                platformMapper.setApprovalOpinion(Integer.parseInt(businessKey));
            } else {
                String name = taskQuery.processInstanceId(instance.getProcessInstanceId()).singleResult().getName();
                if (name.equals("编制驳动计划")){
                    platformMapper.remarkOrganization(Integer.parseInt(businessKey));
                }
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
    private List<PlatformReason> saveApprovalReasons(PlatformReason platformReason) {
        String[] requestIds = platformReason.getRequestId().split(",");
        List<PlatformReason> reasons = new ArrayList<>(requestIds.length);
        for (String requestId : requestIds) {
            PlatformReason reason = new PlatformReason();
            reason.setRequestId(requestId);
            reason.setOpinion(platformReason.getOpinion());
            reason.setReason(platformReason.getReason());
            reason.setReasonDescription(platformReason.getReasonDescription());
            reason.setCreateDate(LocalDateTime.now());
            reason.setCreateId(platformReason.getCreateId());
            reason.setState(1);
            reasons.add(reason);
        }
        return reasons;
    }
}