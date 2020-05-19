package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.SysLog;
import com.caidao.mapper.SysLogMapper;
import com.caidao.service.SysLogService;

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
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

	@Autowired
	private SysLogMapper sysLogMapper;
	
	/**
	 * 获取日志的页面信息 ，当前页 页大小
	 */
	@Override
	public IPage<SysLog> findPage(Page<SysLog> page, SysLog logs) {
		IPage<SysLog> selectPage = sysLogMapper.selectPage(page, new LambdaQueryWrapper<SysLog>()
				.like(StringUtils.hasText(logs.getUsername()), SysLog::getUsername,logs.getUsername())
				.like(StringUtils.hasText(logs.getOperation()), SysLog::getOperation,logs.getOperation()));
		return selectPage;
	}

}
