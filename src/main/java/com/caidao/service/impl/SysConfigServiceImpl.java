package com.caidao.service.impl;

import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.Config;
import com.caidao.mapper.SysConfigMapper;
import com.caidao.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
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
@Slf4j
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, Config> implements SysConfigService {

	@Autowired
	private SysConfigMapper sysConfigMapper;
	
	/**
	 * 获取配置字典类的分页数据
	 */
	@Override
	public IPage<Config> findPage(Page<Config> page, Config config) {
		Assert.notNull(config, "sysConfig must not be null");
		log.info("查询配置类的当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<Config> selectPage = sysConfigMapper.selectPage(page, new LambdaQueryWrapper<Config>()
				.like(StringUtils.hasText(config.getParamKey()), Config::getParamKey, config.getParamKey()));
		return selectPage;
	}
	
	/**
	 * 校验新增数据参数
	 */	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(Config config) {
		Assert.state(config.getParamKey() !=null
				&& config.getParamValue() !=null
				&& config.getRemark() !=null ,"新增参数丢失，请查询");
		return super.save(config);
	}

	/**
	 * 校验Id不能为空
	 */
	@Override
	public Config getById(Serializable id) {
		Assert.state(id !=null, "Id不能为空");
		return super.getById(id);
	}
}
