package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.PlatformApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * @author tom
 * @since 2020-06-11
 */
@Repository
public interface PlatformApplyMapper extends BaseMapper<PlatformApply> {

    /**
     * 用户拾取组任务
     * @param username
     * @param businessKey
     * @param updateId
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = #{username} , update_id = #{updateId} WHERE prs_id = #{businessKey}")
    Integer updateApplyName(@Param("businessKey") Integer businessKey, @Param("username") String username, @Param("updateId") Integer updateId);

    /**
     * 设置审批不同意，设置状态为不同意
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET apply_name = NULL , apply_state = 3 WHERE prs_id = #{businessKey}")
    Integer fileEndFlatCarPlanTask(Integer businessKey);

    /**
     * 计划任务的完成
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET apply_name = NULL , apply_state = 2 WHERE prs_id = #{businessKey}")
    Integer successEndFlatCarPlanTask(Integer businessKey);

    /**
     * 在表中标记改该记录为编制驳动计划
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform SET reserve1 = 1 WHERE prs_id = #{businessKey}")
    Integer remarkOrganization(Integer businessKey);

    /**
     * 获得候选人的列表
     * @param name
     * @param instanceId
     */
    @Select("SELECT TEXT_ FROM ACT_RU_VARIABLE WHERE NAME_ = #{name} AND PROC_INST_ID_ = #{instanceId}")
    String getcandidate(@Param("name") String name, @Param("instanceId") String instanceId);
}
