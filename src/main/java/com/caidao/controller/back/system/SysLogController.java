package com.caidao.controller.back.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.Log;
import com.caidao.service.SysLogService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Dillon
 * @since 2020-05-11
 */
@RestController
@RequestMapping("/sys/log")
@Slf4j
public class SysLogController {
	
	@Autowired
	private SysLogService sysLogService;
	
	/**
	 * 获取日志的当前页  页大小
	 * @param page
	 * @param logs
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取当前页日志信息")
	@RequiresPermissions("sys:log:page")
	public ResponseEntity<IPage<Log>> getPage(Page<Log> page, Log logs){
		log.info("日志获取当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<Log> findPage = sysLogService.findPage(page,logs);
		return ResponseEntity.ok(findPage);
	}

}
