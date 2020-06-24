package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.SysUserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 批量新增用户角色中间表
     * @param userRoles
     * @return
     */
    Boolean insertBatches(List<SysUserRole> userRoles);
}
