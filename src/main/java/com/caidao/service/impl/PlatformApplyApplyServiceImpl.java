package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.*;
import com.caidao.param.ActivityQueryParam;
import com.caidao.param.PlatformCancelParam;
import com.caidao.pojo.*;
import com.caidao.service.CarService;
import com.caidao.service.PlatformApplyService;
import com.caidao.util.*;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
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
import org.apache.shiro.SecurityUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dillon
 * @since 2020-06-11
 */
@Service
@Slf4j
public class PlatformApplyApplyServiceImpl extends ServiceImpl<PlatformApplyMapper, PlatformApply> implements PlatformApplyService {

    private static final int FIXED_BUSINESS_KEY_LENGTH = 6;

    private static final String SAVE_PLATFORM_APPLY = "save";

    private static final String SUBMIT_PLATFORM_APPLY = "submit";

    @Autowired
    private RuntimeService runtimeService;

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
     * 通过Id获取申请单的详情
     * @param prsId
     * @return
     */
    @Override
    public PlatformApply getPlatformById(Integer prsId) {
        Assert.notNull(prsId,"申请单Id不能为空");
        PlatformApply platformApply = platformApplyMapper.selectById(prsId);
        return platformApply;
    }

    /**
     * 开始一个平板车计划任务流程
     * @param platformApply
     * @return 流程实例Id
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> saveOrStartPlanTasks(PlatformApply platformApply) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null ){
            throw new MyException("登录已超时，请重新登录");
        }
        log.info("用户{}申请了一个未提交任务",deptUser.getUsername());
        String result = platformApply.getOperateState();
        platformApply.setCreateId(deptUser.getUserId());
        platformApply.setApplyName(deptUser.getUsername());
        platformApply.setCreateDate(LocalDateTime.now());
        platformApply.setState(1);
        //判断是新增的保存或者提交还是修改的保存或者提交
        if (platformApply.getPrsId() == null) {
            //新增保存的逻辑
            if (SAVE_PLATFORM_APPLY.equals(result)) {
                platformApply.setApplyState(0);
                //将申请任务保存
                platformApplyMapper.insert(platformApply);
                Map<String, String> map = startPlatformProcessInstance(platformApply);
                String goodsName = map.get("goodsName");
                String applicationNum = map.get("applicationNum");
                String flatcarName = map.get("flatcarName");
                String businessKey = map.get("businessKey");
                String requestName = goodsName + " " + platformApply.getJobContent() + "(" + getSavePlatformApplyName(platformApply.getRequestType()) + ")";
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
            //提交的逻辑
            if (SUBMIT_PLATFORM_APPLY.equals(result)) {
                platformApply.setUpdateId(deptUser.getUserId());
                platformApply.setUpdateDate(LocalDateTime.now());
                platformApply.setApplyState(1);
                //将申请任务保存
                platformApplyMapper.insert(platformApply);
                Map<String, String> map = startPlatformProcessInstance(platformApply);
                String goodsName = map.get("goodsName");
                String applicationNum = map.get("applicationNum");
                String flatcarName = map.get("flatcarName");
                String businessKey = map.get("businessKey");
                String requestName = goodsName + " " + platformApply.getJobContent() + "(" + getSavePlatformApplyName(platformApply.getRequestType()) + ")";
                //返回值放到map中
                platformApply.setRequestOddNumber(applicationNum);
                platformApply.setRequestName(requestName);
                platformApplyMapper.updateById(platformApply);
                return completePlatformApply(platformApply.getOperateState(),applicationNum, flatcarName, businessKey, requestName);
            }
        } else {
            String applicationNum = platformApply.getRequestOddNumber();
            String businessKey = String.valueOf(platformApply.getPrsId());
            String requestName = platformApply.getRequestName();
            String flatcarName = getSavePlatformApplyName(platformApply.getRequestType());
            if (SAVE_PLATFORM_APPLY.equals(result)) {
                //将更改的数据进行保存
                platformApplyMapper.updateById(platformApply);
                String returnMessage = "平板车" + flatcarName + "任务修改成功";
                return MapUtils.getMap("requestName",requestName,"returnMessage",returnMessage,"applicationNum",applicationNum,"businessKey", businessKey);
            }
            if (SUBMIT_PLATFORM_APPLY.equals(result)) {
                //返回值放到map中
                platformApplyMapper.updateById(platformApply);
                return completePlatformApply(platformApply.getOperateState(),applicationNum, flatcarName, businessKey, requestName);
            }
        }
        return null;
    }

    /**
     * 删除保存的平板车计划任务流程
     * @param platformApplyId
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void deletePlatformTaskByPlatformApplyId(Integer platformApplyId) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUser,"用户登录超时，请重新登录");
        log.info("用户{}删除申请单为{}的未申请任务",deptUser.getUsername(),platformApplyId);
        PlatformApply platformApply = platformApplyMapper.selectOne(new LambdaQueryWrapper<PlatformApply>()
                .eq(PlatformApply::getPrsId,platformApplyId)
                .eq(PlatformApply::getState,1));
        //获取任务的申请状态，已提交状态的任务不能被删除
        Integer state = platformApply.getApplyState();
        if(state != 0) {
            throw new MyException("任务申请已提交，不能删除");
        }
        //删除未提交的任务
        platformApplyMapper.deleteById(platformApplyId);
        //更改分段绑定信息状态
        platformGoodsMapper.updateGoodsBindStateWithGoodsId(platformApply.getObjectId());
        //删除流程中的开始实例
        ProcessInstanceQuery instanceQuery = runtimeService.createProcessInstanceQuery();
        String id = instanceQuery.processInstanceBusinessKey(String.valueOf(platformApplyId)).singleResult().getId();
        try {
            runtimeService.deleteProcessInstance(id,"用户未提交申请，可以删除");
        } catch (RuntimeException e) {
            throw new MyException("删除流程操作失败，请联系管理员");
        }
    }

    /**
     * 获取的所有任务列表
     * @return
     */
    @Override
    public Map<String, Object> getApprovalList() {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUser,"用户登录超时，请重新登录");
        //获取用户的审批任务
        List<AppTasksMassage> tasksMassages = appTasksMassageMapper.selectList(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getUserId, deptUser.getUserId())
                .eq(AppTasksMassage::getState, 1));
        //获取用户的平板车任务
        List<PlatformApply> platformApplies = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .eq(PlatformApply::getCreateId, deptUser.getUserId())
                .eq(PlatformApply::getState, 1));
        //获取所有的TranGoods的id 放到新的list中
        List<Integer> tranGoodsIds = platformApplies.stream().map(PlatformApply::getObjectId).collect(Collectors.toList());
        //获取所有的物件信息
        List<PlatformGoods> platformGoods = platformGoodsMapper.selectList(new LambdaQueryWrapper<PlatformGoods>()
                .in(PlatformGoods::getGoodsId, tranGoodsIds)
                .eq(PlatformGoods::getState,1));
        platformGoods.sort(Comparator.comparingInt(PlatformGoods::getGoodsId));
        //封装返回的内容
        List<Map<String, Object>> platformApplyList = new ArrayList<>();
        for (PlatformApply platformApply : platformApplies) {
            //判断申请里面的物品对象是否还存在
            List<PlatformGoods> goodsList = platformGoods.stream().filter(a -> a.getGoodsId().equals(platformApply.getObjectId())).collect(Collectors.toList());
            if (goodsList.size() == 0) {
                throw new MyException("申请绑定的商品不存在");
            }
            //将一个申请一个对应申请对象封装成一个Map
            platformApplyList.add(MapUtils.getMap("platformApply", platformApply, "PlatformGoods", goodsList.get(0)));
        }
        return MapUtils.getMap("tasksMassages",tasksMassages,"platformApplyList",platformApplyList);
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
        List<String> arrayList = tasks.stream().map( x -> x.getProcessInstanceId()).collect(Collectors.toList());
        //自定义查询对应BusinessKeys
        List<String> integers = customActivitiMapper.getInstanceListByInstanceIds(arrayList);
        //获取对应的列表
        List<PlatformApply> platformApplyList = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .in(PlatformApply::getPrsId, integers)
                .orderByDesc(PlatformApply::getPrsId));
        return platformApplyList;
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
     * 流程任务的转办，直接给别人，别人做好之后直接推到下一个需要办理的人手里
     * taskService.deleteCandidateUser(taskId,"原用户ID");
     * taskService.addCandidateUser(taskId,"新用户ID");
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void flatCarPlan2OtherUser(String taskId, String username) {
        Assert.notNull(taskId, "任务ID不能为空");
        Assert.notNull(username, "用户名称不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        log.info("任务id为{}交给{}", taskId, username);
        //用户转交之前查询
        AppTasksMassage massage = appTasksMassageMapper.selectOne(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getTaskId, taskId));
        if (massage == null) {
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
            taskService.setAssignee(taskId, username);
            //获取流程的业务主键
            String businessKey = toTaskIdGetBusinessKey(taskId);
            //增加数据库中审批人
            Integer update = platformApplyMapper.updateTaskApprovalName(Integer.parseInt(businessKey), username, deptUser.getUserId());
            if (update <= 0) {
                throw new MyException("任务指派失败，请重试");
            }
        } catch (RuntimeException e) {
            throw new MyException("任务指派失败，请重试");
        }
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
        Set<String> set = null;
        for (int i = 0; i < nameList.size(); i++) {
            set = Arrays.stream(nameList.get(i).split(",")).collect(Collectors.toSet());
        }
        //去除姓名中司机和跟车员名称
        Map<String, String> map = new HashMap<>(2);
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUsername, set));
        for (DeptUser deptuser : userList) {
            if (deptuser.getUserRoleName() == null) {
                map.put("用户权限","该用户无取消权限");
                break;
            }
            if (deptuser.getUserRoleName().contains("司机")) {
                set.remove(deptuser.getUsername());
            }
            if (!set.contains(deptUser.getUsername())) {
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
        String newTaskId = addNextCandidateMassage(null,task,instanceId, null);
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
    public String flatcarCancelDriverCompleteTask(PlatformCancelParam param) {
        Assert.notNull(param.getTaskId(),"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("司机{}完成任务的执行",deptuser.getUsername());
        String cancelBusinessKey = param.getCancelBusinessKey();
        String taskId = param.getTaskId();
        //解除司机与车辆的绑定
        deptUserCarApplyMapper.delete(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getBusinessKey, cancelBusinessKey));
        //解绑驳运完成的物件信息
        //获取对应的业务Id
        String businessKey = toTaskIdGetBusinessKey(taskId);
        //更新对应的物件信息为未绑定
        platformApplyMapper.updateGoodsBindStateToUnbind(businessKey);
        //获得对应的实例Id
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //司机完成任务执行
        taskService.complete(taskId);
        //删除推送消息中信息
        deleteCompleteMassage(taskId);
        //给部门评价人员推送消息
        String instanceId = task.getProcessInstanceId();
        String newTaskId = addNextCandidateMassage(null,task,instanceId, null);
        return newTaskId;
    }

    /**
     * 取消任务部门评价人员进行评价
     * @param param
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean flatcarCancelDepartmentEvaluate(PlatformCancelParam param) {
        Assert.notNull(param.getTaskId(),"任务id不能为空");
        DeptUser deptuser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptuser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("部门评价人员{}完成评价",deptuser.getUsername());
        String taskId = param.getTaskId();
        //新增部门评价的信息
        //todo 之后确认评价信息表字段之后更新
        //工作流程的完成
        taskService.complete(taskId);
        //删除推送消息中信息
        deleteCompleteMassage(taskId);
        //更新平板车申请人里面审批人未null
        Integer integer = platformApplyMapper.updateApplyNameAsNull(param.getCancelBusinessKey());
        if (integer == 0) {
            return false;
        }
        return true;
    }

    /**
     * 通过申请Id获取详细的申请信息
     * @param platformApplyId
     * @return
     */
    @Override
    public Map<String, Object> getPlatformApplyDetailInfoByApplyId(Integer platformApplyId) {
        //获取申请单详情
        PlatformApply platformApply = platformApplyMapper.selectOne(new LambdaQueryWrapper<PlatformApply>()
                .eq(PlatformApply::getPrsId,platformApplyId)
                .eq(PlatformApply::getState,1));
        //获取申请单绑定的车辆以及时间信息
        DeptUserCarApply userCarApply = deptUserCarApplyMapper.selectOne(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getBusinessKey, platformApplyId));
        //获取对应的车辆信息
        Car car = carService.getById(userCarApply.getCarId());
        return MapUtils.getMap("platformApplyDetails",platformApply,"bindTimeMassage",userCarApply,"car",car);
    }

    /**
     * 获取平板车申请的工单号
     * @param businessKey
     * @return
     */
    private String getPlatformApplyJobNum(String businessKey) {
        String sort;
        int parseInt = Integer.parseInt(businessKey);
        if (FIXED_BUSINESS_KEY_LENGTH - businessKey.length() >0){
            sort = String.format("%06d",parseInt);
        }else {
            sort = businessKey.substring(businessKey.length()-6);
        }
        return sort;
    }

    /**
     * 开始平板车申请流程
     * @param apply
     * @return
     */
    private Map<String, String> startPlatformProcessInstance(PlatformApply apply){
        //获取插入数据的ID
        String businessKey = String.valueOf(apply.getPrsId());
        //生产申请工单号
        String sort = getPlatformApplyJobNum(businessKey);
        //保存一个驳运流程并且获取驳运对象的对象名字
        String applicationNum;
        String goodsName;
        String flatcarName;
        switch (apply.getRequestType()) {
            case 1:
                //插入一条平板车计划任务的申请
                String flatcarPlanPrefix = "flatcarPlan";
                flatcarName = getSavePlatformApplyName(1);
                applicationNum = PropertyUtils.FLAT_CAR_PLAN_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName, flatcarPlanPrefix, apply, businessKey);
                break;
            case 2:
                //插入一条平板车临时任务的申请 ，获取插入的实例
                String flatcarTempPrefix = "flatcarTemp";
                flatcarName =  getSavePlatformApplyName(2);
                applicationNum = PropertyUtils.FLAT_CAR_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarTempPrefix,apply, businessKey);
                break;
            case 3:
                //插入一条平板车取消任务的申请 ，获取插入的实例
                String flatcarCancelPrefix = "flatcarFast";
                flatcarName =  getSavePlatformApplyName(3);
                applicationNum = PropertyUtils.FLAT_CAR_CANCEL_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarCancelPrefix,apply, businessKey);
                break;
            case 4:
                //插入一条平板车衍生任务的申请 ，获取插入的实例
                String flatcarOtherTempPrefix = "flatcarOtherTemp";
                flatcarName = getSavePlatformApplyName(4);
                applicationNum = PropertyUtils.FLAT_CAR_OTHER_TEMP_ODD_NUMBER_PREFIX + DateUtils.getYyyyMm() + sort;
                goodsName = getProcessInstance(flatcarName,flatcarOtherTempPrefix,apply, businessKey);
                break;
            default :
                throw new MyException("申请类型不正确");
        }
        return MapUtils.getMap("applicationNum",applicationNum,"goodsName",goodsName,"flatcarName",flatcarName,"businessKey",businessKey);
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
        variables.put("dept", platformApply.getRequestDepartmentName() + "申请");
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
            List<DeptUser> deptUsers = usersList.stream().filter((x) -> roleId.equals(String.valueOf(x.getUserRoleId()))).collect(Collectors.toList());
            //获取角色用户组的名字
            List<String> collect = deptUsers.stream().map(DeptUser::getUsername).collect(Collectors.toList());
            String substring = collect.toString().substring(1, collect.toString().length() - 1).replaceAll(" ","");
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
        platformGoodsMapper.updateById(platform);
        return goodsName;
    }

    /**
     * 新增消息库中下一个候选人未读信息
     * @param instanceId
     */
    private String addNextCandidateMassage(String operateState, Task task,String instanceId, Integer opinion) {
        //反求流程定义的Key
        String definitionKey = task.getTaskDefinitionKey();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(definitionKey);
        // 输出连线
        List<SequenceFlow> outFlows = flowNode.getOutgoingFlows();
        String name = loopNextPoint(bpmnModel, outFlows, opinion);
        //将用户批量放在信息表中 ,获取流程ID
        Task task1 = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
        String task1Id = task1.getId();
        String taskName = task1.getName();
        String candidateUsers = customActivitiMapper.getCandidateUsersInActivitiTables(name, instanceId);
        //获得部门代办类型
        String definitionKey1 = task1.getTaskDefinitionKey();
        FlowNode flowNode1 = (FlowNode) bpmnModel.getFlowElement(definitionKey1);
        // 输出连线
        List<SequenceFlow> outFlows1 = flowNode1.getOutgoingFlows();
        Integer massageType = getMassageType(outFlows1, taskName);
        //插入待办任务消息
        if (!SAVE_PLATFORM_APPLY.equals(operateState)) {
            insertAppMassage(task1Id, massageType, taskName, candidateUsers);
        }
        return task1Id;
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
     * 返回下一个节点的类型
     * @param outFlows
     * @return
     */
    private Integer getMassageType(List<SequenceFlow> outFlows, String taskName) {
        for (SequenceFlow outFlow : outFlows) {
            FlowElement element = outFlow.getTargetFlowElement();
            if (element instanceof UserTask && PropertyUtils.PLAT_CAR_PROCESS_DRIVER_EXECUTE.equals(taskName)) {
                return 3;
            } else if (element instanceof UserTask && PropertyUtils.PLAT_CAR_PROCESS_DRIVER_COMPLETE.equals(taskName)) {
                return 4;
            } else if (element instanceof ExclusiveGateway) {
                return 2;
            } else if (element instanceof EndEvent) {
                return 0;
            } else if (element instanceof UserTask) {
                return 1;
            }
        }
        return null;
    }

    /**
     * 批量插入代办事项消息
     * @param taskId
     * @param taskName
     * @param candidateUser
     * @return
     */
    private Boolean insertAppMassage(String taskId, Integer massageType, String taskName, String candidateUser) {
        String[] candidateUse = candidateUser.split(",");
        //数组转列表
        List<String> asList = Arrays.asList(candidateUse);
        //批量查询对应的人员
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUsername, asList));
        List<AppTasksMassage> massages = userList.stream().map((x) -> EntityUtils.getAppMassage(taskName + "任务", Integer.parseInt(taskId), x.getUserId(),x.getUsername(),massageType))
                                                            .collect(Collectors.toList());
        Integer integer = appTasksMassageMapper.insertBatches(massages);
        if (integer == 0) {
            return false;
        }
        return true;
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

    /**
     * 获取对应的申请类型名称
     * @param applyType
     * @return
     */
    private String getSavePlatformApplyName(Integer applyType){
        String flatcarName;
        switch (applyType) {
            case 1: {
                flatcarName = "计划";
                break;
            }
            case 2: {
                flatcarName = "临时";
                break;
            }
            case 3: {
                flatcarName = "快速";
                break;
            }
            case 4: {
                flatcarName = "衍生";
                break;
            }
            default: {
                throw new MyException("无此申请类型");
            }
        }
        return flatcarName;
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
     * 完成平板车任务的申请
     */
    @NotNull
    private Map<String, String> completePlatformApply(String operateState, String applicationNum, String flatcarName, String businessKey, String requestName) {
        TaskQuery taskQuery = taskService.createTaskQuery();
        Task task = taskQuery.processInstanceBusinessKey(businessKey).singleResult();
        String taskId = task.getId();
        //完成任务审批
        try {
            //完成前获得任务实例Id
            String instanceId = task.getProcessInstanceId();
            //完成任务
            taskService.complete(taskId);
            addNextCandidateMassage(operateState ,task, instanceId, null);
            String returnMessage = "平板车" + flatcarName + "任务申请成功";
            return MapUtils.getMap("requestName", requestName, "returnMessage", returnMessage, "applicationNum", applicationNum, "businessKey", businessKey);
        } catch (RuntimeException e) {
            //获取已经保存的任务
            throw new MyException("任务申请失败，请联系管理员确认");
        }
    }

}