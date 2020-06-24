package com.caidao.mapper;

import com.caidao.pojo.DeptUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Repository
public interface DeptUserRoleMapper extends BaseMapper<DeptUserRole> {

    /**
     * 批量插入用户角色中间表
     * @param deptUserRoles
     * @return
     */
    Boolean insertBatches(List<DeptUserRole> deptUserRoles);
}
