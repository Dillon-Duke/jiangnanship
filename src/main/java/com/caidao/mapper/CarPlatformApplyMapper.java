package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.CarPlatformApply;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-30
 */
@Repository
public interface CarPlatformApplyMapper extends BaseMapper<CarPlatformApply> {

    /**
     * 获取当前时间内有任务的车辆
     * @return
     * @param now
     */
    @Select("SELECT car_id FROM car_platform_apply WHERE start_time <= #{now} AND end_time >= #{now}")
    List<Integer> selectTaskCarList(@Param("now") LocalDateTime now);

    /**
     * 批量的进行车辆和任务进行绑定
     * @param platformApplies
     * @return
     */
    Boolean insertBatches(List<CarPlatformApply> platformApplies);

    /**
     * 通过车辆Id和业务Id批量删除记录
     * @param cancelBusinessKey
     * @param cancelCarIds
     * @return
     */
    @Delete("DELETE FROM car_platform_apply WHERE car_id IN #{cancelCarIds} AND prs_id = #{cancelBusinessKey}")
    Boolean deleteBatchCarIdAndPrsId(@Param("cancelBusinessKey") String cancelBusinessKey, @Param("cancelCarIds") String[] cancelCarIds);

    /**
     * 查询该车辆所有的任务
     * @param carId
     * @return
     */
    @Select("SELECT * FROM car_platform_apply where car_id IN ( #{carId} ) ")
    List<CarPlatformApply> selectBatchCarIds(@Param("carId") String[] carId);
}
