package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptAuthorisation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptConfigMapper extends BaseMapper<DeptAuthorisation> {

    /**
     * 通过用户Id获取用户的权限
     * @param userId
     * @return
     */
    @Select("SELECT config_id from dept_role_authorisation WHERE dept_id = (SELECT user_dept_id FROM dept_user WHERE user_id = #{userId}) AND role_id = (SELECT role_id FROM dept_user_role WHERE user_id = #{userId})")
    List<Integer> getUserPowerIdsWithUserId(@Param("userId") Integer userId);

    /**
     * 批量删除信息，假删除
     * @param idList
     * @return
     */
    boolean updateBatchesState(@Param("idList") Collection<? extends Serializable> idList);
}

