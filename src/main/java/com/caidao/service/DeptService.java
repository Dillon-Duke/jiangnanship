package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.Dept;
import com.caidao.pojo.DeptRole;

import java.util.List;

/**
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
     * 获得部门列表信息
     * @return
     */
    List<Dept> selectList();
}
