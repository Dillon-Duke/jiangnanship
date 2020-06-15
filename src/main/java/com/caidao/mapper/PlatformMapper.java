package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.Platform;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author Dillon
 * @since 2020-06-11
 */
public interface PlatformMapper extends BaseMapper<Platform> {

    /**
     * 用户拾取组任务
     * @param username
     * @return
     */
    @Update("UPDATE platform_plan SET apply_name = #{username} WHERE prs_id = #{businessKey}")
    Integer updateApplyName(@Param("businessKey") String businessKey, @Param("username") String username);

    /**
     *
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_plan SET apply_name = NULL , apply_state = 3 WHERE prs_id = #{businessKey}")
    Integer setApprovalOpinion(String businessKey);

    /**
     * 计划任务的完成
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_plan SET apply_name = NULL , apply_state = 2 WHERE prs_id = #{businessKey}")
    Integer endFlatCarPlanTask(String businessKey);
}
