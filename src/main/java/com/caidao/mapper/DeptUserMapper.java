package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Repository
public interface DeptUserMapper extends BaseMapper<DeptUser> {

    /**
     * 查询新增用户对应的角色和部门信息
     * @param integer
     * @return
     */
    Map<String, Object> selectDeptRole(@Param("integer") Integer integer);

    /**
     * 忘记密码更新用户的密码
     * @param userId
     * @param password
     * @return 返回的是更新部门用户的条数
     */
    @Update("update dept_user set password = #{password} where user_id = #{userId}")
    Integer updateUserPasswordByUserId(@Param("userId") Integer userId, @Param("password") String password);

    /**
     * 通过业务主键获取申请人姓名
     * @param businessKey
     * @return
     */
    @Select("SELECT D.username FROM dept_user D LEFT JOIN platform_apply P ON P.create_id = D.user_id WHERE P.prs_id = #{businessKey}")
    String selectApplyNameWithApplyId(@Param("businessKey") String businessKey);
}

