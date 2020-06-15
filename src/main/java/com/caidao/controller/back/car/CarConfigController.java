package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.CarConfig;
import com.caidao.service.CarConfigService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@RestController
@RequestMapping("/car/config")
@Slf4j
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

		Assert.notNull(config, "sysConfig must not be null");
		log.info("查询配置类的当前页{}，页大小{}",page.getCurrent(),page.getSize());

		IPage<CarConfig> configPage = configService.findPage(page, config);
		return ResponseEntity.ok(configPage);
	}
	
	/**
	 * 新增字典值
	 * @param config
	 * @return
	 */
	@PostMapping
	@ApiOperation("新增字典数据")
	public ResponseEntity<CarConfig> addSysConfig(@RequestBody CarConfig config){

		Assert.notNull(config,"新增数据字典参数不能为空");
		log.info("新增参数名为{}的数据",config.getParamKey());

		configService.save(config);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 更新前获取对象
	 * @param id
	 * @return
	 */
	@GetMapping("/info/{id}")
	@ApiOperation("通过ID查询字典数据")
	public ResponseEntity<CarConfig> beforeUpdate(@PathVariable("id") Integer id){

		Assert.state(id !=null, "Id不能为空");
		log.info("新增参数ID为{}的数据",id);

		CarConfig config = configService.getById(id);
		return ResponseEntity.ok(config);
	}
	
	/**
	 * 更新字典值  看看是否需要进行必须值判断
	 * @param config
	 * @return
	 */
	@PutMapping
	@ApiOperation("更新字典数据")
	public ResponseEntity<CarConfig> updateSysConfig(@RequestBody CarConfig config){

		Assert.notNull(config,"更新数据字典参数不能为空");
		log.info("更新参数名为{}的数据",config.getParamKey());

		configService.updateById(config);
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
	public ResponseEntity<Void> deleteSysConfig(@RequestBody List<Integer> configId){

		Assert.state(configId.size() !=0, "Id不能为空");
		log.info("新增参数ID为{}的数据",configId);

		configService.removeByIds(configId);
		return ResponseEntity.ok().build();
	}

}
