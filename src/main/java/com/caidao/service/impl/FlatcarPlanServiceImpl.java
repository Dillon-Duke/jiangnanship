package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.FlatcarPlan;
import com.caidao.entity.SysUser;
import com.caidao.mapper.FlatcarPlanMapper;
import com.caidao.service.FlatcarPlanService;
import com.caidao.util.DateUtils;
import com.caidao.util.PropertiesReaderUtils;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-23
 */
@Service
public class FlatcarPlanServiceImpl extends ServiceImpl<FlatcarPlanMapper, FlatcarPlan> implements FlatcarPlanService {

    @Autowired
    private FlatcarPlanMapper flatcarPlanMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 复写平板车计划流程，增加创建人信息，状态
     * @param flatcarPlan
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(FlatcarPlan flatcarPlan) {
        flatcarPlan.setCreateDate(LocalDateTime.now());
        flatcarPlan.setApplyState(0);
        flatcarPlan.setState(1);
        return super.save(flatcarPlan);
    }

    /**
     * 提交一个平板车计划任务
     * @param flatcarPlan
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Integer applyFlatcarPlan(FlatcarPlan flatcarPlan, SysUser sysUser) {

        //如果平板车计划没有id 则为新增提交任务
        if (flatcarPlan.getFlatcarId() == null){

            flatcarPlan.setCreateId(sysUser.getUserId());
            flatcarPlan.setCreateDate(LocalDateTime.now());
            flatcarPlan.setApplyState(1);
            flatcarPlan.setState(1);
            flatcarPlan.setApplyName(sysUser.getUsername());
            flatcarPlan.setJobNumber("PBSQLS" + DateUtils.yyyyMMdd());
            Integer insert = flatcarPlanMapper.insert(flatcarPlan);
            //平板车计划任务提交成功 新增提交流程
            if (insert != 0){

                //设置业务主键
                String businessKey = sysUser.getUsername() + "." + flatcarPlan.getFlatcarId();
                boolean instence = startInstence(flatcarPlan, businessKey);

                if (!instence){
                    throw new RuntimeException("提交平板车计划任务失败，请重新提交");
                }
                //返回新建任务实例Id
                return insert;

            }else {
                throw new RuntimeException("提交平板车计划任务失败，请重新提交");
            }
        } else {
            //如果平板车计划有id 则为更新提交任务
            flatcarPlan.setUpdateId(sysUser.getUserId());
            flatcarPlan.setUpdateDate(LocalDateTime.now());
            flatcarPlan.setJobNumber("PBSQLS" + DateUtils.yyyyMMdd());

            Integer updateById = flatcarPlanMapper.updateById(flatcarPlan);
            //平板车计划任务提交成功 新增提交流程
            if (updateById == 1) {

                //设置业务主键
                String businessKey = sysUser.getUsername() + "." + flatcarPlan.getFlatcarId();
                boolean instence = startInstence(flatcarPlan, businessKey);
                if (!instence){
                    throw new RuntimeException("提交平板车计划任务失败，请重新提交");
                }
                //返回新建任务实例Id
                return flatcarPlan.getFlatcarId();
            }
        }
        return -1;
    }

    /**
     * 通过创建人id 查询任务列表
     * @param id
     * @return
     */
    @Override
    public List<FlatcarPlan> selectListByApplyId(Integer id) {
        List<FlatcarPlan> planList = flatcarPlanMapper.selectList(new LambdaQueryWrapper<FlatcarPlan>()
                .eq(FlatcarPlan::getCreateId, id));
        return planList;
    }

    /**
     * 完成给人任务的审批
     * @param flatcarPlan
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean complayUserTask(FlatcarPlan flatcarPlan,String taskId,String reasion) {

        //设置下一个审批人的名称
        taskService.complete(taskId);

        //设置审批人姓名
        taskService.setVariableLocal(taskId,"apprName",flatcarPlan.getApprName());

        //更新计划流程表里面的信息
        flatcarPlan.setApprName(flatcarPlan.getApprName());
        Integer update = flatcarPlanMapper.updateById(flatcarPlan);
        //TODO 将同意原因写在一个表里面 ，以后查询的时候调用数据少
        if (update == 1){
            return true;
        }
        return false;
    }

    /**
     * 新增一个实例
     * @param flatcarPlan
     * @param businessKey
     * @return
     */
    private boolean startInstence(FlatcarPlan flatcarPlan, String businessKey) {
        //设置审批人姓名
        Map<String, Object> variables = new HashMap<String, Object>(1);
        variables.put("apprName",flatcarPlan.getApprName());

        Map<String, String> map = PropertiesReaderUtils.getMap();
        String planDeploymentId = map.get("flatcarPlanDeploymentId");

        //启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(planDeploymentId, businessKey, variables);
        String businessKey1 = processInstance.getBusinessKey();

        //判断是否启动成功
        if (businessKey != businessKey1){
            throw new RuntimeException("平板车计划任务申请失败");
        }
        return true;
    }
}
