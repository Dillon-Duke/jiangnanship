package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.*;
import com.caidao.param.ActivityQueryParam;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.*;
import com.caidao.service.CarService;
import com.caidao.service.PlatformApplyService;
import com.caidao.util.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Dillon
 * @since 2020-06-11
 */
@Service
@Slf4j
public class PlatformApplyApplyServiceImpl extends ServiceImpl<PlatformApplyMapper, PlatformApply> implements PlatformApplyService {

    private static final int FIXED_BUSINESS_KEY_LENGTH = 6;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CarService carService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private PlatformGoodsMapper platformGoodsMapper;

    @Autowired
    private PlatformApplyMapper platformApplyMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private AppTasksMassageMapper appTasksMassageMapper;

    @Autowired
    private DeptUserCarApplyMapper deptUserCarApplyMapper;

    @Autowired
    private CustomActivitiMapper customActivitiMapper;

    /**
     * 保存一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> saveFlatCarPlan(PlatformApply platformApply) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null){
            throw new MyException("用户未登录，请登录");
        }
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());
        platformApply.setCreateId(deptUser.getUserId());
        platformApply.setCreateDate(LocalDateTime.now());
        platformApply.setApplyName(deptUser.getUsername());
        platformApply.setApplyState(0);
        platformApply.setState(1);
        //将申请任务保存
        int insert = platformApplyMapper.insert(platformApply);
        if (insert != 1) {
            throw new MyException("平板车计划任务插入失败");
        }
        //获取插入数据的ID
        Integer id = platformApply.getPrsId();
        //生产申请工单号
        String businessKey = String.valueOf(id);
        String sort = null;
        if (FIXED_BUSINESS_KEY_LENGTH - businessKey.length() >0){
            sort = String.format("%06d",id);
        }else {
            sort = businessKey.substring(businessKey.length()-6);
        }
        String applicationNum;
        //保存一个驳运流程并且获取驳运对象的对象名字
        String goodsName;
        String flatcarName;
        switch (platformApply.getRequestType()) {
            case 1:
                //插入一条平板车计划任务的申请
                String flatcarPlanPrefix = "flatcarPlan";
                flatcarName = "计划";
                applicationNum = PropertyUtils.FLAT_CAR_PLAN_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName, flatcarPlanPrefix, platformApply, businessKey);
                break;
            case 2:
                //插入一条平板车临时任务的申请 ，获取插入的实例
                String flatcarTempPrefix = "flatcarTemp";
                flatcarName = "临时";
                applicationNum = PropertyUtils.FLAT_CAR_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarTempPrefix,platformApply, businessKey);
                break;
            case 3:
                //插入一条平板车取消任务的申请 ，获取插入的实例
                String flatcarCancelPrefix = "flatcarFast";
                flatcarName = "快速";
                applicationNum = PropertyUtils.FLAT_CAR_CANCEL_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarCancelPrefix,platformApply, businessKey);
                break;
            case 4:
                //插入一条平板车衍生任务的申请 ，获取插入的实例
                String flatcarOtherTempPrefix = "flatcarOtherTemp";
                flatcarName = "衍生";
                applicationNum = PropertyUtils.FLAT_CAR_OTHER_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarOtherTempPrefix,platformApply, businessKey);
                break;
            default :
                throw new MyException("申请类型不正确");
        }
        String requestName = goodsName + " " + platformApply.getJobContent() + "(" + platformApply.getRequestType() + ")";
        platformApply.setRequestName(requestName);
        platformApply.setRequestOddNumber(applicationNum);
        Integer update = platformApplyMapper.updateById(platformApply);
        if (update != 1) {
            throw new MyException("保存平板车任务失败");
        }
        //返回值放到map中
        String returnMessage = "平板车" + flatcarName + "任务保存成功";
        return MapUtils.getMap("requestName",requestName,"returnMessage",returnMessage,"applicationNum",applicationNum,"businessKey",businessKey);
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param platformReason
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean removePlanById(PlatformReason platformReason) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}删除未申请的任务",deptUser.getUsername());
        String id = platformReason.getTaskId();
        String reason = platformReason.getReason();
        PlatformApply platformApply = platformApplyMapper.selectById(id);
        //获取任务的申请状态，已提交状态的任务不能被删除
        Integer state = platformApply.getApplyState();
        if(state != 0) {
            throw new MyException("任务申请已提交，不能删除");
        }
        //删除未提交的任务
        int delete = platformApplyMapper.deleteById(id);
        if(delete <= 0) {
            throw new MyException("删除操作失败，请重试");
        }
        //更改分段绑定信息状态
        int update = platformGoodsMapper.updateGoodsBindStateWithGoodsId(platformApply.getObjectId());
        if(update <= 0) {
            throw new MyException("删除操作失败，请重试");
        }
        //删除流程中的开始实例
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        String ids = instanceQuery.processInstanceBusinessKey(id).singleResult().getId();
        try {
            runtimeService.deleteProcessInstance(ids,reason);
        } catch (RuntimeException e) {
            throw new MyException("删除操作失败，请联系管理员");
        }
        return true;
    }

    /**
     * 开始一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> startPlanTasks(PlatformApply platformApply) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null ){
            throw new MyException("登录已超时，请重新登录");
        }
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());
        //判断提交的是不是之前保存过的申请 未保存，应该先保存，
        Integer prsId = platformApply.getPrsId();
        Map<String, String> withAutoSaveFlatCarPlanMap = null;
        if (prsId == null || prsId == 0){
            withAutoSaveFlatCarPlanMap = withAutoSaveFlatCarPlan(platformApply);
        }
        //更改审批状态，未审批变为审批中
        platformApply.setRequestOddNumber(withAutoSaveFlatCarPlanMap.get("applicationNum"));
        platformApply.setRequestName(withAutoSaveFlatCarPlanMap.get("requestName"));
        platformApply.setCreateId(deptUser.getUserId());
        platformApply.setCreateDate(LocalDateTime.now());
        platformApply.setState(1);
        platformApply.setUpdateId(deptUser.getUserId());
        platformApply.setUpdateDate(LocalDateTime.now());
        platformApply.setApplyState(1);
        platformApplyMapper.updateById(platformApply);
        //获取任务ID
        Object businessKeys = withAutoSaveFlatCarPlanMap.get("businessKey");
        TaskQuery taskQuery = taskService.createTaskQuery();
        Task task = taskQuery.processInstanceBusinessKey(String.valueOf(businessKeys)).singleResult();
        if (task == null){
            throw new MyException("任务申请失败，请联系管理员确认");
        }
        String taskId = task.getId();
        //完成任务审批
        try {
            //完成前获得任务实例Id
            String instanceId = task.getProcessInstanceId();
            //完成任务
            taskService.complete(taskId);
            addNextCandidateMassage(task,instanceId, null);
            String requestName = withAutoSaveFlatCarPlanMap.get("requestName");
            String returnMessage = "平板车" + withAutoSaveFlatCarPlanMap.get("flatcarName") + "任务申请成功";
            String applicationNum = withAutoSaveFlatCarPlanMap.get("applicationNum");
            String businessKey = withAutoSaveFlatCarPlanMap.get("BusinessKey");
            return MapUtils.getMap("requestName",requestName,"returnMessage",returnMessage,"applicationNum",applicationNum,"businessKey",businessKey);
        } catch (RuntimeException e) {
            throw new MyException("任务申请失败，请联系管理员确认");
        }
    }

    /**
     * 获取用户的历史任务
     * @param param
     * @return
     */
    @Override
    public List<HistoricTaskInstance> getUserHistoryTaskList(ActivityQueryParam param) {
        Assert.notNull(param,"参数不能为空");
        log.info("查询用户名为{}的历史列表",param.getUserName());
        //默认查询当前用户的所有历史任务
        String username;
        if (param.getUserName() != null && param.getUserName() !=""){
            username = param.getUserName();
        } else {
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        }
        log.info("用户{}查询历史记录",username);
        //获取所有的个人历史任务
        HistoricTaskInstanceQuery instanceQuery = historyService.createHistoricTaskInstanceQuery();
        List<HistoricTaskInstance> list = instanceQuery.taskAssignee(username)
                .taskName(StringUtils.hasText(param.getTaskName()) ? param.getTaskName() : null)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        //判断日期条件是否建立 不建立 则为null
        String startDate = StringUtils.hasText(param.getHistoryStartTime()) ? param.getHistoryStartTime() : null;
        String endDate = StringUtils.hasText(param.getHistoryEndTime()) ? param.getHistoryEndTime() : null;
        //判断结束日期如果没有条件则位当前时间
        Date endTime;
        if (endDate == null){
            endTime = DateUtils.localDateTime2Date(LocalDateTime.now());
        } else {
            endTime = DateUtils.string2Date(endDate);
        }
        //判断当前时间是否为空，为空则不作为条件
        List<HistoricTaskInstance> list1 = new ArrayList<>();
        if (startDate == null){
            for (HistoricTaskInstance historicTaskInstance : list) {
                Date endTime1 = historicTaskInstance.getEndTime();
                if (endTime1 != null && endTime.after(endTime1)){
                    list1.add(historicTaskInstance);
                }
            }
        } else {
            Date startTime = DateUtils.string2Date(startDate);
            for (HistoricTaskInstance historicTaskInstance : list) {
                Date endTime1 = historicTaskInstance.getEndTime();
                Date startTime1 = historicTaskInstance.getStartTime();
                if (endTime1 != null && endTime.after(endTime1) && startTime.before(startTime1)){
                    list1.add(historicTaskInstance);
                }
            }
        }
        return list1;
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String getPlanOwnerGroupTask(String taskId) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        String username = deptUser.getUsername();
        Integer updateId = deptUser.getUserId();
        log.info("拾取用户名为{}的组任务列表", username);
        try {
            AppTasksMassage massage = appTasksMassageMapper.selectOne(new LambdaQueryWrapper<AppTasksMassage>()
                    .eq(AppTasksMassage::getTaskId, taskId)
                    .eq(AppTasksMassage::getDeptUsername,username));
            //判断别人是否拾取
            if (massage == null || (massage.getIsRead() == -1)) {
                throw new MyException("任务已被拾取");
            }
            taskService.claim(taskId, username);
            //更新该任务id的消息
            Integer massages = appTasksMassageMapper.updateReadStateToReadied(taskId);
            if (massages == 0) {
                throw new MyException("任务拾取失败，请联系管理员");
            }
            //获取流程的业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            //更新数据库中审批人
            Integer update = platformApplyMapper.updateTaskApprovalName(Integer.parseInt(businessKey), username,updateId);
            if (update <= 0){
                throw new MyException("任务拾取失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("拾取任务失败，请重试");
        }
        return "任务拾取成功";
    }

    /**
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void flatCarPlan2OtherUser(String taskId, String username) {
        Assert.notNull(taskId,"任务ID不能为空");
        Assert.notNull(username,"用户名称不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("任务id为{}交给{}",taskId,username);
        //用户转交之前查询
        AppTasksMassage massage = appTasksMassageMapper.selectOne(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getTaskId, taskId));
        if (massage == null ) {
            throw new MyException("任务已被拾取");
        }
        massage.setMassageName(username);
        massage.setIsRead(1);
        int update1 = appTasksMassageMapper.updateById(massage);
        if (update1 == 0) {
            throw new MyException("任务转交失败，请联系管理员");
        }
        try {
            taskService.setOwner(taskId, deptUser.getUsername());
            taskService.setAssignee(taskId,username);
            //获取流程的业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            //增加数据库中审批人
            Integer update = platformApplyMapper.updateTaskApprovalName(Integer.parseInt(businessKey),username,deptUser.getUserId());
            if (update <= 0){
                throw new MyException("任务指派失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("任务指派失败，请重试");
        }
    }

    /**
     * 用户归还组任务
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void backPlanOwner2GroupTask(String taskId) {
        Assert.notNull(taskId,"任务ID不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Integer updateId = deptUser.getUserId();
        log.info("拾取用户名为{}的组任务列表",deptUser.getUsername());
        //用户归还任务 更新信息状态为未阅读
        Integer massage = appTasksMassageMapper.backTasksWithReadStateToUnRead(taskId);
        if (massage == 0 ) {
            throw new MyException("任务归还失败，请联系管理员");
        }
        try {
            taskService.setAssignee(taskId,null);
            //获取流程的业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            //增加数据库中审批人
            Integer update = platformApplyMapper.updateTaskApprovalName(Integer.parseInt(businessKey),null,updateId);
            if (update <= 0){
                throw new MyException("任务指派失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("任务指派失败，请重试");
        }
    }

    /**
     * 获取用户的所有任务列表
     * @param username
     * @param taskState
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<Map<String, Object>> getApprovalList(String username, String taskState) {
        //默认查询的当前用户的所有审批的列表
        Assert.notNull(username,"参数不能为空");
        //判断是查询自己的正在审批的任务还是别人正在审批的任务
        String usernames;
        if (username == null || username == ""){
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            usernames = deptUser.getUsername();
        } else {
            usernames = username;
        }
        log.info("查询用户名为{}的任务列表",username);
        //获取所有满足条件的任务列表
        List<PlatformApply> submitList = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .eq(StringUtils.hasText(taskState), PlatformApply::getApplyState, taskState)
                .eq(PlatformApply::getApplyName, usernames)
                .orderByAsc(PlatformApply::getObjectId));
        //判断如果没有任何任务，直接返回空值
        if(submitList.size() == 0){
            return null;
        }
        //获取所有的TranGoods的id 放到新的list中
        List<Integer> tranGoodsIds = new ArrayList<>();
        for (PlatformApply requestSubmit : submitList) {
            tranGoodsIds.add(requestSubmit.getObjectId());
        }
        //获取所有的物件信息
        List<PlatformGoods> platformGoods = platformGoodsMapper.selectList(new LambdaQueryWrapper<PlatformGoods>()
                .in(PlatformGoods::getGoodsId, tranGoodsIds)
                .orderByAsc(PlatformGoods::getGoodsId));
        //封装返回的内容
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < submitList.size(); i++) {
            PlatformApply requestSubmit = submitList.get(i);
            Integer requestId = requestSubmit.getPrsId();
            String requestName = platformGoods.get(i).getGoodsType() + " " + requestSubmit.getJobContent() + "(" + requestSubmit.getRequestType() + ")";
            Integer status = requestSubmit.getApplyState();
            Long startTime = requestSubmit.getStartTime();
            Long endTime = requestSubmit.getEndTime();
            String object = platformGoods.get(i).getGoodsType();
            list.add(MapUtils.getMap("requestID",requestId,"requestName",requestName,"status",status,"startTime",startTime,"endTime",endTime,"object",object));
        }
        return list;
    }

    /**
     * 获得可以编制的任务
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public List<PlatformApply> getPlatformOrganizationTasks() {
        TaskQuery query = taskService.createTaskQuery();
        //获取编制驳动计划的所有任务列表
        List<Task> tasks = query.taskName("编制驳动计划").orderByTaskCreateTime().desc().list();
       if (tasks.size() == 0){
            return null;
       }
       //获取实例ID列表
        List<String> arrayList = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            arrayList.add(task.getProcessInstanceId());
        }
        String string = arrayList.toString();
        String substring = string.substring(1, string.length() - 1);
        //自定义查询对应BusinessKeys
        NativeProcessInstanceQuery instanceQuery = runtimeService.createNativeProcessInstanceQuery();
        List<ProcessInstance> instanceList = instanceQuery.sql("SELECT * FROM " + managementService.getTableName(Execution.class) + " T " + "WHERE T.BUSINESS_KEY_ IS NOT NULL AND T.PROC_INST_ID_ IN (#{substring})").parameter("substring", substring).list();

        //查询对应的业务信息
        List<Integer> integers = new ArrayList<>(instanceList.size());
        for (ProcessInstance instance : instanceList) {
            integers.add(Integer.parseInt(instance.getBusinessKey()));
        }
        //获取对应的列表
        List<PlatformApply> platformApplyList = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .in(PlatformApply::getPrsId, integers)
                .orderByDesc(PlatformApply::getPrsId));
        return platformApplyList;
    }

    /**
     * 查询用户是否有取消权限
     * @param businessKey
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> startCancelApplyTask(Integer businessKey) {
        Assert.notNull(businessKey,"业务主键不能为空");
        log.info("查询业务主键为{}的所有可执行删除或修改的用户");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        //获取对应的实例查询serviceNames
        TaskQuery taskQuery = taskService.createTaskQuery();
        Task singleResult = taskQuery.processInstanceBusinessKey(String.valueOf(businessKey)).singleResult();
        //判断任务执行是否完成
        String singleResultName = singleResult.getName();
        if("执行完成".equals(singleResultName) || "部门评价".equals(singleResultName)) {
            throw new MyException("任务已完成，无法取消");
        }
        String instanceId = singleResult.getProcessInstanceId();
        //查询所有的可以取消的审批人 包括司机
        List<String> nameList = customActivitiMapper.getPlatCarApplyAndApprovalUsers(instanceId);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nameList.size(); i++) {
            String names = nameList.get(i);
            String[] namesList = names.split(",");
            for (String name : namesList) {
                if(!list.contains(name)) {
                    list.add(name);
                }
            }
        }
        //去除姓名中司机和跟车员名称
        Map<String, String> map = new HashMap<>(2);
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUsername, list));
        for (DeptUser deptuser : userList) {
            if (deptuser.getUserRoleName() == null) {
                map.put("用户权限","该用户无取消权限");
                break;
            }
            if (deptuser.getUserRoleName().contains("司机")) {
                list.remove(deptuser.getUsername());
            }
            if (!list.contains(deptUser.getUsername())) {
                map.put("用户权限","该用户无取消权限");
                break;
            }
        }
        //获取流程现在的状态
        Task task = taskQuery.processInstanceId(instanceId).singleResult();
        String taskName = task.getName();
        //获取流程ID
        String platCarProcessIsOrNotExecute = PropertyUtils.PLAT_CAR_PROCESS_IS_OR_NOT_EXECUTE;
        if (platCarProcessIsOrNotExecute.contains(taskName)) {
            map.put("执行状态","已执行");
        } else {
            map.put("执行状态","未执行");
        }
        return map;
    }

    /**
     * 部门评价人员进行评价
     * @param taskId
     * @return
     */
    @Override
    public boolean departmentEvaluate(String taskId) {
        Assert.notNull(taskId,"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("部门评价人员{}完成评价",deptuser.getUsername());

        //1、新增部门评价的信息
        //todo 之后确认评价信息表字段之后更新

        //2、工作流程的完成
        taskService.complete(taskId);

        //3、更改申请流程状态为完成
        //获取对应的业务Id
        String businessKey = toTaskIdGetBusinessKey(taskId);
        Integer integer = platformApplyMapper.updatePlatformApplyStateToSuccess(Integer.parseInt(businessKey));

        //4、删除推送消息中信息
        deleteCompleteMassage(taskId);

        if (integer == 0) {
            return false;
        }
        return true;
    }

    /**
     * 司机完成任务的执行
     * @param taskId
     * @return
     */
    @Override
    public boolean driverCompleteTask(String taskId) {
        Assert.notNull(taskId,"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("司机{}完成任务的执行",deptuser.getUsername());
        //获取对应的业务Id
        String businessKey = toTaskIdGetBusinessKey(taskId);
        //解除司机与车辆的绑定
        deptUserCarApplyMapper.delete(new LambdaQueryWrapper<DeptUserCarApply>()
        .eq(DeptUserCarApply::getBusinessKey,businessKey));
        //解绑驳运完成的物件信息
        platformApplyMapper.updateGoodsBindStateToUnbind(businessKey);
        //删除推送消息中信息
        deleteCompleteMassage(taskId);
        //给部门评价人员推送消息
        //获得对应的实例Id
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String instanceId = task.getProcessInstanceId();
        //司机完成任务执行
        taskService.complete(taskId);
        String newTaskId = addNextCandidateMassage(task,instanceId, null);
        if (newTaskId != null) {
            return true;
        }
        return false;
    }

    /**
     * 取消任务司机完成任务的执行
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public String flatcarCancelDriverCompleteTask(FlatCarCancelParam param) {

        Assert.notNull(param.getTaskId(),"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("司机{}完成任务的执行",deptuser.getUsername());
        String cancelBusinessKey = param.getCancelBusinessKey();
        String taskId = param.getTaskId();
        //1、解除司机与车辆的绑定
        deptUserCarApplyMapper.delete(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getBusinessKey, cancelBusinessKey));

        //2、解绑驳运完成的物件信息
        //获取对应的业务Id
        String businessKey = toTaskIdGetBusinessKey(taskId);
        //更新对应的物件信息为未绑定
        platformApplyMapper.updateGoodsBindStateToUnbind(businessKey);

        //3、获得对应的实例Id
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //4、司机完成任务执行
        taskService.complete(taskId);

        //5、删除推送消息中信息
        deleteCompleteMassage(taskId);

        //6、给部门评价人员推送消息
        String instanceId = task.getProcessInstanceId();
        String newTaskId = addNextCandidateMassage(task,instanceId, null);
        return newTaskId;
    }

    /**
     * 取消任务部门评价人员进行评价
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean flatcarCancelDepartmentEvaluate(FlatCarCancelParam param) {
        Assert.notNull(param.getTaskId(),"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("部门评价人员{}完成评价",deptuser.getUsername());
        String taskId = param.getTaskId();
        //1、新增部门评价的信息
        //todo 之后确认评价信息表字段之后更新

        //2、工作流程的完成
        taskService.complete(taskId);

        //3、删除推送消息中信息
        deleteCompleteMassage(taskId);

        //4、更新平板车申请人里面审批人未null
        Integer integer = platformApplyMapper.updateApplyNameAsNull(param.getCancelBusinessKey());
        if (integer == 0) {
            return false;
        }
        return true;
    }

    /**
     * 通过申请Id获取详细的申请信息
     * @param businessKey
     * @return
     */
    @Override
    public Map<String, Object> getApplyDetailInfoByApplyId(Integer businessKey) {
        //TODO 之后看一下可以前端直接带参数过来，后台只需要查询必要的参数即可
        //获取所有的分段信息，作为之后地图展示坐标使用
        List<PlatformGoods> goods = platformGoodsMapper.selectList(null);
        //获取申请单详情
        PlatformApply platformApply = platformApplyMapper.selectById(businessKey);
        //获取申请单绑定的车辆以及时间信息
        DeptUserCarApply userCarApply = deptUserCarApplyMapper.selectOne(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getBusinessKey, businessKey));
        //获取对应的车辆信息
        Car car = carService.getById(userCarApply.getCarId());
        return MapUtils.getMap("goodsMassage",goods,"platformApplyDetails",platformApply,"bindTimeMassage",userCarApply,"car",car);
    }

    /**
     * 直接提交一个工作申请之前的保存
     * @param platformApply
     * @return
     */
    private Map<String, String> withAutoSaveFlatCarPlan(PlatformApply platformApply) {
        //将申请任务保存
        int insert = platformApplyMapper.insert(platformApply);
        if (insert != 1) {
            throw new MyException("平板车计划任务插入失败");
        }
        //获取插入数据的ID
        Integer id = platformApply.getPrsId();
        //生产申请工单号
        String sort;
        String businessKey = String.valueOf(id);
        if (FIXED_BUSINESS_KEY_LENGTH - businessKey.length() >0){
            sort = String.format("%06d",id);
        }else {
            sort = businessKey.substring(businessKey.length()-6);
        }
        String applicationNum;
        //保存一个驳运流程并且获取驳运对象的对象名字
        String goodsName;
        String flatcarName;
        switch (platformApply.getRequestType()) {
            case 1:
                //插入一条平板车计划任务的申请
                String flatcarPlanPrefix = "flatcarPlan";
                flatcarName = "计划";
                applicationNum = PropertyUtils.FLAT_CAR_PLAN_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName, flatcarPlanPrefix, platformApply, businessKey);
                break;
            case 2:
                //插入一条平板车临时任务的申请 ，获取插入的实例
                String flatcarTempPrefix = "flatcarTemp";
                flatcarName = "临时";
                applicationNum = PropertyUtils.FLAT_CAR_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarTempPrefix,platformApply, businessKey);
                break;
            case 3:
                //插入一条平板车取消任务的申请 ，获取插入的实例
                String flatcarCancelPrefix = "flatcarFast";
                flatcarName = "快速";
                applicationNum = PropertyUtils.FLAT_CAR_CANCEL_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarCancelPrefix,platformApply, businessKey);
                break;
            case 4:
                //插入一条平板车衍生任务的申请 ，获取插入的实例
                String flatcarOtherTempPrefix = "flatcarOtherTemp";
                flatcarName = "衍生";
                applicationNum = PropertyUtils.FLAT_CAR_OTHER_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarOtherTempPrefix,platformApply, businessKey);
                break;
            default :
                throw new MyException("申请类型不正确");
        }
        String requestName = goodsName + " " + platformApply.getJobContent() + "(" + platformApply.getRequestType() + ")";
        //返回值放到map中
        return MapUtils.getMap("requestName",requestName,"applicationNum",applicationNum,"businessKey",businessKey,"flatcarName",flatcarName);
    }

    /**
     * 插入一条平板车任务的申请 ，获取插入的实例
     * @param flatcarPlanName
     * @param flatcarPlanPrefix
     * @param platformApply
     * @param businessKey
     * @return
     */
    private String getProcessInstance(String flatcarPlanName, String flatcarPlanPrefix, PlatformApply platformApply, String businessKey) {
        String count = PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "DeploymentGroupCount");
        //插入一条平板车任务的申请
        Map<String, Object> variables = new HashMap<>(Integer.parseInt(count) + 2);
        variables.put("startName", platformApply.getApplyName());
        variables.put("dept", platformApply.getRequestDepartment() + "申请");
        //动态的向流程中添加审批角色属性
        List<String> roleIdsList = new ArrayList<>(Integer.parseInt(count));
        for (int i = 1 ; i <= Integer.parseInt(count) ; i++ ) {
            String roleIds = PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "GroupTask" + i);
            roleIdsList.add(roleIds);
        }
        //一次性查出来所有的角色用户
        List<DeptUser> usersList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUserRoleId, roleIdsList));
        for (int i = 1 ; i <= Integer.parseInt(count) ; i++ ) {
            String roleId = PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "GroupTask" + i);
            List<DeptUser> deptUsers = new ArrayList<>(usersList.size());
            for (DeptUser deptUser : usersList) {
                String userRoleId = String.valueOf(deptUser.getUserRoleId());
                if (roleId.equals(userRoleId)) {
                    deptUsers.add(deptUser);
                }
            }
            //获取角色用户的名字
            StringBuilder builder = new StringBuilder();
            for (DeptUser user : deptUsers ) {
                builder.append(user.getUsername()).append(",");
            }
            String substring = builder.substring(0, builder.length() - 1);
            variables.put(flatcarPlanPrefix + "GroupTask" + i, substring);
        }
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "DeploymentId"), businessKey, variables);
        String processInstanceId = processInstance.getProcessInstanceId();
        if (processInstanceId == null || processInstanceId == "" || processInstanceId.isEmpty()){
            throw new MyException("平板车" + flatcarPlanName + "任务插入失败");
        }
        //获取运输分段的名字
        PlatformGoods platform = platformGoodsMapper.selectById(platformApply.getObjectId());
        String goodsName = platform.getGoodsCode();
        //更新平板车申请绑定状态
        platform.setIsBinder(1);
        Integer update = platformGoodsMapper.updateById(platform);
        if (update <=0){
            throw new MyException("平板车" + flatcarPlanName + "任务插入失败");
        }
        return goodsName;
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
        String taskId = taskService.createTaskQuery().processInstanceId(instanceId).singleResult().getId();
        String taskName = task.getName();
        Boolean result =insertAppMassage(taskId, taskName, candidateUsers);
        if (!result) {
            throw new MyException("插入消息失败，请联系管理员");
        }
        return taskId;
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
     * 批量插入消息
     * @param taskId
     * @param taskName
     * @param candidateUser
     * @return
     */
    private Boolean insertAppMassage(String taskId, String taskName, String candidateUser) {
        String[] candidateUse = candidateUser.split(",");
        List<AppTasksMassage> massages = new ArrayList<>(candidateUse.length);
        for (String string : candidateUse) {
            massages.add(EntityUtils.getAppMassage(taskName + "未审批任务", Integer.parseInt(taskId), string));
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
            businessKey = processInstance.getBusinessKey();
        } else {
            businessKey = processInstance.getBusinessKey();
        }
        return businessKey;
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

}