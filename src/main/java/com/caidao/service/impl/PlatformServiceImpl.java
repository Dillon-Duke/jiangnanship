package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.PlatformMapper;
import com.caidao.mapper.TranGoodsMapper;
import com.caidao.param.ActivityQueryParam;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.Platform;
import com.caidao.pojo.TranGoods;
import com.caidao.service.PlatformService;
import com.caidao.util.ActivitiObj2MapUtils;
import com.caidao.util.DateUtils;
import com.caidao.util.PropertiesReaderUtils;
import com.caidao.util.PropertyUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
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
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, Platform> implements PlatformService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TranGoodsMapper tranGoodsMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    /**
     * 保存一个平板车计划任务流程
     * @param platform
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> saveFlatCarPlan(Platform platform) {
        platform.setCreateDate(LocalDateTime.now());
        platform.setApplyState(0);
        platform.setState(1);

        //将申请任务保存
        int insert = platformMapper.insert(platform);
        if (insert != 1) {
            throw new MyException("平板车计划任务插入失败");
        }
        //获取插入数据的ID
        Integer id = platform.getPrsId();

        //生产申请工单号
        String BusinessKey = String.valueOf(id);
        String sort = null;
        if (6 - BusinessKey.length() >0){
            sort = String.format("%06d",id);
        }else {
            sort = BusinessKey.substring(BusinessKey.length()-6);
        }
        String requestOddNumber = PropertyUtils.FLAT_CAR_PLAN_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;

        String goodsName = null;
        switch (platform.getRequestType()) {
            case 1:
                //插入一条平板车任务的申请
                Map<String, Object> variables = new HashMap<>(1);
                variables.put("startName", platform.getApplyName());
                variables.put("dept", platform.getRequestDepartment() + "申请");
                //动态的向流程中添加审批角色属性
                String count = PropertiesReaderUtils.getMap().get("flatcarPlanDeploymentGroupCount");
                for (int i = 1 ; i <= Integer.parseInt(count) ; i++ ) {
                    String roleId = PropertiesReaderUtils.getMap().get("flatcarPlanGroupTask" + i);
                    //获取角色用的ids
                    List<DeptUser> users = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                            .eq(DeptUser::getUserRoleId, roleId));
                    //获取角色用户的名字
                    StringBuilder builder = new StringBuilder();
                    for (DeptUser user : users) {
                        builder.append(user.getUsername()).append(",");
                    }
                    String substring = builder.substring(0, builder.length() - 1);
                    variables.put("flatcarPlanGroupTask" + i, substring);
                }

                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(PropertiesReaderUtils.getMap().get("flatcarPlanDeploymentId"),BusinessKey,variables);
                String processInstanceId = processInstance.getProcessInstanceId();
                if (processInstanceId == null || processInstanceId == "" || processInstanceId.isEmpty()){
                    throw new MyException("平板车计划任务插入失败");
                }

                //TODO 获取驳运物件的名称 ，目前是获取了驳运部门的类型 表不对，
                TranGoods goods = tranGoodsMapper.selectById(platform.getObjectId());
                goodsName = goods.getGoodsType();

                //TODO 需要在驳运物件表里面设置一个字段，表示该物件是否被绑定驳运
                goods.setGoodsType("1");
                int update = tranGoodsMapper.updateById(goods);
                if (update <=0){
                    throw new MyException("平板车计划任务插入失败");
                }
                break;
            default :
        }

        //返回值放到map中
        Map<String, Object> map = new HashMap<>(4);
        map.put("requestName",goodsName + " " + platform.getJobContent() + "(" + platform.getRequestType() + ")");
        map.put("returnMessage","申请平板车任务保存成功");
        map.put("applicationNum",requestOddNumber);
        map.put("BusinessKey",BusinessKey);
        return map;
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param id
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = MyException.class)
    public Boolean removePlanById(String id,String reason) {
        Platform selectById = platformMapper.selectById(id);
        //获取任务的申请状态，已提交状态的任务不能被删除
        Integer state = selectById.getApplyState();
        if(state != 0) {
            throw new MyException("任务申请已提交，不能删除");
        }

        //删除未提交的任务
        int delete = platformMapper.deleteById(id);
        if(delete <= 0) {
            throw new MyException("删除操作失败，请重试");
        }

        //更改分段绑定信息状态
        int update = tranGoodsMapper.updateGoodsBindState(selectById.getObjectId());
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
     * @param platform
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, Object> startPlanTasks(Platform platform) {

        //判断提交的是不是之前保存过的申请
        Integer prsId = platform.getPrsId();

        //为空说明之前没有保存过 ，没有自动生成了对应的主键
        Map<String, Object> saveFlatCarPlanMap = null;
        if (prsId == null || prsId == 0){
            saveFlatCarPlanMap = this.saveFlatCarPlan(platform);
        }

        //更改审批状态，未审批变为审批中
        platform.setUpdateDate(LocalDateTime.now());
        platform.setApplyState(1);

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
            map.put("returnMessage","申请平板车计划任务申请成功");
            map.put("applicationNum",saveFlatCarPlanMap.get("applicationNum"));
            map.put("BusinessKey",saveFlatCarPlanMap.get("BusinessKey"));
            return map;
        } catch (RuntimeException e) {
            throw new MyException("任务申请失败，请联系管理员确认");
        }
    }

    /**
     * 获取用户的任务列表
     * @param username
     * @return
     */
    @Override
    public List<Map<String, Object>> getDeptUserTaskList(ActivityQueryParam param, String username) {
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
     * @param username
     * @return
     */
    @Override
    public List<HistoricTaskInstance> getUserHistoryTaskList(ActivityQueryParam param, String username) {
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
                if (endTime.after(endTime1)){
                    list1.add(historicTaskInstance);
                }
            }
        } else {
            Date startTime = DateUtils.string2Date(startDate);
            for (HistoricTaskInstance historicTaskInstance : list) {
                Date endTime1 = historicTaskInstance.getEndTime();
                Date startTime1 = historicTaskInstance.getStartTime();
                if (endTime.after(endTime1) && startTime.before(startTime1)){
                    list1.add(historicTaskInstance);
                }
            }
        }
        return list1;
    }

    /**
     * 查询个人用户的组任务列表
     * @return
     */
    @Override
    public List<Task> listPlanOwnerGroupTask(ActivityQueryParam param) {

        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(StringUtils.hasText(param.getTaskName()) ? param.getTaskName() : null)
                .taskCandidateUser(param.getUserName())
                .list();
        return list;
    }

    /**
     * 用户拾取组任务
     * @param taskId
     * @return
     */
    @Override
    @Transactional(rollbackFor = MyException.class)
    public void getPlanOwnerGroupTask(String taskId, String username) {
        try {
            taskService.claim(taskId,username);
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
            Integer update = platformMapper.updateApplyName(businuessKey,username);
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
    public void flatCarPlan2OtherUser(String taskId, String username) {
        try {
            taskService.setOwner(taskId, username);
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
            Integer update = platformMapper.updateApplyName(businuessKey,username);
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
    public void backPlanOwner2GroupTask(String taskId) {
        try {
            taskService.setAssignee(taskId,null);
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
            Integer update = platformMapper.updateApplyName(businuessKey,null);
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
     * @param  username
     * @return
     */
    @Override
    public List<Map<String, Object>> getApprovalList(ActivityQueryParam param, String username) {

        //获取所有满足条件的任务列表
        List<Platform> submitList = platformMapper.selectList(new LambdaQueryWrapper<Platform>()
                .eq(StringUtils.hasText(param.getTaskName()), Platform::getRequestType, param.getTaskName())
                .eq(StringUtils.hasText(param.taskState), Platform::getApplyState, param.getTaskState())
                .eq(Platform::getApplyName, username)
                .orderByAsc(Platform::getObjectId));

        //判断如果没有任何任务，直接返回空值
        if(submitList.size() == 0){
            return null;
        }

        //获取所有的TranGoods的id 放到新的list中
        List<Integer> tranGoodsIds = new ArrayList<>();
        for (Platform requestSubmit : submitList) {
            tranGoodsIds.add(requestSubmit.getObjectId());
        }

        //获取所有的物件信息
        List<TranGoods> tranGoods = tranGoodsMapper.selectList(new LambdaQueryWrapper<TranGoods>()
                .in(TranGoods::getGoodsId, tranGoodsIds)
                .orderByAsc(TranGoods::getGoodsId));

        //封装返回的内容
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < submitList.size(); i++) {
            Map<String, Object> resultMap = new HashMap<>(6);
            Platform requestSubmit = submitList.get(i);
            resultMap.put("requestID", requestSubmit.getPrsId());
            resultMap.put("requestName",tranGoods.get(i).getGoodsType() + " " + requestSubmit.getJobContent() + "(" + requestSubmit.getRequestType() + ")");
            resultMap.put("status",requestSubmit.getApplyState());
            resultMap.put("startTime",requestSubmit.getStartTime());
            resultMap.put("endTime",requestSubmit.getEndTime());
            resultMap.put("object",tranGoods.get(i).getGoodsType());
            list.add(resultMap);
        }
        return list;
    }

}

