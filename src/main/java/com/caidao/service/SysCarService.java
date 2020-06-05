package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.Car;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
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

    /**
     * 删除信息 真删除
     * @param cars
     * @return
     */
    boolean batchRemoveByIds(List<Car> cars);
}
