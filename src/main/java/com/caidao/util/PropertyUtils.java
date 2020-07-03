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
	 * 前台用户授权前缀
	 */
	public static final String APP_USER_REALM = "appUserRealm";

	/**
	 * 后台用户授权前缀
	 */
	public static final String BACK_USER_REALM = "backUserRealm";

	/**
	 * 全局验证token
	 */
	public static final String VALCODE_PRIFAX = "prifaxuuid:";

	/**
	 * 前台公钥前缀
	 */
	public static final String APP_USER_PUBLIC_KEY = "appUserPublicKey:";

	/**
	 * 前台私钥前缀
	 */
	public static final String APP_USER_PRIVATE_KEY = "appUserPrivateKey:";

	/**
	 * aes密文前缀
	 */
	public static final String AES_PREFIX = "aes_prefix:";

	/**
	 * aes加密密钥长度
	 */
	public static final Integer AES_KEY_LENGTH = 16;

	/**
	 * 编码格式
	 */
	public static final String ENCODING = "UTF-8";

	/**
	 * 声明使用aes加密
	 */
	public static final String AES_KEY_ALGORITHM = "AES";

	/**
	 * 加解密算法/工作模式/填充方式
	 */
	public static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

	/**
	 * 字符串分割符号
	 */
	public static final String STRING_SPILT_WITH_SEMICOLON = ";";

	/**
	 * 用户后台登录后的session凭证
	 */
	public static final String USER_SESSION = "shiro:session:";

	/**
	 * 所有用户的登录token集合
	 */
	public static final String ALL_USER_TOKEN = "allUserToken";

	/**
	 * 自定义token头部
	 */
	public static final String TOKEN_HEADER = "TOKEN";

	/**
	 * 多台nginx时取到最初的机器的真是IP
	 */
	public static final String X_FORDARDED_FOR = "X-Forwarded-For";

	/**
	 * 取到单台nginx的真实ID
	 */
	public static final String X_REAL_IP = "X-real-ip";

	/**
	 * 用户修改密码短信验证的6位验证码
	 */
	public static final String MASSAGE_CODE = "massage_code";

	/**
	 * 平板车计划任务申请工单号前缀
	 */
	public static final String FLAT_CAR_PLAN_ODD_NUMBER_PREFIX = "PBSQJH";

	/**
	 * 平板车临时任务申请工单号前缀
	 */
	public static final String FLAT_CAR_TEMP_ODD_NUMBER_PREFIX = "PBSQLS";

	/**
	 * 平板车取消任务申请工单号前缀
	 */
	public static final String FLAT_CAR_CANCEL_ODD_NUMBER_PREFIX = "PBSQQS";

	/**
	 * 平板车衍生任务申请工单号前缀
	 */
	public static final String FLAT_CAR_OTHER_TEMP_ODD_NUMBER_PREFIX = "PBSQKS";

	/**
	 * 司机绑定车辆信息前缀
	 */
	public static final String USER_BIND_CAR_TASK_PREFIX = "PBRW";

	/**
	 * 工作流程中司机执行，执行确认的后几个流程名称，用来判断任务是否已经开始执行了
	 */
	public static final String PLAT_CAR_PROCESS_IS_OR_NOT_EXECUTE = "司机执行,执行完成,部门评价";

}