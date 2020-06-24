package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.AppMassageMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.PlatformApplyMapper;
import com.caidao.mapper.PlatformReasonMapper;
import com.caidao.pojo.AppMassage;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.PlatformApply;
import com.caidao.pojo.PlatformReason;
import com.caidao.service.PlatformReasonService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
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
    private DeptUserMapper deptUserMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private PlatformApplyMapper platformApplyMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AppMassageMapper appMassageMapper;

    /**
     * 完成审批
     * @param platformReason
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void completeApprovalWithOpinion(PlatformReason platformReason ) {
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
        //判断是否有带审批意见(0代表没有审批意见)，如果没有，直接是任务完成，流程进入下一步
        Integer opinions = platformReason.getOpinion();
        String opinion = null;
        TaskQuery taskQuery = taskService.createTaskQuery();
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        if (opinions == 0){
            String[] requests = platformReason.getRequestId().split(",");
            for (String taskId : requests) {
                //将消息中的内容删掉
                deleteCompleteMassage(taskId);
                String businessKey = toTaskIdGetBusinessKey(taskId);
                Task task = taskQuery.taskId(taskId).singleResult();
                //判断任务是否被拾取
                String assignee = task.getAssignee();
                if (assignee == null) {
                    throw new MyException("请先拾取任务");
                }
                String instanceId = task.getProcessInstanceId();
                taskService.complete(taskId);
                ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
                if (instance == null) {
                    //审批完成
                    Integer integer = platformApplyMapper.successEndFlatCarPlanTask(Integer.parseInt(businessKey));
                    if (integer == 0) {
                        throw new MyException("审批失败，请联系管理员");
                    }
                    //设置一条消息通知申请人审批结果
                    notifyApplicant(businessKey,instance.getName() + "审批通过");
                } else {
                    //新增消息库中下一个候选人未读信息
                    addNextCandidateMassage(instanceId, opinion);
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
            //将消息中的内容删掉
            deleteCompleteMassage(taskId);
            taskService.setVariable(taskId,"ApprovalOpinion",opinion);
            //获取业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            Task task = taskQuery.taskId(taskId).singleResult();
            //判断任务是否被拾取
            String assignee = task.getAssignee();
            if (assignee == null) {
                throw new MyException("请先拾取任务");
            }
            String instanceId = task.getProcessInstanceId();
            taskService.complete(taskId);
            ProcessInstance instance = instanceQuery.processInstanceId(instanceId).singleResult();
            //流程完成后判断是否流程完成，如果完成，则改审批状态为不通过，反之则跳过状态步骤
            if (opinions == 2 && instance == null) {
                Integer integer = platformApplyMapper.fileEndFlatCarPlanTask(Integer.parseInt(businessKey));
                if (integer == 0) {
                    throw new MyException("审批失败，请联系管理员");
                }
                //设置一条消息通知申请人审批结果
                notifyApplicant(businessKey,instance.getName() + "审批未通过");
            } else {
                //新增消息库中下一个候选人未读信息
                addNextCandidateMassage(instanceId, opinion);
            }
        }
    }

    /**
     * 新增消息库中下一个候选人未读信息
     * @param instanceId
     */
    private void addNextCandidateMassage(String instanceId, String opinion) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        //反求流程定义的Key
        Task task = taskQuery.processInstanceId(instanceId).singleResult();
        String definitionKey = task.getTaskDefinitionKey();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(definitionKey);
        // 输出连线
        List<SequenceFlow> outFlows = flowNode.getOutgoingFlows();
        String name = loopNextPoint(bpmnModel, outFlows, opinion);
        String candidateUsers = platformApplyMapper.getcandidate(name, instanceId);
        //将用户批量放在信息表中 ,获取流程ID
        String taskId1 = task.getId();
        String taskName = task.getName();
        Boolean result =insertAppMassage(taskId1, taskName, candidateUsers);
        if (result) {
            throw new MyException("插入消息失败，请联系管理员");
        }
    }

    /**
     * 删除对应的消息里面的数据
     * @param taskId
     */
    private void deleteCompleteMassage(String taskId) {
        int delete = appMassageMapper.delete(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getTaskId, taskId));
        if (delete == 0) {
            throw new MyException("删除消息失败，请联系管理员");
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
     * 递归调用 找到下一个节点里面的name
     * @param bpmnModel
     * @param outFlows
     * @param opinion
     * @return
     */
    private String loopNextPoint(BpmnModel bpmnModel, List<SequenceFlow> outFlows, String opinion) {
        for (SequenceFlow outFlow : outFlows) {
            FlowElement element = outFlow.getTargetFlowElement();
            if (element instanceof ExclusiveGateway) {
                String id = element.getId();
                FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(id);
                List<SequenceFlow> out = flowNode.getOutgoingFlows();
                return loopNextPoint(bpmnModel, out, opinion);
            } else if (element instanceof UserTask) {
                UserTask userTask = (UserTask) element;
                if (opinion == null) {
                    List<String> candidateUsers = userTask.getCandidateUsers();
                    return candidateUsers.get(0).replaceAll("\\$", "")
                            .replaceAll("\\{", "")
                            .replaceAll("}", "");
                }
                String expression = outFlow.getConditionExpression();
                if (expression.contains("不同意")) {
                    expression = "不同意";
                } else {
                    expression = "同意";
                }
                if (opinion.equals(expression)) {
                    List<String> candidateUsers = userTask.getCandidateUsers();
                    return candidateUsers.get(0).replaceAll("\\$", "")
                            .replaceAll("\\{", "")
                            .replaceAll("}", "");
                }
            }
        }
        return null;
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

    /**
     * 递归调用 找到下一个节点里面的name
     * @param candidateUser
     * @return
     */
    private Boolean insertAppMassage(String taskId, String taskName, String candidateUser) {
        String[] candidateUse = candidateUser.split(",");
        List<AppMassage> massages = new ArrayList<>(candidateUse.length);
        for (String sting : candidateUse) {
            AppMassage appMassage = new AppMassage();
            appMassage.setIsRead(1);
            appMassage.setMassageName(taskName + "任务审批");
            appMassage.setTaskId(Integer.parseInt(taskId));
            appMassage.setCreateTime(LocalDateTime.now());
            appMassage.setDeptUsername(sting);
            massages.add(appMassage);
        }
        return appMassageMapper.insertBatches(massages);
    }

    /**
     * 通知申请人申请结果
     * @param businessKey
     * @param content
     * @return
     */
    private Integer notifyApplicant(String businessKey, String content) {
        //获取申请人id
        PlatformApply platformApply = platformApplyMapper.selectById(Integer.parseInt(businessKey));
        Integer userId = platformApply.getCreateId();
        DeptUser deptUser = deptUserMapper.selectById(userId);
        AppMassage appMassage = new AppMassage();
        appMassage.setIsRead(1);
        appMassage.setMassageName(content);
        appMassage.setTaskId(null);
        appMassage.setCreateTime(LocalDateTime.now());
        appMassage.setDeptUsername(deptUser.getUsername());
        return appMassageMapper.insert(appMassage);
    }
}