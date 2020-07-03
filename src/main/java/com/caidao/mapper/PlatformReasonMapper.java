package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.PlatformReason;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author Dillon
 * @since 2020-06-13
 */
@Repository
public interface PlatformReasonMapper extends BaseMapper<PlatformReason> {

    /**
     * 通过业务Id获取任务Id
     * @param adjustmentBusinessKey
     * @return
     */
    @Select("SELECT T.ID_ FROM ACT_RU_TASK T LEFT JOIN ACT_RU_EXECUTION E ON T.PROC_INST_ID_ = E.PROC_INST_ID_ WHERE E.BUSINESS_KEY_ = #{adjustmentBusinessKey}")
    String getTaskIdByBusinessKey(@Param("adjustmentBusinessKey") Integer adjustmentBusinessKey);

    /**
     * 更新坐标Id和坐标Gps信息
     * @param endPositionId
     * @param endPositionGps
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET end_position_id = #{endPositionId} , end_position_gps = #{endPositionGps} WHERE prs_id = #{businessKey}")
    Integer updateDestination(@Param("endPositionId") Long endPositionId, @Param("endPositionGps") String endPositionGps, @Param("businessKey") Integer businessKey);

}
