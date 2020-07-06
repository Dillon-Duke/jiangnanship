package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.DeptAuthorisation;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-27
 */
public interface DeptConfigService extends IService<DeptAuthorisation> {

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptAuthorisation
     * @return
     */
    IPage<DeptAuthorisation> findPage(Page<DeptAuthorisation> page, DeptAuthorisation deptAuthorisation);

    /**
     * 查询所有的部门列表
     * @return
     */
    List<DeptAuthorisation> getListDept();


    List<String> getPowerByUserId(Integer userId);
}
