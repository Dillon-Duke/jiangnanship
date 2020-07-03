package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptUserCar;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-06
 */
@Repository
public interface DeptUserCarMapper extends BaseMapper<DeptUserCar> {

    /**
     * 批量插入车辆用户绑定信息
     * @param deptUserCars
     * @return
     */
    Boolean insertBatches(@Param("deptUserCars") List<DeptUserCar> deptUserCars);

    /**
     * 通过业务主键获取对应的任务Id
     * @param businessKey
     * @return
     */
    @Select("SELECT T.ID_ FROM ACT_RU_TASK T LEFT JOIN ACT_RU_EXECUTION E ON T.PROC_INST_ID_ = E.PROC_INST_ID_ WHERE E.BUSINESS_KEY_ = #{businessKey}")
    String selectTaskIdByBusinessKey(@Param("businessKey") Integer businessKey);

    /**
     * 更换司机
     * @param businessKey
     * @param driverId
     * @param driverName
     * @return
     */
    @Update("UPDATE dept_user_car SET driver_id = #{driverId} , driver_name = #{driverName} WHERE business_key = #{businessKey}")
    Integer updateDriver(@Param("businessKey") Integer businessKey, @Param("driverId") Integer driverId, @Param("driverName") String driverName);

}
