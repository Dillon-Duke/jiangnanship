package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.SysRoleMenu;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 批量新增角色菜单
     * @param roleMenus
     * @return
     */
    Boolean insertBatches(List<SysRoleMenu> roleMenus);
}
