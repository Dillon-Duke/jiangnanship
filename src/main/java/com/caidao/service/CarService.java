package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.Car;
import com.caidao.pojo.DeptUserCarApply;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * @param car
     * @return
     */
    List<Car> selectConditionCar(Car car);

    /**
     * 车辆与任务做绑定 多太车与一个任务作为绑定
     * @param deptUserCarApplies
     * @return
     */
    Boolean saveOrBindTaskWithCar(List<DeptUserCarApply> deptUserCarApplies);

    /**
     * 获得空闲的、在使用的车辆已经对应的司机信息
     * @return
     */
    Map<String, Object> getAllAndFreeCarWithDrivers();

    /**
     * 获得所有的车辆信息，有车辆任务的显示车辆的任务，没有车辆任务的显示为空闲车辆
     * @param date
     * @return
     */
    Map<String, Object> getAllCarsWithHaveTasksAndNoTasks(LocalDateTime date);

    /**
     * 将已经绑定车辆的任务进行排序
     * @return
     */
    void sortBindApplyTasks(Integer businessKey);

    /**
     * 自动绑定车辆与申请的关系
     * @return
     */
    void autoCompareCarWithApply();
}
