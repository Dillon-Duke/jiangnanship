package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.DeptUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Dillon
 * @since 2020-05-28
 */
public interface DeptUserService extends IService<DeptUser> {

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    IPage<DeptUser> getDeptUserPage(Page<DeptUser> page, DeptUser deptUser);

    /**
     *通过名字获取对应的用户信息
     * @param toString
     * @return
     */
    DeptUser getUserByUsername(String toString);
}
