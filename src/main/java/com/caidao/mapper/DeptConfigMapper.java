package com.caidao.mapper;

import com.caidao.pojo.DeptAuthorition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptConfigMapper extends BaseMapper<DeptAuthorition> {

    /**
     * 获取用户的权限
     * @param userId
     * @return
     */
    @Select("SELECT config_id from dept_role_config WHERE dept_id = (SELECT user_dept_id FROM dept_user WHERE user_id = #{userId}) AND role_id = (SELECT role_id FROM dept_user_role WHERE user_id = #{userId})")
    List<Integer> getpowerids(@Param("userId") Integer userId);
}
