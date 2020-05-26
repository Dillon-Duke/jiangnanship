package com.caidao.service;

import com.caidao.entity.Dept;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 获取所有的部门列表
     * @return
     */
    List<Dept> findSysDept();


    /**
     * 查询所有的部门列表
     * @return
     */
    List<Dept> getListDept();
}
