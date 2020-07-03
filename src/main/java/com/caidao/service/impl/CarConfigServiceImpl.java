package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.CarConfigMapper;
import com.caidao.pojo.CarConfig;
import com.caidao.service.CarConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
@Slf4j
public class CarConfigServiceImpl extends ServiceImpl<CarConfigMapper, CarConfig> implements CarConfigService {

	@Autowired
	private CarConfigMapper carConfigMapper;
	
	/**
	 * 获取配置字典类的分页数据
	 */
	@Override
	public IPage<CarConfig> findPage(Page<CarConfig> page, CarConfig config) {
		Assert.notNull(config, "sysConfig must not be null");
		log.info("查询配置类的当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<CarConfig> selectPage = carConfigMapper.selectPage(page, new LambdaQueryWrapper<CarConfig>()
				.like(StringUtils.hasText(config.getParamKey()), CarConfig::getParamKey, config.getParamKey()));
		return selectPage;
	}

	/**
	 * 获得不同类型车辆的工作内容
	 * @param configKey
	 * @return
	 */
	@Override
	public List<CarConfig> getCarContent(String configKey) {
		Assert.notNull(configKey,"车辆类别名称不能为空");
		log.info("查询车辆类别为{}的车辆工作内容",configKey);
		List<CarConfig> configList = carConfigMapper.selectList(new LambdaQueryWrapper<CarConfig>()
				.eq(CarConfig::getParamKey, configKey));
		return configList;
	}

	/**
	 * 新增字典值
	 * @param config
	 * @return
	 */
	@Override
	public boolean save(CarConfig config) {
		Assert.notNull(config,"新增数据字典参数不能为空");
		log.info("新增参数名为{}的数据",config.getParamKey());
		boolean save = super.save(config);
		return save;
	}

	/**
	 * 更新前获取对象
	 * @param id
	 * @return
	 */
	@Override
	public CarConfig getById(Serializable id) {
		Assert.notNull(id , "Id不能为空");
		log.info("获取参数ID为{}的数据",id);
		return super.getById(id);
	}

	/**
	 * 更新字典值
	 * @param config
	 * @return
	 */
	@Override
	public boolean updateById(CarConfig config) {
		Assert.notNull(config,"更新数据字典参数不能为空");
		log.info("更新参数名为{}的数据",config.getParamKey());
		return super.updateById(config);
	}

	/**
	 * 批量删除
	 * @param idList
	 * @return
	 */
	@Override
	public boolean removeByIds(Collection<? extends Serializable> idList) {
		Assert.notNull(idList, "Id不能为空");
		log.info("新增参数ID为{}的数据",idList);
		return super.removeByIds(idList);
	}
}
