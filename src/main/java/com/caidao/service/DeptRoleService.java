package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.DeptRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Dillon
 * @since 2020-05-27
 */
public interface DeptRoleService extends IService<DeptRole> {

    IPage<DeptRole> getDeptRolePage(Page<DeptRole> page , DeptRole deptRole);
}
