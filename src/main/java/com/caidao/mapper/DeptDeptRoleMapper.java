package com.caidao.mapper;

import com.caidao.pojo.DeptDeptRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptDeptRoleMapper extends BaseMapper<DeptDeptRole> {
    /**
     * 批量新增部门角色中间表
     * @param deptDeptRoles
     * @return
     */
    Boolean insertBatches(List<DeptDeptRole> deptDeptRoles);
}
