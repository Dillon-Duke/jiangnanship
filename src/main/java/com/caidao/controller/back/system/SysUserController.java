package com.caidao.controller.back.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysUser;
import com.caidao.service.SysUserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
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
@Slf4j
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

		Assert.notNull(page,"用户属性不能为空");
		log.info("查询用户页面当前页{}，页大小{}",page.getCurrent(),page.getSize());

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
	public ResponseEntity<SysUser> addUser(@RequestBody SysUser sysUser){

		Assert.notNull(sysUser,"新增用户属性不能为空");
		log.info("新增用户名为{}的用户",sysUser.getUsername());

		SysUser sysUser2 = (SysUser)SecurityUtils.getSubject().getPrincipal();
		sysUser.setCreateId(sysUser2.getUserId());
		sysUserService.save(sysUser);
		return ResponseEntity.ok().build();
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

		Assert.notNull(id,"用户ID不能为空");
		log.info("查询用户ID为{}的用户",id);

		SysUser sysUser = sysUserService.getById(id);
		return ResponseEntity.ok(sysUser);
	}
	
	/**
	 * 批量删除用户
	 * 真删除
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("sys:user:delete")
	@DeleteMapping
	@ApiOperation("批量删除用户")
	public ResponseEntity<Void> beachDel(@RequestBody List<Long> ids){

		Assert.notNull(ids,"用户IDs不能为空");
		log.info("删除用户ID为{}的用户",ids);

		sysUserService.removeByIds(ids);
		return ResponseEntity.ok().build();
	}

	/**
	 * 批量删除用户
	 * 假删除
	 * @param ids
	 * @return
	 */
//	@SysLogs("批量删除用户")
//	@RequiresPermissions("sys:user:delete")
//	@DeleteMapping
//	@ApiOperation("批量删除用户，假删除")
//	public ResponseEntity<Void> beachDel(@RequestBody List<Integer> ids){
//		sysUserService.deleteByIds(ids);
//		return ResponseEntity.ok().build();
//	}
	
	/**
	 * 修改用户
	 * @param sysUser
	 * @return
	 * @throws IOException 
	 */
	@PutMapping
	@ApiOperation("修改用户")
	@RequiresPermissions("sys:user:update")
	public ResponseEntity<SysUser> updateUser(@RequestBody SysUser sysUser) throws IOException {

		Assert.notNull(sysUser,"用户信息不能为空");
		log.info("修改用户名为{}的用户",sysUser.getUsername());

		//设置更新人id
		SysUser sysUser2 = (SysUser)SecurityUtils.getSubject().getPrincipal();
		sysUser.setUpdateId(sysUser2.getUserId());
		sysUserService.updateById(sysUser);

		return ResponseEntity.ok().build();
	}	

}
