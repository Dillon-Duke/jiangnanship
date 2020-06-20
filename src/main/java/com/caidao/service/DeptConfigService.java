package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.DeptAuthorition;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-27
 */
public interface DeptConfigService extends IService<DeptAuthorition> {

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptAuthorition
     * @return
     */
    IPage<DeptAuthorition> findPage(Page<DeptAuthorition> page, DeptAuthorition deptAuthorition);

    /**
     * 查询所有的部门列表
     * @return
     */
    List<DeptAuthorition> getListDept();

    /**
     * 获取用户的所有权限
     * @param userId
     * @return
     */
    List<String> getPowerByUserId(Integer userId);

}
