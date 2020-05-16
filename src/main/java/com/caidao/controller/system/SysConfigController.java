package com.caidao.controller.system;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysConfig;
import com.caidao.service.SysConfigService;
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
@RequestMapping("/sys/config")
public class SysConfigController  {
	
	@Autowired
	private SysConfigService sysConfigService;
	
	/**
	 * 获取页面的分页数据
	 * @param page
	 * @param sysConfig
	 * @return
	 */
	@GetMapping("page")
	public ResponseEntity<IPage<SysConfig>> getSysConfigPage(Page<SysConfig> page, SysConfig sysConfig){
		IPage<SysConfig> configPage = sysConfigService.findPage(page, sysConfig);
		return ResponseEntity.ok(configPage);
	}
	
	/**
	 * 新增字典值
	 * @param sysConfig
	 * @return
	 */
	@PostMapping
	public ResponseEntity<SysConfig> addSysConfig(@RequestBody SysConfig sysConfig){
		sysConfigService.save(sysConfig);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 更新前获取对象
	 * @param id
	 * @return
	 */
	@GetMapping("/info/{id}")
	public ResponseEntity<SysConfig> beforeUpdate(@PathVariable("id") Long id){
		SysConfig sysConfig = sysConfigService.getById(id);
		return ResponseEntity.ok(sysConfig);
	}
	
	/**
	 * 更新字典值  看看是否需要进行必须值判断
	 * @param sysConfig
	 * @return
	 */
	@PutMapping
	public ResponseEntity<SysConfig> updateSysConfig(@RequestBody SysConfig sysConfig){
		sysConfigService.updateById(sysConfig);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 批量删除
	 * @param ConfigId
	 * @return
	 */
	@DeleteMapping
	public ResponseEntity<List<Long>> deleteSysConfig(@RequestBody List<Long> ConfigId){
		sysConfigService.removeByIds(ConfigId);
		return ResponseEntity.ok().build();
	}

}
