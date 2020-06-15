package com.caidao.controller.back.system;

import com.caidao.anno.SysLogs;
import com.caidao.pojo.SysMenu;
import com.caidao.pojo.SysUser;
import com.caidao.service.SysMenuService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */

@RestController
@RequestMapping("/sys/menu")
@Slf4j
public class SysMenuController {

	public static final Logger logger = LoggerFactory.getLogger(SysMenuController.class);
	
	@Autowired
	private SysMenuService sysMenuService;
	
	/**
	 * 获取所有的菜单列表 包括目录 菜单 按钮
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取所有的菜单列表 包括目录 菜单 按钮")
	@RequiresPermissions("sys:menu:page")
	public ResponseEntity<List<SysMenu>> getSysMenu(){

		log.info("获取所有菜单信息");

		List<SysMenu> sysMenus = sysMenuService.findSysMenu();
		return ResponseEntity.ok(sysMenus);
	}
	
	/**
	 * 查询菜单 ，只能有目录菜单 但是不能有按钮
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("查询菜单")
	@RequiresPermissions("sys:menu:list")
	public ResponseEntity<List<SysMenu>> addSysmenu(){

		log.info("查询系统菜单列表");

		List<SysMenu> findSysMenu = sysMenuService.getListMenu();
		return ResponseEntity.ok(findSysMenu);
	}
	
	/**
	 * 新增一个菜单
	 * 
	 * @param sysMenu
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增一个菜单,目录或者按钮")
	@RequiresPermissions("sys:menu:save")
	public ResponseEntity<SysMenu> addSysMenu(@RequestBody SysMenu sysMenu){

		Assert.notNull(sysMenu, "系统菜单不能为空");
		log.info("新增菜单名为{}的菜单",sysMenu.getName());
		//获取当前登录对象
		SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysMenu.setCreateId(sysUser.getUserId());

		sysMenuService.save(sysMenu);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 修改前获取对应的需要修改的菜单信息
	 * @param id
	 * @return
	 */
	@GetMapping("info/{id}")
	@ApiOperation("修改前获取对应的需要修改的信息")
	@RequiresPermissions("sys:menu:info")
	public ResponseEntity<SysMenu> getOneMenu(@PathVariable("id") Integer id){

		Assert.notNull(id, "id不能为空");
		log.info("查询菜单Id为{}的菜单",id);

		SysMenu sysMenu = sysMenuService.getById(id);
		return ResponseEntity.ok(sysMenu);
	}
	
	/**
	 * 更新菜单
	 * @param sysMenu
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新菜单,目录或者按钮")
	@RequiresPermissions("sys:menu:update")
	public ResponseEntity<SysMenu> updateMenu(@RequestBody SysMenu sysMenu){

		Assert.notNull(sysMenu, "系统菜单不能为空");
		log.info("更新菜单Id为{}的菜单",sysMenu.getMenuId());

		//获取当前登录对象
		SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysMenu.setUpdateId(sysUser.getUserId());
		sysMenuService.updateById(sysMenu);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 删除菜单目录按钮
	 * @param id
	 * @return
	 */
	@SysLogs("删除菜单或者按钮或者目录")
	@DeleteMapping("{id}")
	@ApiOperation("通过id删除菜单")
	@RequiresPermissions("sys:menu:delete")
	public ResponseEntity<SysMenu> deleteMenu(@PathVariable("id") Integer id){

		Assert.notNull(id, "id不能为空");
		log.info("删除菜单Id为{}的菜单",id);

		sysMenuService.removeById(id);
		return ResponseEntity.ok().build();
	}

}
