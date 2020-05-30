package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.DeptUser;
import com.caidao.entity.FlatcarPlan;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-23
 */
public interface FlatcarPlanService extends IService<FlatcarPlan> {

    /**
     * 提交一个平板车计划任务
     * @param flatcarPlan
     * @return
     */
    Integer applyFlatcarPlan(FlatcarPlan flatcarPlan, DeptUser deptUser);

    /**
     * 通过创建人id 查询任务列表
     * @param id
     * @return
     */
    List<FlatcarPlan> selectListByApplyId(Integer id);

    /**
     * 完成给人任务的审批
     * @param flatcarPlan
     * @param taskId
     * @param reasion
     * @return
     */
    Boolean complayUserTask(FlatcarPlan flatcarPlan,String taskId,String reasion);

    void getUserApplyTask(DeptUser deptUser);
}
