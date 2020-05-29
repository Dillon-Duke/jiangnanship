package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.entity.DeptUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-28
 */
public interface DeptUserMapper extends BaseMapper<DeptUser> {

    Map<String, Object> selectDeptRole(@PathVariable("integer") Integer integer);

    /**
     * 忘记密码更新用户的密码
     * @param deptUser
     */
    @Update("update dept_user set password = #{deptUser.password} where user_id = #{deptUser.userId}")
    Integer updatePassById(@Param("deptUser") DeptUser deptUser);
}
