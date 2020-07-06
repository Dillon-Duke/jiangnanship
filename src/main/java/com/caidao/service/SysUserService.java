package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.UserParam;
import com.caidao.pojo.SysUser;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysUserService extends IService<SysUser> {

	/**
	 * 通过用户名获取用户
	 * @param username
	 * @return
	 */
	SysUser getUserByUsername(String username);

	/**
	 * 获取用户分页数据
	 * @param page
	 * @param sysUser
	 * @return
	 */
	IPage<SysUser> getUserPage(Page<SysUser> page, SysUser sysUser);

	/**
	 * 更新自己的密码
	 * @param sysUser
	 * @param userParam
	 * @return
	 */
    int updatePass(SysUser sysUser, UserParam userParam);
}
