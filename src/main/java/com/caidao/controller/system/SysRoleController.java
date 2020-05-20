	package com.caidao.controller.system;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.entity.SysRole;
import com.caidao.entity.SysUser;
import com.caidao.service.SysRoleService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
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

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController {
	
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
	public ResponseEntity<Void> addSysRolr(@RequestBody SysRole sysRole){
		SysUser sUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
		sysRole.setCreateId(sUser.getUserId());
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
	 public ResponseEntity<SysRole> getSysRoleById(@PathVariable("id") Long id){
		SysRole sysRole = sysRoleService.getById(id);
		 return ResponseEntity.ok(sysRole);
	 }
	
	/**
	 * 修改用户
	 * @param sysRole
	 * @return
	 */
	@PutMapping
	@ApiOperation("修改角色信息")
	public ResponseEntity<Void> updateRoleByRoleId(@RequestBody SysRole sysRole){
		SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
		sysRole.setUpdateId(principal.getUserId());
		sysRoleService.updateById(sysRole);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 删除用户
	 * @param ids
	 * @return
	 */
	@SysLogs("删除角色信息")
	@DeleteMapping
	@ApiOperation("删除角色信息")
	public ResponseEntity<Void> deleteSysRole(@RequestBody List<Long> ids){
		sysRoleService.removeByIds(ids);
		return ResponseEntity.ok().build();
	}

}
