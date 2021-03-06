package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysMenu;
import com.caidao.entity.SysRoleMenu;
import com.caidao.entity.SysUserRole;
import com.caidao.mapper.SysMenuMapper;
import com.caidao.mapper.SysRoleMenuMapper;
import com.caidao.mapper.SysUserRoleMapper;
import com.caidao.param.Menu;
import com.caidao.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
	public List<Menu> getMenuListByUserId(Integer userId) {
		Assert.notNull(userId, "userId must not be null");
		log.info("{}用户登录之后查询菜单",userId);
		List<SysMenu> menuList = findMenuListByUserId(userId);

		if (menuList == null || menuList.isEmpty()) {

			return null;
		}
		
		List<Menu> rootone = new ArrayList<>();
		
		//TODO 暂时没有明白什么意思 以后再说
		//设置一级树形菜单
		for (SysMenu sysMenu : menuList) {
			if (sysMenu.getParentId() == 0) {
				rootone.add(SysMenuService(sysMenu));
			}
		}
		
		//设置二级树形菜单
		for (SysMenu sysMenu : menuList) {
			for (Menu menu : rootone) {
				if (menu.getMenuId().equals(sysMenu.getParentId())) {
					menu.getList().add(SysMenuService(sysMenu));
				}
			}
		}
	return rootone;
	}

	private Menu SysMenuService(SysMenu sysMenu) {
		Menu menu = new Menu();
		menu.setMenuId(sysMenu.getMenuId());
		menu.setName(sysMenu.getName());
		menu.setUrl(sysMenu.getUrl());
		return menu;
	}

	@Override   //查询用户的按钮权限
	//开启缓存，因为是shiro的查询 所以缓存放在redis里面了
//	@Cacheable(cacheNames="com.tencent.service.impl.SysMenuServiceImpl",key="#p0")
	public List<String> getAuth2ByUslerId(Integer userId) {
		//获取登录用户的菜单id
		List<Object> menuIds = getMenuIds(userId);
		List<Object> selectObjs = sysMenuMapper.selectObjs(new LambdaQueryWrapper<SysMenu>()
														.select(SysMenu::getPerms)
														.in(SysMenu::getMenuId, menuIds)
														.isNotNull(SysMenu::getPerms)
														.ne(SysMenu::getPerms, ""));
		ArrayList<String> result = new ArrayList<String>();
		for (Object object : selectObjs) {
			String authorities = String.valueOf(object);
			String[] split = authorities.split(",");
			for (String string : split) {
				result.add(string);
			}
		}
		return result;
	}
	
	private List<SysMenu> findMenuListByUserId(Integer userId) {
		//获取登录用户的菜单id
		List<Object> menuIds = getMenuIds(userId);
		//判断用户如果是admin(菜单id为1) 则查询所有的菜单
		if (userId == 1) {
			List<SysMenu> selectList = sysMenuMapper.selectList(null);
			return selectList;
		}

		//判断登录人员是否有页面权限，没有直接返回null;
		if (menuIds == null){
			return null;
		}
		
		//查询对应的menu列表
		 List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
				 .in(SysMenu::getMenuId, menuIds));
//				 .eq(SysMenu::getParentId, 0l)
//				 .or(true)
//				 .eq(SysMenu::getParentId, 1));
		 return selectList;
	}
	
	//通过用户id获取用户对应的菜单id
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
		log.info("table获取全部的菜单");
		List<SysMenu> selectList = sysMenuMapper.selectList(null);
		return selectList;
	}
	
	/**
	 * 新增获取菜单内容，不包括按钮
	 */
	@Override
	public List<SysMenu> getListMenu() {
		log.info("获取全部的菜单，不包括按钮");
		List<SysMenu> selectList = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
				.ne(SysMenu::getType, 2));
		return selectList;
	}
	
	/**
	 * 新增菜单复写判断
	 */
	@Override
	public boolean save(SysMenu sysMenu) {
		sysMenu.setCreateDate(LocalDateTime.now());
		sysMenu.setState(1);
		boolean sysMenus = super.save(sysMenu);
		//参数校验
		volifyDate(sysMenu);
		return sysMenus;
	}


	/**
	 * 新增更新要求
	 */
	@Override
	public boolean updateById(SysMenu sysMenu) {

		sysMenu.setUpdateDate(LocalDateTime.now());

		//参数校验
		volifyDate(sysMenu);
		Assert.notNull(sysMenu.getMenuId(), "sysMenu.getMenuId() must not be null");

		SysMenu sysMenu2 = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
										.eq(SysMenu::getMenuId, sysMenu.getMenuId()));
		
		//判断数据库中是否还有该用户
		if (sysMenu2 == null) {
				throw new RuntimeException("请查看该用户是否存在");
		}
		
		//判断修改后菜单类型是否正确
		if (!sysMenu.getType().equals(sysMenu2.getType())) {
				throw new RuntimeException("菜单类型修改错误");
		}
		return super.updateById(sysMenu);
	}


	/**
	 * 复写判断 如果父目录还有东西 ，则抛异常 无法删除
	 */
	@Override
	public boolean removeById(Serializable id) {
		
		//获取当前用户
		SysMenu sysMenu = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
														.eq(SysMenu::getParentId, id));
		if (sysMenu == null ) {
			return super.removeById(id);
		}
		//如果有父类，则抛出异常，删除失败
		throw new RuntimeException("父类还存在，无法删除");
	}

	/**
	 * 菜单参数校验
	 * @param sysMenu
	 */
	private void volifyDate(SysMenu sysMenu) {
		switch (sysMenu.getType()) {
		case 0:  //目录条件过滤
			Assert.state(sysMenu.getName()!=null, "目录名不能为空");
			sysMenu.setParentId(0);
			sysMenu.setPerms(null);
			sysMenu.setUrl(null);
			break;
			
		case 1:  //菜单条件过滤
			Assert.state(sysMenu.getName()!=null
								&& sysMenu.getParentId()!=0
								&& sysMenu.getUrl()!=null
								, "菜单新增信息有误");
			sysMenu.setPerms(null);
			break;
			
		case 2: //按钮条件过滤
			Assert.state(sysMenu.getName()!=null
								&& sysMenu.getUrl()==null
								&& sysMenu.getParentId()!=0
								&& sysMenu.getPerms()!=null,
								"按钮新增信息有误");
			sysMenu.setUrl(null);
			sysMenu.setIcon(null);
			break;

		default:
				throw new RuntimeException("菜单类型不合法");
		}
	}

}
