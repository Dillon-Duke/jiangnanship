package com.caidao.util;

import javax.servlet.http.HttpServletRequest;

public class SysLogIpUtils {
	
	private static final String X_FORDARDED_FOR = "X-Forwarded-For";
	
	private static final String X_REAL_IP = "X-real-ip";
	
	//私有化构造
	private SysLogIpUtils() {}
	
	public static String getSysLogIpUtils(HttpServletRequest request) {
		String forwardedFor = request.getHeader(X_FORDARDED_FOR);
		String realIp = request.getHeader(X_REAL_IP);
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
