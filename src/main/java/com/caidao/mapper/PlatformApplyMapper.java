package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.PlatformApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    @Update("UPDATE platform_apply SET apply_name = NULL , apply_state = 3 WHERE prs_id = #{businessKey}")
    Integer fileEndFlatCarPlanTask(Integer businessKey);

    /**
     * 计划任务的完成
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = NULL , apply_state = 2 WHERE prs_id = #{businessKey}")
    Integer successEndFlatCarPlanTask(Integer businessKey);

    /**
     * 获得候选人的列表
     * @param name
     * @param instanceId
     * @return
     */
    @Select("SELECT TEXT_ FROM ACT_RU_VARIABLE WHERE NAME_ = #{name} AND PROC_INST_ID_ = #{instanceId}")
    String getCandidate(@Param("name") String name, @Param("instanceId") String instanceId);

    /**
     * 查询流程中所有人已参与或者将参与人员的信息
     * @param instanceId
     * @return
     */
    @Select("SELECT TEXT_ FROM ACT_RU_VARIABLE WHERE PROC_INST_ID_ = #{instanceId} AND NAME_ LIKE '%GroupTask%'")
    List<String> getPlatCarApplyAndApprovalUsers(@Param("instanceId") String instanceId);

    /**
     * 更新申请状态为取消
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_state = 4 WHERE prs_id = #{businessKey}")
    Integer updateApplyState(@Param("businessKey") String businessKey);

    /**
     * 更新物件绑定信息为未绑定
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_goods G SET G.is_binder = 0 WHERE G.goods_id = (SELECT A.object_id FROM platform_apply A WHERE A.prs_id = #{businessKey})")
    Integer updateGoodsBindState(@Param("businessKey") String businessKey);

    /**
     * 更新审批人为null
     * @param businessKey
     * @return
     */
    @Update("UPDATE platform_apply SET apply_name = null WHERE prs_id = {businessKey}")
    Integer updateApplyNameAsNull(@Param("businessKey") String businessKey);
}
