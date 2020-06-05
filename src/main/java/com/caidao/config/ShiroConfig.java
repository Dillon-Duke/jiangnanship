package com.caidao.config;

import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	 * @param AuthConfig
	 * @param sessionManager
	 * @return
	 */
	@Bean
	public DefaultWebSecurityManager defaultWebSecurityManager(CustomerAuthrizerConfig AuthConfig, BackUserRealmConfig backRealm, AppUserRealmConfig appRealm, Authenticator authenticator, CredentialsMatcher credMatcher, TokenSessionManageConfig sessionManager, @Qualifier("SessionDAO") SessionDAO redisSessionDAO) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();

		securityManager.setAuthenticator(authenticator);

		//不同的角色对应不同的认证方法  这个方法一定要放在前面，很狗血的问题，放到后面就会报异常 No realms have been configured!  One or more realms must be present to execute an authorization oper
		securityManager.setAuthorizer(AuthConfig);
		List<Realm> list = new ArrayList<>();
		list.add(backRealm);
		list.add(appRealm);

		securityManager.setRealms(list);

		//设置通过盐值校验
		backRealm.setCredentialsMatcher(credMatcher);
		appRealm.setCredentialsMatcher(credMatcher);
		//设置将登录信息放在redis里面
		sessionManager.setSessionDAO(redisSessionDAO);
		securityManager.setSessionManager(sessionManager);
		return securityManager;
	}

	/**
	 * 配置自定义的权限认证，不同的用户进入不同的realm里面进行认证
	 */
	@Bean
	public CustomerAuthrizerConfig customerAuthrizerConfig(){
		CustomerAuthrizerConfig customerAuthrizerConfig = new CustomerAuthrizerConfig();
		return customerAuthrizerConfig;
	}

	/**
	 * 配置使用自定义认证器，可以实现多Realm认证，并且可以指定特定Realm处理特定类型的验证
	 */
	@Bean
	public CustomRealmAuthenticatorConfig customRealmAuthenticatorConfig(AppUserRealmConfig appUserRealm , BackUserRealmConfig userRealm , FirstSuccessfulStrategy successfulStrategy){
		CustomRealmAuthenticatorConfig customRealmAuthenticatorConfig = new CustomRealmAuthenticatorConfig();

		//设置刷新缓存
		userRealm.setCachingEnabled(true);
		appUserRealm.setCachingEnabled(true);

		//将所有的realm放在shiro中
		Map<String, Object> hashMap = new HashMap<>(2);
			hashMap.put("appUserRealm",appUserRealm);
			hashMap.put("backUserRealm",userRealm);
		customRealmAuthenticatorConfig.setDefinedRealms(hashMap);

		//配置认证策略，只要有一个Realm认证成功即可，并且返回所有认证成功信息
		customRealmAuthenticatorConfig.setAuthenticationStrategy(successfulStrategy);
		return customRealmAuthenticatorConfig;
	}

	/**
	 * 将shiro认证中的第一个认证成功策略放在spring中
	 * @return
	 */
	@Bean
	public FirstSuccessfulStrategy firstSuccessfulStrategy(){
		FirstSuccessfulStrategy successfulStrategy = new FirstSuccessfulStrategy();
		return successfulStrategy;
	}

	/**
	 * 设置盐值
	 * @return
	 */
	@Bean
	public CredentialsMatcher getCredentialsMatcher() {
		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher("MD5");
		hashedCredentialsMatcher.setHashIterations(1024);
		// 这一行决定hex还是base64 false 指定用base64解密
		hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);
		return hashedCredentialsMatcher;
	}

	/**
	 * 设置过滤和不需要过滤的页面
	 * @return
	 */
	@Bean
	public DefaultShiroFilterChainDefinition defaultShiroFilterChainDefinition() {
		DefaultShiroFilterChainDefinition defaultShiroFilterChainDefinition = new DefaultShiroFilterChainDefinition();

		Map<String, String> hashMap = new HashMap<>(12);

		//配置对swigger权限放开
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
	 * 将shiro中登录信息保存到redis里面
	 * @param redisManager
	 * @return
	 */
	@Bean("SessionDAO")
	public SessionDAO redisSessionDao(IRedisManager redisManager) {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(redisManager);
		return redisSessionDAO;
	}
	
	/**
	 * 配置shiro redis的地址还有端口
	 * @return
	 */
	@Bean
	public IRedisManager getRedisManager() {
		RedisManager redisManager = new RedisManager();
		redisManager.setHost(host+":"+port);
//		设置redis连接池使用
//		redisManager.setJedisPool(jedisPool);
		return redisManager;
	}

}
