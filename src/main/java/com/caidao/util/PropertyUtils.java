package com.caidao.util;

/**
 * @author tom
 * @since 2020-5-12
 * 全文中所有的静态属性统一存放类
 */

public class PropertyUtils {

	/** app用户 */
	public static final String APP_USER_REALM = "appUserRealm";

	/** 后台用户 */
	public static final String BACK_USER_REALM = "backUserRealm";
	
	/** 全局验证token */
	public static final String VALCODE_PRIFAX = "prifaxuuid:";
	
	/** 后台用户登录的sessionUUID */
	public static final String USER_LOGIN_SESSION_ID = "loginSession";

	/** 前台用户登录的sessionUUID */
	public static final String APP_USER_LOGIN_SESSION_ID = "appUserLoginSession";

	/** 用户登录后的session凭证 */
	public static final String USER_SESSION = "shiro:session:";

	/** 自定义token头部 */
	public static final String TOKEN_HEADER = "TOKEN";

	/** 多太nginx时取到最初的机器的真是IP */
	public static final String X_FORDARDED_FOR = "X-Forwarded-For";

	/** 取到单台nngix的真是I */
	public static final String X_REAL_IP = "X-real-ip";

	/** 用户修改密码短信验证的6位验证码 */
	public static final String MASSAGE_CODE = "massage_code";

}
