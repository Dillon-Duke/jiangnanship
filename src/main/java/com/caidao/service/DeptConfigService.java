package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.DeptConfig;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-27
 */
public interface DeptConfigService extends IService<DeptConfig> {

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptConfig
     * @return
     */
    IPage<DeptConfig> findPage(Page<DeptConfig> page, DeptConfig deptConfig);

    /**
     * 查询所有的部门列表
     * @return
     */
    List<DeptConfig> getListDept();

    /**
     * 获取用户的所有权限
     * @param userId
     * @return
     */
    List<String> getPowerByUserId(Integer userId);
}
