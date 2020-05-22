package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.Car;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
public interface SysCarService extends IService<Car> {

    /**
     * 查询车辆分页数据
     * @param page
     * @param car
     * @return
     */
    IPage<Car> findSysCarPage(Page<Car> page, Car car);
}
