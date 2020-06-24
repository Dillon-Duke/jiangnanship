package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.CarConfig;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface CarConfigService extends IService<CarConfig> {

	/**
	 * 获取页面的page页面
	 * @param page
	 * @param config
	 * @return
	 */
	IPage<CarConfig> findPage(Page<CarConfig> page, CarConfig config);

	/**
	 * 获得不同类型车辆的工作内容
	 * @param configKey
	 * @return
	 */
	List<CarConfig> getCarContent(String configKey);

}
