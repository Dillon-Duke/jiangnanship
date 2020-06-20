package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.caidao.pojo.Dept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.DeptRole;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-21
 */
public interface DeptService extends IService<Dept> {

    /**
     * 查询所有的部门列表
     * @return
     */
    List<Dept> getListDept();

    /**
     * 根据部门ID获取部门角色列表
     * @param deptId
     * @return
     */
    List<DeptRole> getDeptRoles(Integer deptId);

    /**
     * 获取所有的部门信息
     * @param iPage
     * @param dept
     * @return
     */
    IPage<Dept> selectPage(IPage<Dept> iPage, Dept dept);
}
