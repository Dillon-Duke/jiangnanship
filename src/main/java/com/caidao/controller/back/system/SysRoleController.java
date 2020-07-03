package com.caidao.controller.back.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.SysRole;
import com.caidao.service.SysRoleService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @author jinpeng
 * @since 2020-05-15
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController {

	public static final Logger logger = LoggerFactory.getLogger(SysRoleController.class);
	
	@Autowired
	private SysRoleService sysRoleService;
	
	/**
	 * 获取分页数据
	 * @param page
	 * @param sysRole
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取分页数据")
	@RequiresPermissions("sys:role:page")
	public ResponseEntity<IPage<SysRole>> getRoleList(Page<SysRole> page , SysRole sysRole){
		IPage<SysRole> sysRoles = sysRoleService.findSysRolePage(page,sysRole);
		return ResponseEntity.ok(sysRoles);
	}
	
	/**
	 * 获取所有的角色列表
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("获取所有角色列表")
	@RequiresPermissions("sys:role:list")
	public ResponseEntity<List<SysRole>> getSysRoleList(){
		List<SysRole> rList = sysRoleService.list();
		return ResponseEntity.ok(rList);
	}
	
	/**
	 * 新增角色
	 * @param sysRole
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增角色")
	@RequiresPermissions("sys:role:save")
	public ResponseEntity<Void> addSysRole(@RequestBody SysRole sysRole){
		sysRoleService.save(sysRole);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 编辑前获取被编辑的角色
	 * @param id
	 * @return
	 */
	@GetMapping("info/{id}")
	@ApiOperation("通过id获取角色")
	@RequiresPermissions("sys:role:info")
	 public ResponseEntity<SysRole> getSysRoleById(@PathVariable("id") Long id){
		SysRole sysRole = sysRoleService.getById(id);
		return ResponseEntity.ok(sysRole);
	 }
	
	/**
	 * 修改角色
	 * @param sysRole
	 * @return
	 */
	@PutMapping
	@ApiOperation("修改角色信息")
	@RequiresPermissions("sys:role:update")
	public ResponseEntity<Boolean> updateRoleByRoleId(@RequestBody SysRole sysRole){
		Boolean updateById = sysRoleService.updateById(sysRole);
		return ResponseEntity.ok(updateById);
	}
	
	/**
	 * 删除角色
	 * @param ids
	 * @return
	 */
	@SysLogs("删除角色信息")
	@DeleteMapping
	@ApiOperation("删除角色信息")
	@RequiresPermissions("sys:role:delete")
	public ResponseEntity<Boolean> deleteSysRole(@RequestBody List<Long> ids){
		Boolean remove = sysRoleService.removeByIds(ids);
		return ResponseEntity.ok(remove);
	}

}
