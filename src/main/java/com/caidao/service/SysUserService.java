package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysUser;
import com.caidao.param.UsernamePasswordParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
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
	 * 批量删除用户
	 * @param ids
	 */
    void deleteByIds(List<Integer> ids);

	/**
	 * 通过用户名和手机号判断用户是否存在
	 * @param username
	 * @param phone
	 * @return
	 */
    SysUser findUserByUsernameAndPhone(String username, String phone);

	/**
	 * 忘记密码，更新用户的密码
	 * @param sysUser
	 * @return
	 */
	boolean updatePassById(SysUser sysUser);

	/**
	 * 更新自己的密码
	 * @param sysUser
	 * @param usernamePasswordParam
	 * @return
	 */
    int updatePass(SysUser sysUser, UsernamePasswordParam usernamePasswordParam);
}
