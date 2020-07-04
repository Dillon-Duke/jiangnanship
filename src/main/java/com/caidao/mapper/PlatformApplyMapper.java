package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.PlatformApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author tom
 * @since 2020-06-11
 */
@Repository
public interface PlatformApplyMapper extends BaseMapper<PlatformApply> {

    /**
     * 用户拾取组任务,更新审批人到申请表中
     * @param username
     * @param businessKey
     * @param updateId
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = #{username} , update_id = #{updateId} WHERE prs_id = #{businessKey}")
    Integer updateTaskApprovalName(@Param("businessKey") Integer businessKey, @Param("username") String username, @Param("updateId") Integer updateId);

    /**
     * 计划任务的完成 更新审批单中完成状态
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = NULL , apply_state = 2 WHERE prs_id = #{businessKey}")
    Integer updatePlatformApplyStateToSuccess(Integer businessKey);

    /**
     * 更新申请状态为取消
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_state = 4 WHERE prs_id = #{businessKey}")
    Integer updateApplyStateToCancel(@Param("businessKey") String businessKey);

    /**
     * 更新物件绑定信息为未绑定
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_goods G SET G.is_binder = 0 WHERE G.goods_id = (SELECT A.object_id FROM platform_apply A WHERE A.prs_id = #{businessKey})")
    Integer updateGoodsBindStateToUnbind(@Param("businessKey") String businessKey);

    /**
     * 更新审批人为null
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = null WHERE prs_id = {businessKey}")
    Integer updateApplyNameAsNull(@Param("businessKey") String businessKey);

    /**
     * 更新申请的绑定开始时间和结束时间
     * @param businessKey
     * @param applyStartTime
     * @param applyEndTime
     * @return
     */
    @Update("UPDATE platform_apply SET start_time = #{applyStartTime} , end_time = #{applyEndTime} WHERE prs_id = {businessKey}")
    Integer updateApplyStartTimeAndEndTimeWithApplyId(@Param("businessKey") String businessKey, @Param("applyStartTime") LocalDateTime applyStartTime, @Param("applyEndTime") LocalDateTime applyEndTime);

    /**
     * 更新绑定物品的运输重要程度
     * @param businessKey
     * @param applyIsImportant
     * @return
     */
    @Update("UPDATE platform_apply SET importance = #{applyIsImportant} WHERE prs_id = {businessKey}")
    Integer updateApplyImportantWithApplyId(@Param("businessKey") String businessKey, @Param("applyIsImportant") Integer applyIsImportant);

    /**
     * 更新申请的绑定开始时间和结束时间和物品的运输重要程度
     * @param businessKey
     * @param applyStartTime
     * @param applyEndTime
     * @param applyIsImportant
     * @return
     */
    @Update("UPDATE platform_apply SET start_time = #{applyStartTime} , end_time = #{applyEndTime} , importance = #{applyIsImportant} WHERE prs_id = {businessKey}")
    Integer updateApplyImportantAndApplyStartTimeAndEndTimeWithApplyId(@Param("businessKey") String businessKey, @Param("applyStartTime") LocalDateTime applyStartTime, @Param("applyEndTime") LocalDateTime applyEndTime, @Param("applyIsImportant") Integer applyIsImportant);

    /**
     * 更新坐标Id和坐标Gps信息
     * @param endPositionId
     * @param endPositionGps
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET end_position_id = #{endPositionId} , end_position_gps = #{endPositionGps} WHERE prs_id = #{businessKey}")
    Integer updateDestinationIdAndDestinationGpsWithApplyId(@Param("endPositionId") Long endPositionId, @Param("endPositionGps") String endPositionGps, @Param("businessKey") Integer businessKey);
}
