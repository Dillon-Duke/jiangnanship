package com.caidao.util;

/**
 * @author tom
 * @since 2020-5-12
 * 全文中所有的静态属性统一存放类
 */

public class PropertyUtils {

	/**
	 * 密钥长度 于原文长度对应 以及越长速度越慢
	 */
	public static final int KEY_SIZE = 1024;

	/**
	 * app用户
	 */
	public static final String APP_USER_REALM = "appUserRealm";

	/**
	 * 后台用户
	 */
	public static final String BACK_USER_REALM = "backUserRealm";

	/**
	 * 全局验证token
	 */
	public static final String VALCODE_PRIFAX = "prifaxuuid:";

	/**
	 * 前台公钥
	 */
	public static final String APP_USER_PUBLIC_KEY = "appUserPublicKey:";

	/**
	 * 前台私钥
	 */
	public static final String APP_USER_PRIVATE_KEY = "appUserPrivateKey:";

	/**
	 * 用户后台登录后的session凭证
	 */
	public static final String USER_SESSION = "shiro:session:";

	/**
	 * 自定义token头部
	 */
	public static final String TOKEN_HEADER = "TOKEN";

	/**
	 * 多太nginx时取到最初的机器的真是IP
	 */
	public static final String X_FORDARDED_FOR = "X-Forwarded-For";

	/**
	 * 取到单台nngix的真实ID
	 */
	public static final String X_REAL_IP = "X-real-ip";

	/**
	 * 用户修改密码短信验证的6位验证码
	 */
	public static final String MASSAGE_CODE = "massage_code";

	/**
	 *部门取消任务是否开始驳运
	 */
	public static final String IS_START_TRANS = "is_start_trans";

}