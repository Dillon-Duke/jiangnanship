package com.caidao.controller.back.system;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.entity.SysUser;
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
@RequestMapping("/sys/user")
public class SysUserController {
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
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
	public ResponseEntity<SysUser> addUser(@RequestBody SysUser sysUser){
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
	public ResponseEntity<SysUser> getUserById(@PathVariable("id") long id){
		SysUser sysUser = sysUserService.getById(id);
		return ResponseEntity.ok(sysUser);
	}
	
	/**
	 * 批量删除用户
	 * 真删除
	 * @param ids
	 * @return
	 */
//	@RequiresPermissions("sys:user:delete")
//	@DeleteMapping
//	public ResponseEntity<Void> beachDel(@RequestBody List<Long> ids){
//		sysUserService.removeByIds(ids);
//		return ResponseEntity.ok().build();
//	}

	/**
	 * 批量删除用户
	 * 假删除
	 * @param ids
	 * @return
	 */
	@SysLogs("批量删除用户")
	@RequiresPermissions("sys:user:delete")
	@DeleteMapping
	@ApiOperation("批量删除用户，假删除")
	public ResponseEntity<Void> beachDel(@RequestBody List<Integer> ids){
		sysUserService.deleteByIds(ids);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 修改用户
	 * @param sysUser
	 * @return
	 * @throws IOException 
	 */
	@RequiresPermissions("sys:user:update")
	@PutMapping
	@ApiOperation("修改用户")
	public ResponseEntity<SysUser> updateUser(@RequestBody SysUser sysUser,HttpServletResponse resource) throws IOException{
		SysUser sysUser2 = (SysUser)SecurityUtils.getSubject().getPrincipal();

		//设置更新人id
		sysUser.setUpdateId(sysUser2.getUserId());

		sysUserService.updateById(sysUser);
		
		//获取对应的登录用户session
		String sessionKey = redisTemplate.opsForValue().get(PropertyUtils.USER_LOGIN_SESSION_ID+sysUser.getUsername());
		
		//判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
		if (sessionKey != null) {
			redisTemplate.delete(PropertyUtils.USER_SESSION+sessionKey);
			redisTemplate.delete(PropertyUtils.USER_LOGIN_SESSION_ID+sysUser.getUsername());
			resource.sendError(401);
		}
		return ResponseEntity.ok().build();
	}	

}
