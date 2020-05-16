package com.caidao.controller;

import java.sql.SQLException;

import com.caidao.entity.SysUser;
import com.caidao.service.SysUserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class ManagerWebController {
	
	@Autowired
	private SysUserService sysUserService;
	
	@GetMapping("/jiekou")
	@ApiOperation("測試接口")
	@RequiresPermissions("sys:user:page,sys:user:info")
	public ResponseEntity<String> test() throws SQLException{
		return ResponseEntity.ok("yes");
	}
	
	@GetMapping("/test/{ids}")
	@ApiOperation("通过id查用户")
	@RequiresPermissions("sys:user:info")
	public ResponseEntity<SysUser> userTest(@PathVariable("ids")String ids){
		int id = Integer.getInteger(ids);
		SysUser sysUser = sysUserService.getById(id);
		return ResponseEntity.ok(sysUser);
	}
		
}


