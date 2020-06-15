package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.pojo.CarConfig;
import com.caidao.mapper.CarConfigMapper;
import com.caidao.service.CarConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
@Service
public class CarCarConfigServiceImpl extends ServiceImpl<CarConfigMapper, CarConfig> implements CarConfigService {

	@Autowired
	private CarConfigMapper carConfigMapper;
	
	/**
	 * 获取配置字典类的分页数据
	 */
	@Override
	public IPage<CarConfig> findPage(Page<CarConfig> page, CarConfig config) {
		IPage<CarConfig> selectPage = carConfigMapper.selectPage(page, new LambdaQueryWrapper<CarConfig>()
				.like(StringUtils.hasText(config.getParamKey()), CarConfig::getParamKey, config.getParamKey()));
		return selectPage;
	}

}
