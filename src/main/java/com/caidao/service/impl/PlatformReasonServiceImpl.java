package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.*;
import com.caidao.param.FlatCarAdjustmentParam;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.*;
import com.caidao.service.PlatformReasonService;
import com.caidao.util.EntityUtils;
import com.caidao.util.MapUtils;
import com.caidao.util.PropertiesReaderUtils;
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
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Dillon
 * @since 2020-06-13
 */
@Service
@Slf4j
public class PlatformReasonServiceImpl extends ServiceImpl<PlatformReasonMapper, PlatformReason> implements PlatformReasonService {

    @Autowired
    private PlatformReasonMapper platformReasonMapper;

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
    private AppTasksMassageMapper appTasksMassageMapper;

    @Autowired
    private DeptUserCarApplyMapper deptUserCarApplyMapper;

    @Autowired
    private CustomActivitiMapper customActivitiMapper;

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean completeApprovalWithOpinion(PlatformReason platformReason ) {
        Assert.notNull(platformReason,"审批原因不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("用户{}完成了任务的审批",deptUser.getUsername());
        //用户审批前拾取任务
        applyUserPickUpTask(platformReason.getTaskId(),deptUser.getUsername());
        //驳回时将审批意见存入数据库
        if (platformReason.getOpinion() == 2 && platformReason.getReason() != null) {
            platformReason.setCreateId(deptUser.getCreateId());
            platformReasonMapper.insert(platformReason);
        } else {
            throw new MyException("请编写驳回原因");
        }
        //如果同意，并书写同意原因，则将原因存在数据库中
        if (platformReason.getOpinion() == 1 && platformReason.getReason() != null) {
            platformReason.setCreateId(deptUser.getCreateId());
            platformReasonMapper.insert(platformReason);
        }
        //如果修改了起始结束时间，并且没有修改重要程度，则更新申请表中时间
        if (haveApplyStartTimeAndEndTime(platformReason.getApplyStartTime(),platformReason.getApplyEndTime()) && platformReason.getApplyIsImportant() == null) {
            String businessKey = toTaskIdGetBusinessKey(platformReason.getTaskId());
            platformApplyMapper.updateApplyStartTimeAndEndTimeWithApplyId(businessKey,platformReason.getApplyStartTime(),platformReason.getApplyEndTime());
        }
        //如果没有修改起始结束时间，修改重要程度，则更新申请表中重要程度
        if (haveApplyStartTimeAndEndTime(platformReason.getApplyStartTime(),platformReason.getApplyEndTime()) && platformReason.getApplyIsImportant() != null) {
            String businessKey = toTaskIdGetBusinessKey(platformReason.getTaskId());
            platformApplyMapper.updateApplyImportantWithApplyId(businessKey,platformReason.getApplyIsImportant());
        }
        //如果修改了起始结束时间，并且修改重要程度，则更新申请表中时间和重要程度
        if (haveApplyStartTimeAndEndTime(platformReason.getApplyStartTime(),platformReason.getApplyEndTime()) && platformReason.getApplyIsImportant() != null) {
            String businessKey = toTaskIdGetBusinessKey(platformReason.getTaskId());
            platformApplyMapper.updateApplyImportantAndApplyStartTimeAndEndTimeWithApplyId(businessKey,platformReason.getApplyStartTime(),platformReason.getApplyEndTime(),platformReason.getApplyIsImportant());
        }
        //获得对应的任务Id
        String taskId = platformReason.getTaskId();
        //设置对应的审批意见，1为同意 2为不同意
        taskService.setVariable(taskId,"ApprovalOpinion", platformReason.getOpinion());
        //判断是否流程进入endEvent,如果进入EndEvent，则不需要再再推送消息
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //判断下一节点是否哦为null  如果为null，则不获取下一节点信息
        String newTaskId = null;
        if (task != null) {
            String instanceId = task.getProcessInstanceId();
            //完成流程的审批
            taskService.complete(taskId);
            //新增消息库中下一个候选人未读信息
            newTaskId = addNextCandidateMassage(task,instanceId, platformReason.getOpinion());
        }
        //将消息中的内容删掉
        deleteCompleteMassage(taskId);
        if (newTaskId != null) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否输入了起始时间和结束时间
     * @param applyStartTime
     * @param applyEndTime
     * @return
     */
    private boolean haveApplyStartTimeAndEndTime(LocalDateTime applyStartTime,LocalDateTime applyEndTime){
        if (applyStartTime == null && applyEndTime != null) {
            throw new MyException("请输入开始时间");
        }
        if (applyStartTime != null && applyEndTime == null) {
            throw new MyException("请输入结束时间");
        }
        if (applyStartTime == null && applyEndTime == null) {
            return false;
        }
        return true;
    }

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean completeApprovalWithNoOpinion(String taskId) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("用户{}完成了任务的审批",deptUser.getUsername());
        //用户审批前拾取任务
        applyUserPickUpTask(taskId,deptUser.getUsername());
        //将消息中的内容删掉
        deleteCompleteMassage(taskId);
        //获取对应的业务流程
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //判断任务是否被拾取
        String assignee = task.getAssignee();
        if (assignee == null) {
            throw new MyException("请先拾取任务");
        }
        //获取对应的实例Id
        String instanceId = task.getProcessInstanceId();
        //完成任务流程
        taskService.complete(taskId);
        //推送对应的消息给对应的角色
        String newTaskId = addNextCandidateMassage(task,instanceId, null);
        if (newTaskId != null) {
            return true;
        }
        return false;
    }

    /**
     * 开始一个取消任务申请
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> cancelApplyTaskStart(FlatCarCancelParam param) {
        Assert.notNull(param,"取消任务参数不能为空");
        log.info("取消ID为{}的申请",param.getCancelBusinessKey());
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        //判断任务是否已经完成
        String cancelTaskId = param.getCancelTaskId();
        String taskNames = taskService.createTaskQuery().taskId(cancelTaskId).singleResult().getName();
        if ("部门评价".equals(taskNames)) {
            throw new MyException("司机已经执行完成，任务无法取消");
        }
        //开始一个流程任务
        String count = PropertiesReaderUtils.getMap().get("flatcarCancelDeploymentGroupCount");
        String instanceId = startFlatCarCancelTask(param.getCancelBusinessKey(), deptUser, count, param.getIsExecute());
        //将流程更改为取消
        Integer result = platformApplyMapper.updateApplyStateToCancel(param.getCancelBusinessKey());
        if (result == 0) {
            throw new MyException("更新申请状态失败。请联系管理员");
        }
        //挂起被取消的流程实例
        String processInstanceId = taskService.createTaskQuery().taskId(param.getCancelTaskId()).singleResult().getProcessInstanceId();
        runtimeService.suspendProcessInstanceById(processInstanceId);
        //获得对应的任务ID
        TaskQuery taskQuery = taskService.createTaskQuery();
        Task singleResult = taskQuery.processInstanceId(instanceId).singleResult();
        String taskId =  singleResult.getId();
        //完成任务的申请
        taskService.complete(taskId);
        //自动推送消息到相关部门
        String taskId1 = addNextCandidateMassage(singleResult, instanceId, null);
        //新增取消原因
        Integer createId = deptUser.getUserId();
        String reasonDescription = param.getDriverOperation();
        String reason = param.getCancelReason();
        platformReasonMapper.insert(EntityUtils.getPlatformReason(taskId1, reason, reasonDescription, createId));
        Map<String, String> map = MapUtils.getMap("taskId",taskId1,"instanceId",instanceId);
        return map;
    }

    /**
     * 完成取消任务的审批
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean completeCancelApplyTask(FlatCarCancelParam param) {
        Assert.notNull(param,"取消任务参数不能为空");
        log.info("取消ID为{}的申请",param.getCancelBusinessKey());
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        //获得当前的取消任务
        String taskId = param.getTaskId();
        //判断是否司机已经开始执行
        String execute = param.getIsExecute();
        if ("已执行".equals(execute)) {
            cancelAddDriverMassage(param, taskId);
            //删除被取消任务的人员消息
            int delete = appTasksMassageMapper.delete(new LambdaQueryWrapper<AppTasksMassage>()
                    .eq(AppTasksMassage::getTaskId, param.getCancelTaskId()));
            if(delete == 0) {
                throw new MyException("删除消息失败，请联系管理员");
            }
        } else {
            //删除旧任务消息
            int delete = appTasksMassageMapper.delete(new LambdaQueryWrapper<AppTasksMassage>()
                    .eq(AppTasksMassage::getTaskId, param.getCancelTaskId()));
            if(delete == 0) {
                throw new MyException("删除消息失败，请联系管理员");
            }
            taskService.complete(taskId);
        }
        //删除已经挂起的实例
        String instanceId = taskService.createTaskQuery().taskId(param.getCancelTaskId()).singleResult().getProcessInstanceId();
        runtimeService.deleteProcessInstance(instanceId,"任务取消");
        //解绑用户和车辆的绑定
        deptUserCarApplyMapper.delete(new LambdaQueryWrapper<DeptUserCarApply>()
        .eq(DeptUserCarApply::getBusinessKey,param.getCancelBusinessKey()));
        //通知申请人取消信息
        DeptUser username = deptUserMapper.selectApplyNameWithApplyId(param.getCancelBusinessKey());
        AppTasksMassage appTasksMassage = EntityUtils.getAppMassage(param.getCancelReason(),null , username.getUserId(),username.getUsername());
        int insert = appTasksMassageMapper.insert(appTasksMassage);
        if (insert == 0) {
            return false;
        }
        return true;
    }

    /**
     * 取消任务司机接单和执行
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void cancelApplyTaskDriverWorking(FlatCarCancelParam param) {
        Assert.notNull(param,"取消任务参数不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("取消ID为{}的申请",param.getCancelBusinessKey());
        //获得当前的取消任务
        String taskId = param.getTaskId();
        cancelAddDriverMassage(param, taskId);
    }

    /**
     * 未开始执行的平板车任务调整
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean flatcarAdjustmentWithNoStart(FlatCarAdjustmentParam param) {
        Assert.notNull(param,"调整参数不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null ) {
            throw new MyException("用户登录超时，请重新登录");
        }
        Integer businessKey = param.getAdjustmentBusinessKey();
        log.info("用户{}调整了平板车id为{}的任务",deptUser.getUsername(), businessKey);
        //添加修改原因
        String taskId = customActivitiMapper.getTaskIdByBusinessKey(businessKey);
        PlatformReason reason = EntityUtils.getPlatformReason(taskId, param.getAdjustmentReason(), param.getDriverTips(), deptUser.getUserId());
        platformReasonMapper.insert(reason);
        //判断是否目的地变动，如果是，修改
        if (param.getEndPositionId() != null) {
            platformApplyMapper.updateDestinationIdAndDestinationGpsWithApplyId(param.getEndPositionId(),param.getEndPositionGps(), businessKey);
        }
        //判断是否车辆更换，如果是，修改
        if (param.getCarId().length != 0) {
            //TODO  判断更换车辆
        }
        //判断是否司机更换，如果是，修改
        if (param.getDriverId() != null) {
            //查询所有的司机任务
            List<DeptUserCarApply> deptUserCarApplies = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                    .eq(DeptUserCarApply::getDriverId, param.getDriverId()));
            for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
                LocalDateTime startTime = deptUserCarApply.getStartTime();
                LocalDateTime endTime = deptUserCarApply.getEndTime();
                Integer key = deptUserCarApply.getBusinessKey();
                if (!key.equals(param.getAdjustmentBusinessKey()) && startTime.isAfter(param.getStartTime()) && startTime.isBefore(param.getEndTime())) {
                    throw new MyException("司机在该段时间内有其他任务，不能使用");
                }
                if (!key.equals(param.getAdjustmentBusinessKey()) && endTime.isAfter(param.getStartTime()) && endTime.isBefore(param.getEndTime())) {
                    throw new MyException("司机在该段时间内有其他任务，不能使用");
                }
            }
            //更换司机
            deptUserCarApplyMapper.updateDriverWithBusinessKey(param.getAdjustmentBusinessKey(),param.getDriverId(),param.getDriverName());
        }
        //判断是否操作员更换，如果是，修改
        if (param.getOperatorName().length != 0) {
            //获取所有跟车员的任务
            List<DeptUserCarApply> deptUserCarApplies = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                    .in(DeptUserCarApply::getOperatorId, param.getOperatorId()));
            for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
                LocalDateTime startTime = deptUserCarApply.getStartTime();
                LocalDateTime endTime = deptUserCarApply.getEndTime();
                Integer key = deptUserCarApply.getBusinessKey();
                if (!key.equals(param.getAdjustmentBusinessKey()) && startTime.isAfter(param.getStartTime()) && startTime.isBefore(param.getEndTime())) {
                    throw new MyException("操作员" + deptUserCarApply.getOperatorName() + "在该段时间内有其他任务，不能使用");
                }
                if (!key.equals(param.getAdjustmentBusinessKey()) && endTime.isAfter(param.getStartTime()) && endTime.isBefore(param.getEndTime())) {
                    throw new MyException("操作员" + deptUserCarApply.getOperatorName() + "在该段时间内有其他任务，不能使用");
                }
            }
            //获取对应的绑定Id
            List<DeptUserCarApply> userCars = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                    .eq(DeptUserCarApply::getBusinessKey, param.getAdjustmentBusinessKey()));
            //删除该业务id对应的所有绑定
            deptUserCarApplyMapper.delete(new LambdaQueryWrapper<DeptUserCarApply>()
                    .eq(DeptUserCarApply::getBusinessKey, param.getAdjustmentBusinessKey()));
            //重新新增操作员绑定
            DeptUserCarApply userCar = userCars.get(0);
            List<DeptUserCarApply> list = new LinkedList<>();
            for (int i = 0; i < param.getOperatorName().length; i++) {
                list.add(EntityUtils.getDeptUserCar(userCar.getBusinessKey(),userCar.getCarId(),userCar.getStartTime()
                ,userCar.getWorkNum(),userCar.getWorkShift(),userCar.getEndTime(),userCar.getCarPlant(),userCar.getDriverId()
                ,userCar.getDriverName(),param.getOperatorId()[i],param.getOperatorName()[i]));
            }
            deptUserCarApplyMapper.insertBatches(list);
        }
        //判断是否是开始时间调整，如果是，修改
        //todo 调整任务后是否需要将信息推送给手机端？
        if (param.getStartTime() != null) {

        }
        return null;
    }

    /**
     * 已开始执行的平板车任务调整
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean flatcarAdjustmentWithStart(FlatCarAdjustmentParam param) {
        //1、添加修改原因
        //2、判断是否目的地变动，如果是，修改
        //3、判断是否车辆更换，如果是，修改
        //4、判断是否司机和操作员更换，如果是，修改
        //5、判断是否是时间调整，如果是，修改
        return false;
    }

    private void applyUserPickUpTask(String taskId, String username){
        try {
            AppTasksMassage massage = appTasksMassageMapper.selectOne(new LambdaQueryWrapper<AppTasksMassage>()
                    .eq(AppTasksMassage::getTaskId, taskId)
                    .eq(AppTasksMassage::getUsername,username));
            //判断别人是否拾取
            if (massage == null || (massage.getIsRead() == -1)) {
                throw new MyException("任务已被拾取");
            }
            taskService.claim(taskId, username);
        } catch (RuntimeException e) {
            throw new MyException("拾取任务失败，请重试");
        }
    }

    /**
     * 任务取消接口新增司机信息
     * @param param
     * @param taskId
     */
    private void cancelAddDriverMassage(FlatCarCancelParam param, String taskId) {
        //完成审批
        taskService.complete(taskId);
        //获取对应的任务ID
        Task task = taskService.createTaskQuery().processInstanceId(param.getInstanceId()).singleResult();
        String id = task.getId();
        //获取正在执行任务的司机、操作员
        DeptUserCarApply deptUserCarApply = deptUserCarApplyMapper.selectOne(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getBusinessKey, param.getCancelBusinessKey()));
        //删除旧消息
        appTasksMassageMapper.delete(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getTaskId, param.getTaskId()));
        //自动推送消息
        String[] operateNames = deptUserCarApply.getOperatorName().split(",");
        StringBuilder sb = new StringBuilder();
        for (String operateName : operateNames) {
            sb.append(operateName).append(",");
        }
        String driverName = deptUserCarApply.getDriverName();
        sb.append(driverName);
        insertAppMassage(id, task.getName(), sb.toString());
    }

    /**
     * 新增消息库中下一个候选人未读信息
     * @param instanceId
     */
    private String addNextCandidateMassage(Task task,String instanceId, Integer opinion) {
        //反求流程定义的Key
        String definitionKey = task.getTaskDefinitionKey();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(definitionKey);
        // 输出连线
        List<SequenceFlow> outFlows = flowNode.getOutgoingFlows();
        String name = loopNextPoint(bpmnModel, outFlows, opinion);
        String candidateUsers = customActivitiMapper.getCandidateUsersInActivitiTables(name, instanceId);
        //将用户批量放在信息表中 ,获取流程ID
        Task task1= taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        String taskId = task1.getId();
        String taskName = task1.getName();
        Boolean result =insertAppMassage(taskId, taskName, candidateUsers);
        if (!result) {
            throw new MyException("插入消息失败，请联系管理员");
        }
        return taskId;
    }

    /**
     * 删除对应的消息里面的数据
     * @param taskId
     */
    private void deleteCompleteMassage(String taskId) {
        int delete = appTasksMassageMapper.delete(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getTaskId, taskId));
        if (delete == 0) {
            throw new MyException("删除消息失败，请联系管理员");
        }
    }

    /**
     * 开启一个取消任务流程
     * @param businessKey
     * @param deptUser
     * @param count
     * @param approvalOpinion
     * @return
     */
    private String startFlatCarCancelTask(String businessKey, DeptUser deptUser, String count, String approvalOpinion) {
        //表示已经到执行环节了
        Map<String, Object> variables = new HashMap<>(Integer.parseInt(count) + 2);
        variables.put("startName",deptUser.getUsername());
        variables.put("ApprovalOpinion",approvalOpinion);
        //动态的向流程中添加审批角色属性
        List<String> roleIdsList = new ArrayList<>(Integer.parseInt(count));
        for (int i = 1 ; i <= Integer.parseInt(count) ; i++ ) {
            String roleIds = PropertiesReaderUtils.getMap().get("flatcarCancelGroupTask" + i);
            roleIdsList.add(roleIds);
        }
        List<DeptUser> usersList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUserRoleId, roleIdsList));
        for (int i = 1 ; i <= Integer.parseInt(count) ; i++) {
            String roleId = PropertiesReaderUtils.getMap().get("flatcarCancelGroupTask" + i);
            List<DeptUser> deptUsers = new ArrayList<>(usersList.size());
            for (DeptUser deptUser1 : usersList) {
                String userRoleId = String.valueOf(deptUser1.getUserRoleId());
                if (roleId.equals(userRoleId)) {
                    deptUsers.add(deptUser1);
                }
            }
            //获取角色用户的名字
            StringBuilder builder = new StringBuilder();
            for (DeptUser user : deptUsers ) {
                builder.append(user.getUsername()).append(",");
            }
            String substring = builder.substring(0, builder.length() - 1);
            variables.put("flatcarCancelGroupTask" + i, substring);
        }
        //开启一个取消任务
        ProcessInstance instance;
        try {
            instance = runtimeService.startProcessInstanceByKey(PropertiesReaderUtils.getMap().get("flatcarCancelDeploymentId"), businessKey, variables);
        } catch (RuntimeException e) {
            throw new MyException("新增取消任务失败，请联系管理员");
        }
        //返回执行状态
        return instance.getId();
    }

    /**
     * 递归调用 找到下一个节点里面的name
     * @param bpmnModel
     * @param outFlows
     * @param opinion
     * @return
     */
    private String loopNextPoint(BpmnModel bpmnModel, List<SequenceFlow> outFlows, Integer opinion) {
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
                if (expression.contains(String.valueOf(opinion))) {
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
     * 递归调用 找到下一个节点里面的name
     * @param candidateUser
     * @return
     */
    private Boolean insertAppMassage(String taskId, String taskName, String candidateUser) {
        String[] candidateUse = candidateUser.split(",");
        List<AppTasksMassage> massages = new ArrayList<>(candidateUse.length);
        //数组转列表
        List<String> asList = Arrays.asList(candidateUse);
        //批量查询对应的人员
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUsername, asList));
        for (DeptUser deptUser : userList) {
            massages.add(EntityUtils.getAppMassage(taskName + "任务", Integer.parseInt(taskId), deptUser.getUserId(),deptUser.getUsername()));
        }
        return appTasksMassageMapper.insertBatches(massages);
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
        }
        businessKey = processInstance.getBusinessKey();
        return businessKey;
    }
}