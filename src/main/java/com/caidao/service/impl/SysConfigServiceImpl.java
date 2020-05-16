package com.caidao.service.impl;

import java.io.Serializable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysConfig;
import com.caidao.mapper.SysConfigMapper;
import com.caidao.service.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

	@Autowired
	private SysConfigMapper sysConfigMapper;
	
	/**
	 * 获取配置字典类的分页数据
	 */
	@Override
	public IPage<SysConfig> findPage(Page<SysConfig> page, SysConfig sysConfig) {
		Assert.notNull(sysConfig, "sysConfig must not be null");
		log.info("查询配置类的当前页{}，页大小{}",page.getCurrent(),page.getSize());
		IPage<SysConfig> selectPage = sysConfigMapper.selectPage(page, new LambdaQueryWrapper<SysConfig>()
				.like(StringUtils.hasText(sysConfig.getParamKey()),SysConfig::getParamKey, sysConfig.getParamKey()));
		return selectPage;
	}
	
	/**
	 * 校验新增数据参数
	 */	
	@Override
	public boolean save(SysConfig sysConfig) {
		Assert.state(sysConfig.getParamKey() !=null
				&& sysConfig.getParamValue() !=null
				&& sysConfig.getRemark() !=null ,"新增参数丢失，请查询");
		return super.save(sysConfig);
	}

	/**
	 * 校验Id不能为空
	 */
	@Override
	public SysConfig getById(Serializable id) {
		Assert.state(id !=null, "Id不能为空");
		return super.getById(id);
	}
}
