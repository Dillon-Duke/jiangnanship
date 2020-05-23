package com.caidao.service;

import com.caidao.entity.FlatcarPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysUser;

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
    boolean applyFlatcarPlan(FlatcarPlan flatcarPlan, SysUser sysUser);

    /**
     * 通过创建人id 查询任务列表
     * @param id
     * @return
     */
    List<FlatcarPlan> selectListByApplyId(Integer id);
}
