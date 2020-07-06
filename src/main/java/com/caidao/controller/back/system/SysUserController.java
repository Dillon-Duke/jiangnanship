package com.caidao.controller.back.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.SysUser;
import com.caidao.service.SysUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

	public static final Logger logger = LoggerFactory.getLogger(SysUserController.class);
	
	@Autowired
	private SysUserService sysUserService;
	
	/**
	 * 获取分页的用户数据
	 * @param page
	 * @param sysUser
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取分页的用户数据")
	@RequiresPermissions("sys:user:page")
	public ResponseEntity<IPage<SysUser>> userPage(Page<SysUser> page, SysUser sysUser){
		IPage<SysUser> usersPage = sysUserService.getUserPage(page,sysUser);
		return ResponseEntity.ok(usersPage);
	}
	
	/**
	 * 新增用户
	 * @param sysUser
	 * @return
	 */
	@RequiresPermissions("sys:user:save")
	@PostMapping
	@ApiOperation("新增用户")
	public ResponseEntity<Boolean> addUser(@RequestBody SysUser sysUser){
		Boolean save = sysUserService.save(sysUser);
		return ResponseEntity.ok(save);
	}
	
	/**
	 * 通过id获取用户数据
	 * @param id
	 * @return
	 */
	@RequiresPermissions("sys:user:info")
	@GetMapping("info/{id}")
	@ApiOperation("通过id获取用户数据")
	public ResponseEntity<SysUser> getUserById(@PathVariable("id") Integer id){
		SysUser sysUser = sysUserService.getById(id);
		return ResponseEntity.ok(sysUser);
	}

	/**
	 * 批量删除用户 假删除
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("sys:user:delete")
	@DeleteMapping
	@ApiOperation("批量删除用户")
	public ResponseEntity<Boolean> beachDel(@RequestBody List<Integer> ids){
		Boolean removeByIds = sysUserService.removeByIds(ids);
		if (removeByIds) {
			return ResponseEntity.ok().build();
		}
		return ResponseEntity.badRequest().build();
	}
	
	/**
	 * 修改用户
	 * @param sysUser
	 * @return
	 * @throws IOException 
	 */
	@PutMapping
	@ApiOperation("修改用户")
	@RequiresPermissions("sys:user:update")
	public ResponseEntity<Boolean> updateUser(@RequestBody SysUser sysUser) {
		Boolean updateById = sysUserService.updateById(sysUser);
		return ResponseEntity.ok(updateById);
	}	

}
