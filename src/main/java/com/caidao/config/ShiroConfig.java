package com.caidao.config;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tom
 * @since 2020-5-12
 */

@Configuration
public class ShiroConfig {
	
	@Value("${spring.redis.host}")
	private String host;
	
	@Value("${spring.redis.port}")
	private Integer port;
	
	/**
	 * securitymanager 安全配置
	 * @param sessionManager
	 * @return
	 */
	@Bean("getDefaultWebSecurityManager")
	@Primary
	public DefaultWebSecurityManager getDefaultWebSecurityManager(Authenticator authenticator, TokenSessionManageConfig sessionManager) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setAuthenticator(authenticator);
		//不同的角色对应不同的认证方法  这个方法一定要放在前面，很狗血的问题，放到后面就会报异常 No realms have been configured!  One or more realms must be present to execute an authorization oper
		securityManager.setAuthorizer(getCustomerAuthrizerConfig());
		List<Realm> realms = new ArrayList<>(2);
		realms.add(getBackUserRealmConfig());
		realms.add(getAppUserRealmConfig());
		securityManager.setRealms(realms);
		//设置将登录信息放在redis里面
		sessionManager.setSessionDAO(getSessionDAO());
		securityManager.setSessionManager(sessionManager);
		//设置shiro cacheManager
		securityManager.setCacheManager(getEhCacheManager());
		return securityManager;
	}

	/**
	 * 配置自定义的权限认证，不同的用户进入不同的realm里面进行认证
	 */
	@Bean("getCustomerAuthrizerConfig")
	@Primary
	public CustomerAuthrizerConfig getCustomerAuthrizerConfig(){
		CustomerAuthrizerConfig customerAuthrizerConfig = new CustomerAuthrizerConfig();
		return customerAuthrizerConfig;
	}

	/**
	 * 配置使用自定义认证器，可以实现多Realm认证，并且可以指定特定Realm处理特定类型的验证
	 */
	@Bean("getCustomRealmAuthenticatorConfig")
	public CustomRealmAuthenticatorConfig getCustomRealmAuthenticatorConfig(){
		CustomRealmAuthenticatorConfig customRealmAuthenticatorConfig = new CustomRealmAuthenticatorConfig();
		//将所有的realm放在shiro中
		Map<String, Object> hashMap = new HashMap<>(2);
			hashMap.put("appUserRealm",getAppUserRealmConfig());
			hashMap.put("backUserRealm",getBackUserRealmConfig());
		customRealmAuthenticatorConfig.setDefinedRealms(hashMap);
		//配置认证策略，只要有一个Realm认证成功即可，并且返回所有认证成功信息
		customRealmAuthenticatorConfig.setAuthenticationStrategy(getFirstSuccessfulStrategy());
		return customRealmAuthenticatorConfig;
	}

	/**
	 * 将shiro认证中的第一个认证成功策略放在spring中
	 * @return
	 */
	@Bean("getFirstSuccessfulStrategy")
	public FirstSuccessfulStrategy getFirstSuccessfulStrategy(){
		FirstSuccessfulStrategy successfulStrategy = new FirstSuccessfulStrategy();
		return successfulStrategy;
	}

	/**
	 * 设置过滤和不需要过滤的页面
	 * @return
	 */
	@Bean(name = "getDefaultShiroFilterChainDefinition")
	public DefaultShiroFilterChainDefinition getDefaultShiroFilterChainDefinition() {
		DefaultShiroFilterChainDefinition defaultShiroFilterChainDefinition = new DefaultShiroFilterChainDefinition();
		Map<String, String> hashMap = new HashMap<>(12);
		//配置对swagger权限放开
		hashMap.put("/doc.html", "anon");
		hashMap.put("/webjars/**", "anon");
		hashMap.put("/v2/**", "anon");
		hashMap.put("/swagger-resources/**", "anon");
		//系统后台需要放开的页面路径
		hashMap.put("/captcha.jpg", "anon");
		hashMap.put("/login", "anon");
		hashMap.put("/logout", "logout");
		hashMap.put("/sys/menu/nav", "anon");
		//系统前台需要放开的页面路径
		hashMap.put("/appLogin", "anon");
		hashMap.put("/Authorities", "anon");
		hashMap.put("/AppLogout", "anon");
		//表示所有的路径不拦截 调试使用
		hashMap.put("/**", "anon");
		//表示需要认证才可以访问
//		hashMap.put("/**", "authc");

		defaultShiroFilterChainDefinition.addPathDefinitions(hashMap);
		return defaultShiroFilterChainDefinition;
	}

	/**
	 * 配置shiro redis的地址还有端口
	 * @return
	 */
	@Bean(name = "getRedisManager")
	public IRedisManager getRedisManager() {
		RedisManager redisManager = new RedisManager();
		redisManager.setHost(host+":"+port);
		return redisManager;
	}

	/**
	 * SessionDAO的作用是为Session提供CRUD并进行持久化的一个shiro组件
	 * MemorySessionDAO 直接在内存中进行会话维护
	 * @return
	 */
	@Bean("getSessionDAO")
	public SessionDAO getSessionDAO() {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(getRedisManager());
		//session在redis中的保存时间,最好大于session会话超时时间
		redisSessionDAO.setExpire(1801);
		System.out.println(redisSessionDAO);
		return redisSessionDAO;
	}

	/**
	 * 设置盐值
	 * @return
	 */
	@Bean(name = "getCredentialsMatcher")
	public CredentialsMatcher getCredentialsMatcher() {
		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher("MD5");
		hashedCredentialsMatcher.setHashIterations(1024);
		// 这一行决定hex还是base64 false 指定用base64解密
		hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
		return hashedCredentialsMatcher;
	}

	/**
	 * 生成切面的类 需要注入security manager
	 * @param securityManager
	 * @return
	 */
	@Bean(name = "getAuthorizationAttributeSourceAdvisor")
	public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	/**
	 * 配置ehcache
	 */
	@Bean(name = "getEhCacheManager")
	public EhCacheManager getEhCacheManager(){
		EhCacheManager ehCacheManager = new EhCacheManager();
		ehCacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
		return ehCacheManager;
	}

	/**
	 * 开启后台自定义realm缓存
	 * @return
	 */
	@Bean(name = "getBackUserRealmConfig")
	public BackUserRealmConfig getBackUserRealmConfig() {
		BackUserRealmConfig backRealm = new BackUserRealmConfig();
		//开启缓存
		backRealm.setCachingEnabled(true);
		//启用身份验证缓存，即缓存AuthenticationInfo信息，默认false
		backRealm.setAuthenticationCachingEnabled(true);
		//缓存AuthenticationInfo信息的缓存名称 在ehcache-shiro.xml中有对应缓存的配置
		backRealm.setAuthenticationCacheName("backAuthenticationCache");
		//启用授权缓存，即缓存AuthorizationInfo信息，默认false
		backRealm.setAuthorizationCachingEnabled(true);
		//缓存AuthorizationInfo信息的缓存名称  在ehcache-shiro.xml中有对应缓存的配置
		backRealm.setAuthorizationCacheName("backAuthorizationCache");
		backRealm.setCredentialsMatcher(getCredentialsMatcher());
		return backRealm;
	}

	/**
	 * 开启前台自定义realm缓存
	 * @return
	 */
	@Bean(name = "getAppUserRealmConfig")
	public AppUserRealmConfig getAppUserRealmConfig() {
		AppUserRealmConfig appRealm = new AppUserRealmConfig();
		//开启缓存
		appRealm.setCachingEnabled(true);
		//启用身份验证缓存，即缓存AuthenticationInfo信息，默认false
		appRealm.setAuthenticationCachingEnabled(true);
		//缓存AuthenticationInfo信息的缓存名称 在ehcache-shiro.xml中有对应缓存的配置
		appRealm.setAuthenticationCacheName("appAuthenticationCache");
		//启用授权缓存，即缓存AuthorizationInfo信息，默认false
		appRealm.setAuthorizationCachingEnabled(true);
		//缓存AuthorizationInfo信息的缓存名称  在ehcache-shiro.xml中有对应缓存的配置
		appRealm.setAuthorizationCacheName("appAuthorizationCache");
		appRealm.setCredentialsMatcher(getCredentialsMatcher());
		return appRealm;
	}

	/**
	 * cookie对象
	 * @return
	 */
	@Bean(name = "getAppRememberMeCookie")
	public SimpleCookie getAppRememberMeCookie(){
		SimpleCookie simpleCookie = new SimpleCookie("appRememberMe");
		//防止xss攻击
		simpleCookie.setHttpOnly(true);
		//设置有效期时间30天
		simpleCookie.setMaxAge(24*60*60);
		return simpleCookie;
	}

	@Bean(name = "getRememberMeManager")
	public CookieRememberMeManager getRememberMeManager(){
		CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
		cookieRememberMeManager.setCookie(getAppRememberMeCookie());
		//rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
		cookieRememberMeManager.setCipherKey(Base64.decode("2AvVhsUs0FSA3SDFAda=="));
		return cookieRememberMeManager;
	}

	@Bean(name = "getMethodInvokingFactoryBean")
	public MethodInvokingFactoryBean getMethodInvokingFactoryBean(Authenticator authenticator, TokenSessionManageConfig sessionManager){
		MethodInvokingFactoryBean factoryBean = new MethodInvokingFactoryBean();
		factoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
		factoryBean.setArguments(new Object[]{getDefaultWebSecurityManager(authenticator, sessionManager)});
		return factoryBean;
	}

}
