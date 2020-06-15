package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.sysLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysLogService extends IService<sysLog> {

	/**
	 * 获取页表数据 当前页 页大小
	 * @param page
	 * @param logs
	 * @return
	 */
	IPage<sysLog> findPage(Page<sysLog> page, sysLog logs);

}
