package com.caidao.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysUser;
import com.caidao.entity.SysUserRole;
import com.caidao.exception.MyException;
import com.caidao.mapper.SysUserMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.param.UserParam;
import com.caidao.service.SysUserService;
import com.caidao.util.Md5Utils;
import com.caidao.util.PropertyUtils;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	@Autowired
	private SysUserMapper sysUserMapper;

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;

	/**
	 * 通过用户名查询用户
	 * @param username
	 * @return
	 */
	@Override
	public SysUser getUserByUsername(String username) {
		Assert.notNull(username, "用户名不能为空");
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
		IPage<SysUser> usersPage = sysUserMapper.selectPage(page, new LambdaQueryWrapper<SysUser>()
				.like(StringUtils.hasText(sysUser.getUsername()),SysUser::getUsername,sysUser.getUsername())
				.like(StringUtils.hasText(sysUser.getPhone()),SysUser::getPhone,sysUser.getPhone())
		.ne(SysUser::getUsername,"admin")
		.eq(SysUser::getState,1));
		return usersPage;
	}

	/**
	 * 新增用户时新增用户角色中间表
	 */
	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public boolean save(SysUser sysUser) {
		
		Assert.notNull(sysUser, "sysUser must not be null");

		//查询数据库中是否有该用户名，如果有，则提示更换用户名
		SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
				.eq(SysUser::getUsername, sysUser.getUsername()));
		if (user != null){
			throw new MyException("该名称已被注册，请更换其他名称");
		}
		sysUser.setCreateDate(LocalDateTime.now());

		String salt = UUID.randomUUID().toString().replaceAll("-","");
		sysUser.setUserSalt(salt);
		sysUser.setState(1);

		//自定义工号
		SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
		sysUser.setJobNum(Integer.valueOf(format.format(new Date())+(int)(Math.random()*9000+1000)));

		//将密码设置为盐值密码
		setSaltPass(sysUser, salt);

		boolean save = super.save(sysUser);

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
	 * 真删除
	 */
	@Override
	@Transactional(rollbackFor = NullPointerException.class)
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		boolean removeByIds = super.removeByIds(idList);
		if (idList==null || idList.isEmpty()) {
			throw new MyException("批量删除用户不能为空");
		}
		return removeByIds;
	}

	/**
	 * 批量删除用户
	 * 假删除
	 * @param ids
	 */
	@Override
	@Transactional(rollbackFor = RuntimeException.class)
	public void deleteByIds(List<Integer> ids) {

		//批量更新 设置状态为0
//		List<SysUser> sysUsers = new ArrayList<>();
//		for (Integer id : ids) {
//			SysUser user = new SysUser();
//			user.setUserId(id);
//			user.setState(0);
//			sysUsers.add(user);
//		}
//		//将主键传到mapper sql批量删除
//		this.updateBatchById(sysUsers,100);

		Integer integer = sysUserMapper.batchDelete(ids);
		if (integer == 0){
			throw new MyException("用户删除失败");
		}
	}

	/**
	 * 更新自己的密码
	 * @param sysUser
	 * @param userParam
	 * @return
	 */
	@Override
	public int updatePass(SysUser sysUser, UserParam userParam) {

		//获得加盐密码
		ByteSource bytes = ByteSource.Util.bytes(sysUser.getUserSalt().getBytes());
		String saltPass = Md5Utils.getHashAndSaltAndTime(userParam.getCredentials(), bytes, 1024);
		String oldPassword = sysUser.getPassword();
		//判断老密码是否正确
		Assert.isTrue(oldPassword.equals(saltPass),"原密码不正确");

		//设置新密码
		sysUser.setPassword(userParam.getNewCredentials());
		setSaltPass(sysUser,sysUser.getUserSalt());
		sysUser.setUpdateDate(LocalDateTime.now());
		sysUser.setUpdateId(sysUser.getUserId());
		int updateById = sysUserMapper.updateById(sysUser);
		return updateById;

	}

	/**
	 * 编辑之前查询用户对应的角色
	 */
	@Override
	public SysUser getById(Serializable id) {
		if (id == null || id=="") {
			throw new MyException("id为空");
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
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(SysUser sysUser) {

		//查询数据库中是否有该用户名，如果有，则提示更换用户名
		SysUser user = sysUserMapper.selectById(sysUser.getUserId());
		if (!user.getUsername().equals(sysUser.getUsername())){
			SysUser selectOne = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
					.eq(SysUser::getUsername, sysUser.getUsername()));
			if (selectOne != null){
				throw new MyException("该名称已被注册，请更换其他名称");
			}
		}

		//判断用户密码是否有改动过，如果有，则重新赋值 反之，则直接存入数据库
		SysUser user1 = sysUserMapper.selectById(sysUser.getUserId());
		if (user1.getPassword() != sysUser.getPassword()){
			//设置更新盐值
			setSaltPass(sysUser,sysUser.getUserSalt());
		}

		//设置更新时间
		sysUser.setUpdateDate(LocalDateTime.now());
		
		boolean updateById = super.updateById(sysUser);
		Assert.state(sysUser!=null && sysUser.getPassword()!=null && sysUser.getUsername()!=null, "更新用户失败，请查找传值信息");
		
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

		//获取对应的登录用户session
		String sessionKey = redisTemplate.opsForValue().get(PropertyUtils.USER_LOGIN_SESSION_ID+sysUser.getUsername());
		//判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
		if (sessionKey != null) {
			redisTemplate.delete(PropertyUtils.USER_SESSION+sessionKey);
			redisTemplate.delete(PropertyUtils.USER_LOGIN_SESSION_ID+sysUser.getUsername());
		}
		
		return updateById ;
	}

	/**
	 * 将密码设置为盐值密码
	 * @param sysUser
	 * @param salt
	 */
	private void setSaltPass(SysUser sysUser, String salt) {
		//盐值更新
		String password = sysUser.getPassword();
		ByteSource bytes = ByteSource.Util.bytes(salt.getBytes());
		String saltPass = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
		sysUser.setPassword(saltPass);
	}

}
