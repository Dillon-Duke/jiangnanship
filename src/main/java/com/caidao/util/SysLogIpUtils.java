package com.caidao.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tom
 */

public class SysLogIpUtils {

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
