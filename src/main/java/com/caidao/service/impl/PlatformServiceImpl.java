package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.PlatformGoodsMapper;
import com.caidao.mapper.PlatformMapper;
import com.caidao.param.ActivityQueryParam;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.PlatformApply;
import com.caidao.pojo.PlatformGoods;
import com.caidao.service.PlatformService;
import com.caidao.util.ActivitiObj2MapUtils;
import com.caidao.util.DateUtils;
import com.caidao.util.PropertiesReaderUtils;
import com.caidao.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.NativeProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Dillon
 * @since 2020-06-11
 */
@Service
@Slf4j
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, PlatformApply> implements PlatformService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private PlatformGoodsMapper platformGoodsMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    /**
     * 保存一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> saveFlatCarPlan(PlatformApply platformApply) {

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
        int insert = platformMapper.insert(platformApply);
        if (insert != 1) {
            throw new MyException("平板车计划任务插入失败");
        }

        //获取插入数据的ID
        Integer id = platformApply.getPrsId();

        //生产申请工单号
        String businessKey = String.valueOf(id);
        String sort = null;
        if (6 - businessKey.length() >0){
            sort = String.format("%06d",id);
        }else {
            sort = businessKey.substring(businessKey.length()-6);
        }
        String requestOddNumber;

        //保存一个驳运流程并且获取驳运对象的对象名字
        String goodsName;
        switch (platformApply.getRequestType()) {
            case 1:
                //插入一条平板车计划任务的申请
                String flatcarPlanPrefix = "flatcarPlan";
                String  flatcarPlanName = "计划";
                requestOddNumber = PropertyUtils.FLAT_CAR_PLAN_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarPlanName, flatcarPlanPrefix, platformApply, businessKey);
                break;
            case 2:
                //插入一条平板车临时任务的申请 ，获取插入的实例
                String flatcarTempPrefix = "flatcarTemp";
                String flatcarTempName = "临时";
                requestOddNumber = PropertyUtils.FLAT_CAR_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarTempName,flatcarTempPrefix,platformApply, businessKey);
                break;
            case 3:
                //插入一条平板车取消任务的申请 ，获取插入的实例
                String flatcarCancelPrefix = "flatcarCancel";
                String  flatcarCancelName = "取消";
                requestOddNumber = PropertyUtils.FLAT_CAR_CANCEL_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarCancelName,flatcarCancelPrefix,platformApply, businessKey);
                break;
            case 4:
                //插入一条平板车衍生任务的申请 ，获取插入的实例
                String flatcarOtherTempPrefix = "flatcarOtherTemp";
                String  flatcarOtherTempName = "衍生";
                requestOddNumber = PropertyUtils.FLAT_CAR_OTHER_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarOtherTempName,flatcarOtherTempPrefix,platformApply, businessKey);
                break;
            default :
                throw new MyException("申请类型不正确");
        }
        String requestName = goodsName + " " + platformApply.getJobContent() + "(" + platformApply.getRequestType() + ")";
        platformApply.setRequestName(requestName);
        Integer update = platformMapper.updateById(platformApply);
        if (update != 1) {
            throw new MyException("保存平板车任务失败");
        }
        //返回值放到map中
        Map<String, Object> map = new HashMap<>(4);
        map.put("requestName",requestName);
        map.put("returnMessage","平板车任务保存成功");
        map.put("applicationNum",requestOddNumber);
        map.put("BusinessKey",businessKey);
        return map;
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param id
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean removePlanById(String id,String reason) {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("用户{}删除未申请的任务",deptUser.getUsername());

        PlatformApply platformApply = platformMapper.selectById(id);
        //获取任务的申请状态，已提交状态的任务不能被删除
        Integer state = platformApply.getApplyState();
        if(state != 0) {
            throw new MyException("任务申请已提交，不能删除");
        }

        //删除未提交的任务
        int delete = platformMapper.deleteById(id);
        if(delete <= 0) {
            throw new MyException("删除操作失败，请重试");
        }

        //更改分段绑定信息状态
        int update = platformGoodsMapper.updateGoodsBindState(platformApply.getObjectId());
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
    public Map<String, Object> startPlanTasks(PlatformApply platformApply) {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null ){
            throw new MyException("登录已超时，请重新登录");
        }
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());

        //判断提交的是不是之前保存过的申请 未保存，应该先保存，
        Integer prsId = platformApply.getPrsId();
        Map<String, Object> saveFlatCarPlanMap = null;
        if (prsId == null || prsId == 0){
            saveFlatCarPlanMap = this.saveFlatCarPlan(platformApply);
        }

        //更改审批状态，未审批变为审批中
        platformApply.setUpdateId(deptUser.getUserId());
        platformApply.setUpdateDate(LocalDateTime.now());
        platformApply.setApplyName("");
        platformApply.setApplyState(1);
        platformMapper.updateById(platformApply);
        //获取任务ID
        Object businessKey = saveFlatCarPlanMap.get("BusinessKey");
        TaskQuery taskQuery = taskService.createTaskQuery();
        Task singleResult = taskQuery.processInstanceBusinessKey(String.valueOf(businessKey)).singleResult();
        if (singleResult == null){
            throw new MyException("任务申请失败，请联系管理员确认");
        }
        String taskId = singleResult.getId();

        //完成任务审批
        try {
            taskService.complete(taskId);
            Map<String, Object> map = new HashMap<>(4);
            map.put("requestName",saveFlatCarPlanMap.get("requestName"));
            map.put("returnMessage","平板车任务申请成功");
            map.put("applicationNum",saveFlatCarPlanMap.get("applicationNum"));
            map.put("BusinessKey",saveFlatCarPlanMap.get("BusinessKey"));
            return map;
        } catch (RuntimeException e) {
            throw new MyException("任务申请失败，请联系管理员确认");
        }
    }

    /**
     * 获取用户的任务列表
     * @return
     */
    @Override
    public List<Map<String, Object>> getDeptUserTaskList(ActivityQueryParam param) {

        //默认查询的当前用户的需要审批的列表
        Assert.notNull(param,"参数不能为空");

        //判断是查询自己的正在审批的任务还是别人正在审批的任务
        String username;
        if (param.getUserName() == null || param.getUserName() == ""){
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        } else {
            username = param.getUserName();
        }
        log.info("查询用户名为{}的任务列表",username);

        //activity中获取需要审批的任务列表
        TaskQuery taskQuery = taskService.createTaskQuery();

        //判断是否查询某一个任务审批列表
        String deploymentId = param.getDeploymentId();
        String string = StringUtils.hasText(deploymentId) ? deploymentId : null;
        List<Task> taskList = taskQuery.processDefinitionKey(string).taskAssignee(username).orderByTaskId().desc().list();

        String[] ps = {"id", "name", "processInstanceId", "owner", "assignee"};
        List<Map<String, Object>> listMap = new ArrayList<>();
        for (Task task : taskList) {
            Map<String, Object> map = ActivitiObj2MapUtils.obj2map(task, ps);
            listMap.add(map);
        }
        return listMap;
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
    public void getPlanOwnerGroupTask(String taskId) {

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        String username = deptUser.getUsername();
        Integer updateId = deptUser.getUserId();
        log.info("拾取用户名为{}的组任务列表", username);

        try {
            taskService.claim(taskId, username);
            //获取流程的业务主键
            String businuessKey;
            TaskQuery taskQuery = taskService.createTaskQuery();
            TaskEntity taskEntity = (TaskEntity) taskQuery.taskId(taskId).singleResult();
            HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
            HistoricProcessInstance processInstance = instanceQuery.processInstanceId(taskEntity.getProcessInstanceId()).singleResult();
            if (processInstance.getSuperProcessInstanceId() != null && processInstance.getBusinessKey() == null){
                processInstance = instanceQuery.processInstanceId(processInstance.getSuperProcessInstanceId()).singleResult();
                businuessKey = processInstance.getBusinessKey();
            } else {
                businuessKey = processInstance.getBusinessKey();
            }

            //增加数据库中审批人
            Integer update = platformMapper.updateApplyName(Integer.parseInt(businuessKey), username,updateId);
            if (update <= 0){
                throw new MyException("任务拾取失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("拾取任务失败，请重试");
        }

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
        try {
            taskService.setOwner(taskId, deptUser.getUsername());
            taskService.setAssignee(taskId,username);
            //获取流程的业务主键
            String businessKey;
            TaskQuery taskQuery = taskService.createTaskQuery();
            TaskEntity taskEntity = (TaskEntity) taskQuery.taskId(taskId).singleResult();
            HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
            HistoricProcessInstance processInstance = instanceQuery.processInstanceId(taskEntity.getProcessInstanceId()).singleResult();
            if (processInstance.getSuperProcessInstanceId() != null && processInstance.getBusinessKey() == null){
                processInstance = instanceQuery.processInstanceId(processInstance.getSuperProcessInstanceId()).singleResult();
                businessKey = processInstance.getBusinessKey();
            } else {
                businessKey = processInstance.getBusinessKey();
            }

            //增加数据库中审批人
            Integer update = platformMapper.updateApplyName(Integer.parseInt(businessKey),username,deptUser.getUserId());
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

        try {
            taskService.setAssignee(taskId,null);
            //获取流程的业务主键
            String businessKey;
            TaskQuery taskQuery = taskService.createTaskQuery();
            TaskEntity taskEntity = (TaskEntity) taskQuery.taskId(taskId).singleResult();
            HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
            HistoricProcessInstance processInstance = instanceQuery.processInstanceId(taskEntity.getProcessInstanceId()).singleResult();
            if (processInstance.getSuperProcessInstanceId() != null && processInstance.getBusinessKey() == null){
                processInstance = instanceQuery.processInstanceId(processInstance.getSuperProcessInstanceId()).singleResult();
                businessKey = processInstance.getBusinessKey();
            } else {
                businessKey = processInstance.getBusinessKey();
            }

            //增加数据库中审批人
            Integer update = platformMapper.updateApplyName(Integer.parseInt(businessKey),null,updateId);
            if (update <= 0){
                throw new MyException("任务指派失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("任务指派失败，请重试");
        }
    }

    /**
     * 获取用户的所有任务列表
     * @param param
     * @return
     */
    @Override
    public List<Map<String, Object>> getApprovalList(ActivityQueryParam param) {

        //默认查询的当前用户的所有审批的列表
        Assert.notNull(param,"参数不能为空");

        //判断是查询自己的正在审批的任务还是别人正在审批的任务
        String username;
        if (param.getUserName() == null || param.getUserName() == ""){
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            username = deptUser.getUsername();
        } else {
            username = param.getUserName();
        }
        log.info("查询用户名为{}的任务列表",username);

        //获取所有满足条件的任务列表
        List<PlatformApply> submitList = platformMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .eq(StringUtils.hasText(param.getTaskName()), PlatformApply::getRequestType, param.getTaskName())
                .eq(StringUtils.hasText(param.taskState), PlatformApply::getApplyState, param.getTaskState())
                .eq(PlatformApply::getApplyName, username)
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
            Map<String, Object> resultMap = new HashMap<>(6);
            PlatformApply requestSubmit = submitList.get(i);
            resultMap.put("requestID", requestSubmit.getPrsId());
            resultMap.put("requestName", platformGoods.get(i).getGoodsType() + " " + requestSubmit.getJobContent() + "(" + requestSubmit.getRequestType() + ")");
            resultMap.put("status",requestSubmit.getApplyState());
            resultMap.put("startTime",requestSubmit.getStartTime());
            resultMap.put("endTime",requestSubmit.getEndTime());
            resultMap.put("object", platformGoods.get(i).getGoodsType());
            list.add(resultMap);
        }
        return list;
    }

    /**
     * 获得可以编制的任务
     * @return
     */
    @Override
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
        NativeTaskQuery taskQuery = taskService.createNativeTaskQuery();
        NativeProcessInstanceQuery instanceQuery = runtimeService.createNativeProcessInstanceQuery();
        List<ProcessInstance> instanceList = instanceQuery.sql("SELECT * FROM " + managementService.getTableName(Execution.class) + " T " + "WHERE T.BUSINESS_KEY_ IS NOT NULL AND T.PROC_INST_ID_ IN (#{substring})").parameter("substring", substring).list();

        //查询对应的业务信息
        List<Integer> integers = new ArrayList<>(instanceList.size());
        for (ProcessInstance instance : instanceList) {
            integers.add(Integer.parseInt(instance.getBusinessKey()));
        }
        //获取对应的列表
        List<PlatformApply> platformApplyList = platformMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .in(PlatformApply::getPrsId, integers)
                .orderByDesc(PlatformApply::getPrsId));
        return platformApplyList;
    }

    /**
     * //插入一条平板车任务的申请 ，获取插入的实例
     * @param flatcarPlanName
     * @param flatcarPlanPrefix
     * @param platformApply
     * @param businessKey
     * @return
     */
    private String getProcessInstance(String flatcarPlanName, String flatcarPlanPrefix, PlatformApply platformApply, String businessKey) {
        //插入一条平板车任务的申请
        Map<String, Object> variables = new HashMap<>(1);
        variables.put("startName", platformApply.getApplyName());
        variables.put("dept", platformApply.getRequestDepartment() + "申请");
        //动态的向流程中添加审批角色属性
        String count = PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "DeploymentGroupCount");
        for (int i = 1 ; i <= Integer.parseInt(count) ; i++ ) {
            String roleId = PropertiesReaderUtils.getMap().get(flatcarPlanPrefix + "GroupTask" + i);
            //获取角色用的ids
            List<DeptUser> users = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                    .eq(DeptUser::getUserRoleId, roleId));
            //获取角色用户的名字
            StringBuilder builder = new StringBuilder();
            for (DeptUser user : users) {
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

        platform.setIsBinder(1);
        Integer update = platformGoodsMapper.updateById(platform);
        if (update <=0){
            throw new MyException("平板车" + flatcarPlanName + "任务插入失败");
        }
        return goodsName;
    }

}

