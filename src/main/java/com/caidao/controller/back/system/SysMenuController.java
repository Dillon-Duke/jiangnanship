package com.caidao.controller.back.system;

import com.caidao.anno.SysLogs;
import com.caidao.entity.SysMenu;
import com.caidao.entity.SysUser;
import com.caidao.service.SysMenuService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */

@RestController
@RequestMapping("/sys/menu")
public class SysMenuController {
	
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
		sysMenuService.removeById(id);
		return ResponseEntity.ok().build();
	}

}
