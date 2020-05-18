package com.caidao.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysUser;
import com.caidao.entity.SysUserRole;
import com.caidao.mapper.SysUserMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.service.SysUserService;
import com.caidao.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.lang.model.type.IntersectionType;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	@Autowired
	private SysUserMapper sysUserMapper;
	
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	
	@Override
	public SysUser getUserByUsername(String username) {
		Assert.notNull(username, "用户名不能为空");
		log.info("查询用户名为{}的用户",username);
		SysUser sysUser = sysUserMapper
				.selectOne(new LambdaQueryWrapper<SysUser>()
				.eq(SysUser ::getUsername ,username));
		return sysUser;
	}

	/**
	 * 获取查询用户的当前页 和页大小
	 */
	@Override
	public IPage<SysUser> getUserPage(Page<SysUser> page, SysUser sysUser) {
		log.info("当前页{}，页大小{}",page.getSize(),page.getCurrent());
		IPage<SysUser> usersPage = sysUserMapper.selectPage(page, new LambdaQueryWrapper<SysUser>()
				.like(StringUtils.hasText(sysUser.getUsername()),SysUser::getUsername,sysUser.getUsername())
		.ne(SysUser::getUsername,"admin")
		.eq(SysUser::getState,1));
		return usersPage;
	}

	/**
	 * 新增用户时新增用户角色中间表
	 */
	@Override
	public boolean save(SysUser sysUser) {
		
		Assert.notNull(sysUser, "sysUser must not be null");
		log.info("新增用户{}",sysUser);
		sysUser.setCreateDate(LocalDateTime.now());

		String salt = UUID.randomUUID().toString();
		sysUser.setUserSalt(salt);
		sysUser.setState(1);

		//自定义工号
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		sysUser.setJobNum(Integer.valueOf(format.format(new Date())+(int)(Math.random()*9000+1000)));
		
		//盐值更新
		String password = sysUser.getPassword();
		ByteSource bytes = ByteSource.Util.bytes(salt.getBytes());
		String shiroPasswd = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
		sysUser.setPassword(shiroPasswd);

		boolean save = super.save(sysUser);
	
		//判断新增用户必须传值条件 如果没有，报异常 
		Assert.state(sysUser!=null
				&& sysUser.getUsername()!=null
				&& sysUser.getPassword()!=null,
				"新增用户失败");

		List<Integer> roleIds = sysUser.getRoleIdList();
		if (roleIds == null || roleIds.isEmpty()) {
			return save;
		}
		for (Integer roleId : roleIds) {
			SysUserRole sysUserRole = new SysUserRole();
			sysUserRole.setUserId(sysUser.getUserId());
			sysUserRole.setRoleId(roleId);
			sysUserRoleMapper.insert(sysUserRole);
		}
		return save;
	}
	
	/**
	 * 批量移除用户
	 * 假删除
	 */
	@Override
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		log.info("批量移除id为{}的用户",idList);

		boolean removeByIds = super.removeByIds(idList);
		if (idList==null || idList.isEmpty()) {
			throw new NullPointerException("批量删除用户不能为空");
		}
		return removeByIds;
	}

	/**
	 * 批量删除用户
	 * 真删除
	 * @param ids
	 */
	@Override
	public void deleteByIds(List<Integer> ids) {

		//批量更新 设置状态为0
//		List<SysUser> sysUsers = new ArrayList<>();
//		for (Integer id : ids) {
//			SysUser user = new SysUser();
//			user.setUserId(id);
//			user.setState(0);
//			sysUsers.add(user);
//		}
//
//		//将主键传到mapper sql批量删除
//		this.updateBatchById(sysUsers,100);

		sysUserMapper.batchDelete(ids);
	}
	
	/**
	 * 编辑之前查询用户对应的角色
	 */
	@Override
	public SysUser getById(Serializable id) {
		log.info("查询id为{}的用户所存在的角色",id);
		if (id == null || id=="") {
			throw new NullPointerException("id为空");
		}
		SysUser sysUser = super.getById(id);
		Integer userId = sysUser.getUserId();
		List<Object> selectObjs = sysUserRoleMapper.selectObjs(new LambdaQueryWrapper<SysUserRole>()
														.select(SysUserRole::getRoleId)
														.eq(SysUserRole::getUserId, userId));
		if (selectObjs == null || selectObjs.isEmpty()) {
			return sysUser;
		}
		
		List<Integer> roleIdList = new ArrayList<Integer>(selectObjs.size());
		for (Object object : selectObjs) {
			roleIdList.add(Integer.valueOf(object.toString()));
		}
		sysUser.setRoleIdList(roleIdList);
		return sysUser;
	}
	
	/**
	 * 更新用户
	 */
	@Override
	public boolean updateById(SysUser sysUser) {
		log.info("更新用户为{}的用户",sysUser.getUsername());
		
		//设置更新盐值
		String password = sysUser.getPassword();
		ByteSource bytes = ByteSource.Util.bytes(sysUser.getUserSalt());
		String shiroPasswd = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
		sysUser.setPassword(shiroPasswd);

		//设置更新时间
		sysUser.setUpdateDate(LocalDateTime.now());
		
		boolean updateById = super.updateById(sysUser);
		Assert.state(sysUser!=null
				&& sysUser.getPassword()!=null
				&& sysUser.getUsername()!=null,
				"更新用户失败，请查找传值信息");
		
		//删除之前的用户角色对应之后重新更新
		sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, sysUser.getUserId()));
		
		List<Integer> roleIdList = sysUser.getRoleIdList();
		if (roleIdList == null || roleIdList.isEmpty()) {
			return updateById;
		}
		for (Integer idList : roleIdList) {
			SysUserRole sysUserRole = new SysUserRole();
			sysUserRole.setUserId(sysUser.getUserId());
			sysUserRole.setRoleId(idList);
			sysUserRoleMapper.insert(sysUserRole);
		}
		
		return updateById ;
	}
	
}
