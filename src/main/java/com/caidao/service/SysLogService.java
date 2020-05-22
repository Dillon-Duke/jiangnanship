package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.Log;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysLogService extends IService<Log> {

	/**
	 * 获取页表数据 当前页 页大小
	 * @param page
	 * @param logs
	 * @return
	 */
	IPage<Log> findPage(Page<Log> page, Log logs);

}
