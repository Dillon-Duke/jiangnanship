package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysCar;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysRole;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
public interface SysCarService extends IService<SysCar> {

    /**
     * 查询车辆分页数据
     * @param page
     * @param sysCar
     * @return
     */
    IPage<SysCar> findSysCarPage(Page<SysCar> page, SysCar sysCar);
}
