package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.Config;
import com.caidao.mapper.ConfigMapper;
import com.caidao.service.ConfigService;
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
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

	@Autowired
	private ConfigMapper configMapper;
	
	/**
	 * 获取配置字典类的分页数据
	 */
	@Override
	public IPage<Config> findPage(Page<Config> page, Config config) {
		IPage<Config> selectPage = configMapper.selectPage(page, new LambdaQueryWrapper<Config>()
				.like(StringUtils.hasText(config.getParamKey()), Config::getParamKey, config.getParamKey()));
		return selectPage;
	}

}
