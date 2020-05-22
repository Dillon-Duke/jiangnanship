package com.caidao.controller.back.ohter;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.entity.Config;
import com.caidao.service.SysConfigService;
import io.swagger.annotations.ApiOperation;
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

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/other/config")
public class SysConfigController {
	
	@Autowired
	private SysConfigService sysConfigService;
	
	/**
	 * 获取页面的分页数据
	 * @param page
	 * @param config
	 * @return
	 */
	@GetMapping("page")
	@ApiOperation("获取当前页字典数据")
	@RequiresPermissions("sys:conf:page")
	public ResponseEntity<IPage<Config>> getSysConfigPage(Page<Config> page, Config config){
		IPage<Config> configPage = sysConfigService.findPage(page, config);
		return ResponseEntity.ok(configPage);
	}
	
	/**
	 * 新增字典值
	 * @param config
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增字典数据")
	@RequiresPermissions("sys:conf:save")
	public ResponseEntity<Config> addSysConfig(@RequestBody Config config){
		sysConfigService.save(config);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 更新前获取对象
	 * @param id
	 * @return
	 */
	@GetMapping("/info/{id}")
	@ApiOperation("通过ID查询字典数据")
	@RequiresPermissions("sys:conf:info")
	public ResponseEntity<Config> beforeUpdate(@PathVariable("id") Integer id){
		Config config = sysConfigService.getById(id);
		return ResponseEntity.ok(config);
	}
	
	/**
	 * 更新字典值  看看是否需要进行必须值判断
	 * @param config
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新字典数据")
	@RequiresPermissions("sys:conf:update")
	public ResponseEntity<Config> updateSysConfig(@RequestBody Config config){
		sysConfigService.updateById(config);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 批量删除
	 * @param configId
	 * @return
	 */
	@SysLogs("批量删除字典数据")
	@DeleteMapping
	@ApiOperation("批量删除字典数据")
	@RequiresPermissions("sys:conf:delete")
	public ResponseEntity<Void> deleteSysConfig(@RequestBody List<Integer> configId){
		sysConfigService.removeByIds(configId);
		return ResponseEntity.ok().build();
	}

}
