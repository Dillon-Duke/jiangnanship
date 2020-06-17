package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.Car;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author Dillon
 * @since 2020-05-18
 */
@Repository
public interface CarMapper extends BaseMapper<Car> {

    /**
     * 查询数据库可用车辆
     * @return
     */
    @Select("SELECT count(1) FROM car where car_state = 1")
    Integer getCarCount();

    /**
     * 车辆与任务做绑定 多太车与一个任务作为绑定
     * @param substring
     * @param taskId
     * @return
     */
    @Update("UPDATE car SET bind_task_id = ${taskId} WHERE car_id IN (${substring})")
    Integer saveOrBindTaskWithCar(@Param("substring") String substring, @Param("taskId") String taskId);
}
