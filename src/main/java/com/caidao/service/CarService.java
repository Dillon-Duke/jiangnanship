package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.Car;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-18
 */
public interface CarService extends IService<Car> {

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

    /**
     * 查询数据库可用车辆
     * @return
     */
    Integer getCarCount();

    /**
     * 根据条件查询可用车辆信息
     * @return
     */
    List<Car> selectConditionCar(Car car);

    /**
     * 获得空闲的车辆
     * @return
     */
    List<Car> getFreeCarList();

    /**
     * 车辆与任务做绑定 多太车与一个任务作为绑定
     * @param carId
     * @param taskId
     * @return
     */
    void saveOrBindTaskWithCar(List<String> carId, String taskId);
}
