package com.caidao.mapper;

import com.caidao.entity.Car;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Dillon
 * @since 2020-05-18
 */
public interface SysCarMapper extends BaseMapper<Car> {

    /**
     * 查询数据库可用车辆
     * @return
     */
    @Select("SELECT count(1) FROM car where car_state = 1")
    Integer getCarCount();
}
