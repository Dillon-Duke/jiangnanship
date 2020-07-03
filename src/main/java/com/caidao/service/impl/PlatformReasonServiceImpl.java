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
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
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
    private PlatformApplyMapper platformApplyMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private AppMassageMapper appMassageMapper;

    @Autowired
    private DeptUserCarMapper deptUserCarMapper;

    @Autowired
    private CarPlatformApplyMapper carPlatformApplyMapper;

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String completeApprovalWithOpinion(PlatformReason platformReason ) {
        Assert.notNull(platformReason,"审批原因不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("用户{}完成了任务的审批",deptUser.getUsername());

        //1、驳回时将审批意见存入数据库
        if (platformReason.getOpinion() == 0) {
            platformReason.setCreateId(deptUser.getCreateId());
            platformReasonMapper.insert(platformReason);
        }
        //2、获得对应的任务Id
        String taskId = platformReason.getTaskId();

        //3、设置对应的审批意见，1为同意 2为不同意
        taskService.setVariable(taskId,"ApprovalOpinion", platformReason.getOpinion());

        //4、判断是否流程进入endEvent,如果进入EndEvent，则不需要再再推送消息
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //判断下一节点是否哦为null  如果为null，则不获取下一节点信息
        String newTaskId = null;
        if (task != null) {
            String instanceId = task.getProcessInstanceId();
            //5、完成流程的审批
            taskService.complete(taskId);
            //6、新增消息库中下一个候选人未读信息
            newTaskId = addNextCandidateMassage(task,instanceId, platformReason.getOpinion());
        }

        //7、将消息中的内容删掉
        deleteCompleteMassage(taskId);
        return newTaskId;
    }

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String completeApprovalWithNoOpinion(String taskId) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("用户{}完成了任务的审批",deptUser.getUsername());

        //1、将消息中的内容删掉
        deleteCompleteMassage(taskId);

        //2、获取对应的业务流程
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //3、判断任务是否被拾取
        String assignee = task.getAssignee();
        if (assignee == null) {
            throw new MyException("请先拾取任务");
        }

        //4、获取对应的实例Id
        String instanceId = task.getProcessInstanceId();

        //5、完成任务流程
        taskService.complete(taskId);

        //6、推送对应的消息给对应的角色
        String newTaskId = addNextCandidateMassage(task,instanceId, null);
        return newTaskId;
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
        Integer result = platformApplyMapper.updateApplyState(param.getCancelBusinessKey());
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
            int delete = appMassageMapper.delete(new LambdaQueryWrapper<AppMassage>()
                    .eq(AppMassage::getTaskId, param.getCancelTaskId()));
            if(delete == 0) {
                throw new MyException("删除消息失败，请联系管理员");
            }
        } else {
            //删除旧任务消息
            int delete = appMassageMapper.delete(new LambdaQueryWrapper<AppMassage>()
                    .eq(AppMassage::getTaskId, param.getCancelTaskId()));
            if(delete == 0) {
                throw new MyException("删除消息失败，请联系管理员");
            }
            taskService.complete(taskId);
        }
        //删除已经挂起的实例
        String instanceId = taskService.createTaskQuery().taskId(param.getCancelTaskId()).singleResult().getProcessInstanceId();
        runtimeService.deleteProcessInstance(instanceId,"任务取消");
        //解绑车辆与平板车申请的绑定
        String[] cancelCarIds = param.getCancelCarIds();
        carPlatformApplyMapper.deleteBatchCarIdAndPrsId(param.getCancelBusinessKey(),cancelCarIds);
        //解绑用户和车辆的绑定
        deptUserCarMapper.delete(new LambdaQueryWrapper<DeptUserCar>()
        .eq(DeptUserCar::getBusinessKey,param.getCancelBusinessKey()));
        //通知申请人取消信息
        String username = deptUserMapper.selectApplyName(param.getCancelBusinessKey());
        AppMassage appMassage = EntityUtils.getAppMassage(param.getCancelReason(),null , username);
        int insert = appMassageMapper.insert(appMassage);
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
        String taskId = platformReasonMapper.getTaskIdByBusinessKey(businessKey);
        PlatformReason reason = EntityUtils.getPlatformReason(taskId, param.getAdjustmentReason(), param.getDriverTips(), deptUser.getUserId());
        platformReasonMapper.insert(reason);
        //判断是否目的地变动，如果是，修改
        if (param.getEndPositionId() != null) {
            platformReasonMapper.updateDestination(param.getEndPositionId(),param.getEndPositionGps(), businessKey);
        }
        //判断是否车辆更换，如果是，修改
        if (param.getCarId().length != 0) {
            //查询更换车辆的所有任务
            List<CarPlatformApply> list = carPlatformApplyMapper.selectBatchCarIds(param.getCarId());
            for (CarPlatformApply carPlatformApply : list) {
                Integer prsId = carPlatformApply.getPrsId();
                LocalDateTime startTime = carPlatformApply.getStartTime();
                LocalDateTime endTime = carPlatformApply.getEndTime();
                if (!prsId.equals(businessKey) && startTime.isAfter(param.getStartTime()) && startTime.isBefore(param.getEndTime())) {
                    throw new MyException("车辆在该段时间内有其他任务，不能使用");
                }
                if (!prsId.equals(businessKey) && endTime.isAfter(param.getStartTime()) && endTime.isBefore(param.getEndTime())) {
                    throw new MyException("车辆在该段时间内有其他任务，不能使用");
                }
            }
            //删除之前任务绑定的车辆
            carPlatformApplyMapper.delete(new LambdaQueryWrapper<CarPlatformApply>()
            .eq(CarPlatformApply::getPrsId, businessKey));
            //添加该任务绑定的车辆
            List<CarPlatformApply> applies = new ArrayList<>(param.getCarId().length);
            for (String carId : param.getCarId()) {
                applies.add(EntityUtils.getCarPlatformApply(deptUser.getUserId(),Integer.parseInt(carId),param.getStartTime(),param.getEndTime(), businessKey));
            }
            carPlatformApplyMapper.insertBatches(applies);
        }
        //判断是否司机更换，如果是，修改
        if (param.getDriverId() != null) {
            //查询所有的司机任务
            List<DeptUserCar> deptUserCars= deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                    .eq(DeptUserCar::getDriverId, param.getDriverId()));
            for (DeptUserCar deptUserCar : deptUserCars) {
                LocalDateTime startTime = deptUserCar.getStartTime();
                LocalDateTime endTime = deptUserCar.getEndTime();
                Integer key = deptUserCar.getBusinessKey();
                if (!key.equals(param.getAdjustmentBusinessKey()) && startTime.isAfter(param.getStartTime()) && startTime.isBefore(param.getEndTime())) {
                    throw new MyException("司机在该段时间内有其他任务，不能使用");
                }
                if (!key.equals(param.getAdjustmentBusinessKey()) && endTime.isAfter(param.getStartTime()) && endTime.isBefore(param.getEndTime())) {
                    throw new MyException("司机在该段时间内有其他任务，不能使用");
                }
            }
            //更换司机
            deptUserCarMapper.updateDriver(param.getAdjustmentBusinessKey(),param.getDriverId(),param.getDriverName());
        }
        //判断是否操作员更换，如果是，修改
        if (param.getOperatorName().length != 0) {
            //获取所有跟车员的任务
            List<DeptUserCar> deptUserCars = deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                    .in(DeptUserCar::getOperatorId, param.getOperatorId()));
            for (DeptUserCar deptUserCar : deptUserCars) {
                LocalDateTime startTime = deptUserCar.getStartTime();
                LocalDateTime endTime = deptUserCar.getEndTime();
                Integer key = deptUserCar.getBusinessKey();
                if (!key.equals(param.getAdjustmentBusinessKey()) && startTime.isAfter(param.getStartTime()) && startTime.isBefore(param.getEndTime())) {
                    throw new MyException("操作员" + deptUserCar.getOperatorName() + "在该段时间内有其他任务，不能使用");
                }
                if (!key.equals(param.getAdjustmentBusinessKey()) && endTime.isAfter(param.getStartTime()) && endTime.isBefore(param.getEndTime())) {
                    throw new MyException("操作员" + deptUserCar.getOperatorName() + "在该段时间内有其他任务，不能使用");
                }
            }
            //获取对应的绑定Id
            List<DeptUserCar> userCars = deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                    .eq(DeptUserCar::getBusinessKey, param.getAdjustmentBusinessKey()));
            //删除该业务id对应的所有绑定
            deptUserCarMapper.delete(new LambdaQueryWrapper<DeptUserCar>()
                    .eq(DeptUserCar::getBusinessKey, param.getAdjustmentBusinessKey()));
            //重新新增操作员绑定
            DeptUserCar userCar = userCars.get(0);
            List<DeptUserCar> list = new LinkedList<>();
            for (int i = 0; i < param.getOperatorName().length; i++) {
                list.add(EntityUtils.getDeptUserCar(userCar.getBusinessKey(),userCar.getCarId(),userCar.getStartTime()
                ,userCar.getWorkNum(),userCar.getWorkShift(),userCar.getEndTime(),userCar.getCarPlant(),userCar.getDriverId()
                ,userCar.getDriverName(),param.getOperatorId()[i],param.getOperatorName()[i]));
            }
            deptUserCarMapper.insertBatches(list);
        }
        //判断是否是开始时间调整，如果是，修改
        //todo
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
        DeptUserCar deptUserCar = deptUserCarMapper.selectOne(new LambdaQueryWrapper<DeptUserCar>()
                .eq(DeptUserCar::getBusinessKey, param.getCancelBusinessKey()));
        //删除旧消息
        appMassageMapper.delete(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getTaskId, param.getTaskId()));
        //自动推送消息
        String[] operateNames = deptUserCar.getOperatorName().split(",");
        StringBuilder sb = new StringBuilder();
        for (String operateName : operateNames) {
            sb.append(operateName).append(",");
        }
        String driverName = deptUserCar.getDriverName();
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
        String candidateUsers = platformApplyMapper.getCandidate(name, instanceId);
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
        int delete = appMassageMapper.delete(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getTaskId, taskId));
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
        List<AppMassage> massages = new ArrayList<>(candidateUse.length);
        for (String string : candidateUse) {
            massages.add(EntityUtils.getAppMassage(taskName + "任务", Integer.parseInt(taskId), string));
        }
        return appMassageMapper.insertBatches(massages);
    }
}