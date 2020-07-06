package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.Car;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 批量删除对应车辆信息 假删除
     * @param collect
     * @return
     */
    boolean updateBatchesState(@Param("idList") List<Integer> collect);
}
