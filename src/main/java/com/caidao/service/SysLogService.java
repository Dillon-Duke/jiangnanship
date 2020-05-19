package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.entity.SysLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysLogService extends IService<SysLog> {

	/**
	 * 获取页表数据 当前页 页大小
	 * @param page
	 * @param logs
	 * @return
	 */
	IPage<SysLog> findPage(Page<SysLog> page, SysLog logs);

}
