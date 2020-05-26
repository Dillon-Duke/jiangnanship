package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysMenu;
import com.caidao.entity.SysUser;
import com.caidao.param.Menu;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysMenuService extends IService<SysMenu> {
	/**
	 * 查询用户的菜单列表
	 * @param user
	 * @return
	 */
	List<Menu> getMenuListByUserId(SysUser user);
	
	/**
	 * 查询用户的权限列表
	 * @param userId
	 * @return
	 */
	List<String> getAuth2ByUslerId(Integer userId);

	/**
	 * 获取所有的菜单列表
	 * @return
	 */
	List<SysMenu> findSysMenu();

	/**
	 * 新增菜单管理
	 * @return
	 */
	List<SysMenu> getListMenu();


}
