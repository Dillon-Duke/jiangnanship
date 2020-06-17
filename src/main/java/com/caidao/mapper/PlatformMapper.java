package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.PlatformApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author tom
 * @since 2020-06-11
 */
@Repository
public interface PlatformMapper extends BaseMapper<PlatformApply> {

    /**
     *
     * 用户拾取组任务
     * @param username
     * @return
     */
    @Update("UPDATE platform_plan SET apply_name = #{username} , update_id = #{updateId} WHERE prs_id = #{businessKey}")
    Integer updateApplyName(@Param("businessKey") Integer businessKey, @Param("username") String username, @Param("updateId") Integer updateId);

    /**
     *
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET apply_name = NULL , apply_state = 3 WHERE prs_id = #{businessKey}")
    Integer setApprovalOpinion(Integer businessKey);

    /**
     *
     * 计划任务的完成
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET apply_name = NULL , apply_state = 2 WHERE prs_id = #{businessKey}")
    Integer endFlatCarPlanTask(Integer businessKey);

    /**
     * 在表中标记改该记录为编制驳动计划
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET reserve1 = 1 WHERE prs_id = #{businessKey}")
    Integer remarkOrganization(Integer businessKey);
}
