package com.caidao.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysLog;
import com.caidao.service.SysLogService;
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
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/sys/log")
public class SysLogController {
	
	@Autowired
	private SysLogService sysLogService;
	
	/**
	 * 获取日志的当前页  页大小
	 * @param page
	 * @param log
	 * @return
	 */
	@GetMapping("/page")
	public ResponseEntity<IPage<SysLog>> getPage(Page<SysLog> page, SysLog log){
		IPage<SysLog> findPage = sysLogService.findPage(page,log);
		return ResponseEntity.ok(findPage);
	}

}
