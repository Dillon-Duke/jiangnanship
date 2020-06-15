package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.DeptRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
public interface DeptRoleService extends IService<DeptRole> {

    /**
     * 获得部门角色的分页数据
     * @param page
     * @param deptRole
     * @return
     */
    IPage<DeptRole> getDeptRolePage(Page<DeptRole> page , DeptRole deptRole);

    /**
     * 获取部门所有的角色
     * @return
     */
    List<DeptRole> getDeptRoleList();
}
