package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysRole;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysRoleService extends IService<SysRole> {

	/**
	 * 获取角色分页
	 * @param page
	 * @param sysRole
	 * @return
	 */
	IPage<SysRole> findSysRolePage(Page<SysRole> page, SysRole sysRole);


}
