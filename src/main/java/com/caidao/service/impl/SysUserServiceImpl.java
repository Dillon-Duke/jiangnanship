package com.caidao.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.config.BackUserRealmConfig;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.SysUserMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.param.UserParam;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.SysUser;
import com.caidao.pojo.SysUserRole;
import com.caidao.service.SysUserService;
import com.caidao.util.EntityUtils;
import com.caidao.util.Md5Utils;
import com.caidao.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	private static final String IS_CREATE_DEPARTMENT_USER = "true";

	@Autowired
	private SysUserMapper sysUserMapper;

	@Autowired
	private DeptUserMapper deptUserMapper;

	@Autowired
	private Jedis jedis;

	@Autowired
	@Lazy
	private BackUserRealmConfig backUserRealmConfig;
	
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
		Assert.notNull(page,"用户属性不能为空");
		log.info("查询用户页面当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<SysUser> usersPage = sysUserMapper.selectPage(page, new LambdaQueryWrapper<SysUser>()
				.eq(SysUser::getState,1)
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
		Assert.notNull(sysUser,"新增用户属性不能为空");
		log.info("新增用户名为{}的用户",sysUser.getUsername());
		SysUser sysUser2 = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysUser.setCreateId(sysUser2.getUserId());
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
		//将密码设置为盐值密码
		String saltPassword = Md5Utils.getHashAndSaltAndTime(sysUser.getPassword(), salt, 1024);
		sysUser.setPassword(saltPassword);
		boolean save = super.save(sysUser);
		//判断是否需要创建部门角色
		String userAdd = sysUser.getDeptUserAdd();
		if (IS_CREATE_DEPARTMENT_USER.equals(userAdd)){
			//判断是否昵称已被注册
			DeptUser selectOne = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
					.eq(DeptUser::getUsername, sysUser.getUsername()));
			if (selectOne != null){
				throw new MyException("该名称已被注册，请更换其他名称");
			}
			DeptUser deptUser = new DeptUser();
			deptUser.setSysUserId(sysUser.getUserId());
			deptUser.setUsername(sysUser.getUsername());
			deptUser.setRealName(sysUser.getRealName());
			deptUser.setPassword(sysUser.getPassword());
			deptUser.setUserSalt(sysUser.getUserSalt());
			deptUser.setAge(sysUser.getAge());
			deptUser.setSex(sysUser.getSex());
			deptUser.setPhone(sysUser.getPhone());
			deptUser.setUserRoleName("无");
			deptUser.setUserDeptName("无");
			deptUser.setCreateId(sysUser.getCreateId());
			deptUser.setCreateDate(LocalDateTime.now());
			deptUser.setState(1);
			deptUserMapper.insert(deptUser);
		}
		//设置用户角色列表
		List<Integer> roleIds = sysUser.getRoleIdList();
		if (roleIds == null || roleIds.isEmpty()) {
			return save;
		}
		List<SysUserRole> userRoles = roleIds.stream().map((x) -> EntityUtils.getSysUserRole(sysUser.getUserId(),x)).collect(Collectors.toList());
		return sysUserRoleMapper.insertBatches(userRoles);
	}

	/**
	 * 批量移除用户
	 * @param idList
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = NullPointerException.class)
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		Assert.notNull(idList,"用户IDs不能为空");
		log.info("删除用户ID为{}的用户",idList);
		Integer result = sysUserMapper.updateBatchesState(idList);
		if (result == 0) {
			return false;
		}
		return true;
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
		String saltPass = Md5Utils.getHashAndSaltAndTime(userParam.getCredentials(), sysUser.getUserSalt(), 1024);
		String oldPassword = sysUser.getPassword();
		//判断老密码是否正确
		Assert.isTrue(oldPassword.equals(saltPass),"原密码不正确");
		//设置新密码
		sysUser.setPassword(userParam.getNewCredentials());
		String saltPassword = Md5Utils.getHashAndSaltAndTime(sysUser.getPassword(), sysUser.getUserSalt(), 1024);
		sysUser.setPassword(saltPassword);
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
		Assert.notNull(id,"用户ID不能为空");
		log.info("查询用户ID为{}的用户",id);
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
		List<Integer> roleIdList = selectObjs.stream().map((x) -> Integer.parseInt(x.toString())).collect(Collectors.toList());
		sysUser.setRoleIdList(roleIdList);
		return sysUser;
	}
	
	/**
	 * 更新用户
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(SysUser sysUser) {
		Assert.notNull(sysUser,"用户信息不能为空");
		log.info("修改用户名为{}的用户",sysUser.getUsername());
		//设置更新人id
		SysUser sysUser2 = (SysUser)SecurityUtils.getSubject().getPrincipal();
		sysUser.setUpdateId(sysUser2.getUserId());
		sysUser.setUpdateDate(LocalDateTime.now());
		//获取表中的有相同名字的元数据
		SysUser sysUser1 = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
				.eq(SysUser::getUsername, sysUser.getUsername()));
		//判断表中是否有该名字
		if (sysUser1.getUsername() ==sysUser.getUsername() && !sysUser.getUserId().equals(sysUser.getUserId())) {
			throw new MyException("该名称已被注册，请更换其他名称");
		}
		//设置盐值密码
		if (sysUser1.getPassword() != sysUser.getPassword()) {
			sysUser.setPassword(Md5Utils.getHashAndSaltAndTime(sysUser.getPassword(),sysUser1.getUserSalt(),1024));
		}
		//后台更新用户
		boolean updateById = super.updateById(sysUser);
		//删除之前的用户角色对应之后重新更新
		sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, sysUser.getUserId()));
		List<Integer> roleIdList = sysUser.getRoleIdList();
		if (roleIdList == null || roleIdList.isEmpty()) {
			return true;
		}
		//批量插入更新的用户角色表
		List<SysUserRole> userRoles = roleIdList.stream().map((x) -> EntityUtils.getSysUserRole(sysUser.getUserId(),x)).collect(Collectors.toList());
		boolean batches = sysUserRoleMapper.insertBatches(userRoles);
		//获取被删除用户的token
		Object token = jedis.hget(PropertyUtils.ALL_USER_TOKEN, sysUser.getUserSalt());
		//判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
		if (token != null) {
			//删除用户在hash里面的token
			jedis.hdel(PropertyUtils.ALL_USER_TOKEN, sysUser.getUserSalt());
			//删除用户的权限缓存
			backUserRealmConfig.clearCache();
			//删除用户的session缓存
			jedis.del(PropertyUtils.USER_SESSION+token);
		}
		return batches;
	}

}
