package com.caidao.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tom
 * @since
 * 该类作用是在记录日志的时候，如果在多台nginx的情况下，找到用户的真是IP
 */

public class SysLogIpUtils {

	public static final Logger logger = LoggerFactory.getLogger(SysLogIpUtils.class);

	/**
	 * 私有化构造
	 */
	private SysLogIpUtils() {}
	
	public static String getSysLogIpUtils(HttpServletRequest request) {
		String forwardedFor = request.getHeader(PropertyUtils.X_FORDARDED_FOR);
		String realIp = request.getHeader(PropertyUtils.X_REAL_IP);
		String remoteAddr = request.getRemoteAddr();
		String ip = null;
		if (!(forwardedFor == null || forwardedFor.isEmpty())) {
			ip = forwardedFor;
		}else if (!(realIp == null || realIp.isEmpty())) {
			ip = realIp;
		} else {
			ip = remoteAddr;
		}
		return ip;
	}

}
