package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysConfig;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysConfigService extends IService<SysConfig> {

	/**
	 * 获取页面的page页面
	 * @param page
	 * @param sysConfig
	 * @return
	 */
	IPage<SysConfig> findPage(Page<SysConfig> page, SysConfig sysConfig);

}
