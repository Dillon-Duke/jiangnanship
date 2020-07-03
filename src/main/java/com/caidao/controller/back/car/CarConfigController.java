package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.enums.CarTypeEnums;
import com.caidao.pojo.CarConfig;
import com.caidao.service.CarConfigService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/car/config")
public class CarConfigController {

	public static final Logger logger = LoggerFactory.getLogger(CarConfigController.class);
	
	@Autowired
	private CarConfigService configService;
	
	/**
	 * 获取页面的分页数据
	 * @param page
	 * @param config
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取当前页字典数据")
	public ResponseEntity<IPage<CarConfig>> getSysConfigPage(Page<CarConfig> page, CarConfig config){
		IPage<CarConfig> configPage = configService.findPage(page, config);
		return ResponseEntity.ok(configPage);
	}

	/**
	 * 查询需要新增字典值的大类
	 * @return
	 */
	@GetMapping("/findCarTypeEnums")
	@ApiOperation("查询需要新增字典值的大类")
	public ResponseEntity<CarTypeEnums[]> findCarTypeEnums(){
		CarTypeEnums[] values = CarTypeEnums.values();
		return ResponseEntity.ok(values);
	}
	
	/**
	 * 新增字典值
	 * @param config
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增字典数据")
	public ResponseEntity<Boolean> addSysConfig(@RequestBody CarConfig config){
		Boolean save = configService.save(config);
		return ResponseEntity.ok(save);
	}
	
	/**
	 * 更新前获取对象
	 * @param id
	 * @return
	 */
	@GetMapping("/info/{id}")
	@ApiOperation("通过ID查询字典数据")
	public ResponseEntity<CarConfig> beforeUpdate(@PathVariable("id") Integer id){
		CarConfig config = configService.getById(id);
		return ResponseEntity.ok(config);
	}
	
	/**
	 * 更新字典值
	 * @param config
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新字典数据")
	public ResponseEntity<Boolean> updateSysConfig(@RequestBody CarConfig config){
		Boolean update = configService.updateById(config);
		return ResponseEntity.ok(update);
	}
	
	/**
	 * 批量删除
	 * @param configId
	 * @return
	 */
	@SysLogs("批量删除字典数据")
	@DeleteMapping
	@ApiOperation("批量删除字典数据")
	public ResponseEntity<Boolean> deleteSysConfig(@RequestBody List<Integer> configId){
		Boolean remove = configService.removeByIds(configId);
		return ResponseEntity.ok(remove);

	}

}
