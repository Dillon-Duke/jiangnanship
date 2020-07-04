package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptUserCarApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-06
 */
@Repository
public interface DeptUserCarApplyMapper extends BaseMapper<DeptUserCarApply> {

    /**
     * 批量插入车辆用户绑定信息
     * @param deptUserCarApplies
     * @return
     */
    Boolean insertBatches(@Param("deptUserCars") List<DeptUserCarApply> deptUserCarApplies);

    /**
     * 通过业务Id更换司机
     * @param businessKey
     * @param driverId
     * @param driverName
     * @return
     */
    @Update("UPDATE dept_user_car SET driver_id = #{driverId} , driver_name = #{driverName} WHERE business_key = #{businessKey}")
    Integer updateDriverWithBusinessKey(@Param("businessKey") Integer businessKey, @Param("driverId") Integer driverId, @Param("driverName") String driverName);

}
