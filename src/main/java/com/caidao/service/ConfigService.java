package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.Config;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface ConfigService extends IService<Config> {

	/**
	 * 获取页面的page页面
	 * @param page
	 * @param config
	 * @return
	 */
	IPage<Config> findPage(Page<Config> page, Config config);

}
