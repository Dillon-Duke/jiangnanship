package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.pojo.SysMenu;
import com.caidao.pojo.SysRoleMenu;
import com.caidao.pojo.SysUser;
import com.caidao.pojo.SysUserRole;
import com.caidao.exception.MyException;
import com.caidao.mapper.SysMenuMapper;
import com.caidao.mapper.SysRoleMenuMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.param.Menu;
import com.caidao.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
	
	@Autowired
	private SysMenuMapper sysMenuMapper;
	
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	
	@Autowired
	private SysRoleMenuMapper sysRoleMenuMapper;

	/**
	 * 通过用户获取菜单
	 */
	@Override
	public List<Menu> getMenuListByUserId(SysUser user) {

		//判断用户是否禁用
		if (user.getStatus() == 0){
			return null;
		}

		List<SysMenu> menuList = findMenuListByUserId(user.getUserId());
		if (menuList == null || menuList.isEmpty()) {
			return null;
		}
		
		List<Menu> rootone = new ArrayList<>();
		
		//设置一级树形菜单
		for (SysMenu sysMenu : menuList) {
			if (sysMenu.getParentId() == 0) {
				rootone.add(sysMenuService(sysMenu));
			}
		}
		
		//设置二级树形菜单
		for (SysMenu sysMenu : menuList) {
			for (Menu menu : rootone) {
				if (menu.getMenuId().equals(sysMenu.getParentId())) {
					menu.getList().add(sysMenuService(sysMenu));
				}
			}
		}
	return rootone;
	}

	private Menu sysMenuService(SysMenu sysMenu) {
		Menu menu = new Menu();
		menu.setMenuId(sysMenu.getMenuId());
		menu.setName(sysMenu.getName());
		menu.setUrl(sysMenu.getUrl());
		return menu;
	}

	/**
	 * 查询用户的按钮权限
	 * @param userId
	 * @return
	 * 开启缓存，因为是shiro的查询 所以缓存放在redis里面了
	 *@Cacheable(cacheNames="com.tencent.service.impl.SysMenuServiceImpl",key="#p0")
	 */
	@Override
	public List<String> getAuth2ByUslerId(Integer userId) {
		//获取登录用户的菜单id
		List<Object> menuIds = getMenuIds(userId);

		//判断角色是否为空
		if (menuIds == null){
			return null;
		}

		List<Object> selectObjs = sysMenuMapper.selectObjs(new LambdaQueryWrapper<SysMenu>()
														.select(SysMenu::getPerms)
														.in(SysMenu::getMenuId, menuIds)
														.isNotNull(SysMenu::getPerms)
														.ne(SysMenu::getPerms, ""));
		List<String> result = new ArrayList<String>();
		for (Object object : selectObjs) {
			String authorities = String.valueOf(object);
			String[] split = authorities.split(",");
			for (String string : split) {
				result.add(string);
			}
		}
		return result;
	}

	/**
	 * 通过用户ID获取菜单列表
	 * @param userId
	 * @return
	 */
	private List<SysMenu> findMenuListByUserId(Integer userId) {
		//获取登录用户的菜单id
		List<Object> menuIds = getMenuIds(userId);

		//判断用户如果是admin(菜单id为1) 则查询所有的菜单
		if (userId == 1) {
			List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
														.orderByAsc(SysMenu::getOrderNum));
			return selectList;
		}

		//判断登录人员是否有页面权限，没有直接返回null;
		if (menuIds == null){
			return null;
		}
		
		//查询对应的menu列表
		 List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
				 								.in(SysMenu::getMenuId, menuIds)
		 										.orderByAsc(SysMenu::getOrderNum));
		 return selectList;
	}

	/**
	 * 通过用户id获取用户对应的菜单id
	 */
	private List<Object> getMenuIds(Integer userId) {
		
		//判断用户如果是admin(菜单id为1) 则查询所有的菜单权限
		if (userId == 1) {
			List<Object> menuIds = sysMenuMapper.selectObjs(new LambdaQueryWrapper<SysMenu>()
										.select(SysMenu::getMenuId));
			return menuIds;
		}
		
		//查询对应的role  id
		List<Object> roleIds = sysUserRoleMapper.selectObjs(new LambdaQueryWrapper<SysUserRole>()
				.select(SysUserRole::getRoleId).eq(SysUserRole::getUserId, userId)
				);

		//目录为空
		if (roleIds == null ||roleIds.isEmpty()) {
			return null;
		}
		
		//获取对应的menu  id
		List<Object> menuIds = sysRoleMenuMapper.selectObjs(new LambdaQueryWrapper<SysRoleMenu>()
				.select(SysRoleMenu::getMenuId).in(SysRoleMenu::getRoleId, roleIds));
		
		//目录为空
		if (menuIds == null || menuIds.isEmpty()) {
			return null;
		}
		return menuIds;
	}

	/**
	 * table显示所有菜单
	 */
	@Override
	public List<SysMenu> findSysMenu() {
		List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
									.orderByAsc(SysMenu::getOrderNum));
		return selectList;
	}
	
	/**
	 * 获取菜单内容，不包括按钮
	 */
	@Override
	public List<SysMenu> getListMenu() {
		List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
												.ne(SysMenu::getType, 2)
												.orderByAsc(SysMenu::getOrderNum));
		return selectList;
	}
	
	/**
	 * 新增菜单复写判断
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(SysMenu sysMenu) {
		sysMenu.setCreateDate(LocalDateTime.now());
		sysMenu.setState(1);

		//参数校验
		volifyDate(sysMenu);

		return super.save(sysMenu);
	}

	/**
	 * 新增更新要求
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateById(SysMenu sysMenu) {

		sysMenu.setUpdateDate(LocalDateTime.now());

		//参数校验
		volifyDate(sysMenu);

		SysMenu sysMenu2 = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
										.eq(SysMenu::getMenuId, sysMenu.getMenuId()));
		
		//判断数据库中是否还有该用户
		if (sysMenu2 == null) {
				throw new MyException("请查看该用户是否存在");
		}
		
		//判断修改后菜单类型是否正确
		if (!sysMenu.getType().equals(sysMenu2.getType())) {
				throw new MyException("菜单类型修改错误");
		}
		return super.updateById(sysMenu);
	}


	/**
	 * 复写判断 如果子目录还有东西 ，则抛异常 无法删除
	 */
	@Override
	public boolean removeById(Serializable id) {
		
		//获取当前用户的子用户列表
		List<SysMenu> sysMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
				.eq(SysMenu::getParentId, id));
		if (sysMenus.size() != 0 ) {
			//如果有父类，则抛出异常，删除失败
			throw new MyException("目录下载存在子目录，无法删除");
		}

		//判断如果有角色绑定该菜单，则无法删除
		List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
				.eq(SysRoleMenu::getMenuId, id));
		if (roleMenus.size() != 0){
			//如果如果有角色绑定该菜单，则抛出异常，删除失败
			throw new MyException("有角色绑定该菜单，无法删除");
		}
		return super.removeById(id);

	}

	/**
	 * 菜单参数校验
	 * @param sysMenu
	 */
	private void volifyDate(SysMenu sysMenu) {
		switch (sysMenu.getType()) {

		//目录条件过滤
		case 0:
			Assert.state(sysMenu.getName()!=null, "目录名不能为空");
			sysMenu.setParentId(0);
			sysMenu.setPerms(null);
			sysMenu.setUrl(null);
			break;
		//菜单条件过滤
		case 1:
			Assert.state(sysMenu.getName()!=null && sysMenu.getParentId()!=0 && sysMenu.getUrl()!=null,"菜单新增信息有误");
			sysMenu.setPerms(null);
			break;

		//按钮条件过滤
		case 2:
			Assert.state(sysMenu.getName() != null && sysMenu.getPerms() != null && sysMenu.getParentId() != 0,"按钮新增信息有误");
			sysMenu.setUrl(null);
			sysMenu.setIcon(null);
			break;

		//抛出异常
		default:
				throw new MyException("菜单类型不合法");
		}
	}

}
