package com.caidao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tom
 */
@Repository
public interface CustomActivitiMapper {

    /**
     * 通过业务主键获取对应的任务Id
     * @param businessKey
     * @return
     */
    @Select("SELECT T.ID_ FROM ACT_RU_TASK T LEFT JOIN ACT_RU_EXECUTION E ON T.PROC_INST_ID_ = E.PROC_INST_ID_ WHERE E.BUSINESS_KEY_ = #{businessKey}")
    String selectTaskIdWithBusinessKey(@Param("businessKey") Integer businessKey);

    /**
     * 查询流程中所有人已参与或者将参与任务审批人员的信息
     * @param instanceId
     * @return
     */
    @Select("SELECT TEXT_ FROM ACT_RU_VARIABLE WHERE PROC_INST_ID_ = #{instanceId} AND NAME_ LIKE '%GroupTask%'")
    List<String> getPlatCarApplyAndApprovalUsers(@Param("instanceId") String instanceId);

    /**
     * 获得工作流中候选人的列表
     * @param name
     * @param instanceId
     * @return
     */
    @Select("SELECT TEXT_ FROM ACT_RU_VARIABLE WHERE NAME_ = #{name} AND PROC_INST_ID_ = #{instanceId}")
    String getCandidateUsersInActivitiTables(@Param("name") String name, @Param("instanceId") String instanceId);

    /**
     * 通过业务Id获取任务Id
     * @param adjustmentBusinessKey
     * @return
     */
    @Select("SELECT T.ID_ FROM ACT_RU_TASK T LEFT JOIN ACT_RU_EXECUTION E ON T.PROC_INST_ID_ = E.PROC_INST_ID_ WHERE E.BUSINESS_KEY_ = #{adjustmentBusinessKey}")
    String getTaskIdByBusinessKey(@Param("adjustmentBusinessKey") Integer adjustmentBusinessKey);

}
