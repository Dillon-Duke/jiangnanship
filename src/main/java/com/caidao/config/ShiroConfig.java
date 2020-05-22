package com.caidao.config;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
	 * @param realm
	 * @param sessionManager
	 * @return
	 */
@Bean
	public DefaultWebSecurityManager defaultWebSecurityManager(UserRealm realm, CredentialsMatcher credentialsMatcher, TokenSessionManage sessionManager, @Qualifier("SessionDAO") SessionDAO redisSessionDAO) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();	
		securityManager.setRealm(realm);
		//设置盐值校验
		realm.setCredentialsMatcher(credentialsMatcher);
		//设置将登录信息放在redis里面
		sessionManager.setSessionDAO(redisSessionDAO);
		securityManager.setSessionManager(sessionManager);
		return securityManager;
	}

	/**
	 * 设置盐值
	 * @return
	 */
	@Bean
	public CredentialsMatcher getCredentialsMatcher() {
		HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher("MD5");
		hashedCredentialsMatcher.setHashIterations(1024);
		return hashedCredentialsMatcher;
	}

	/**
	 * 设置过滤和不需要过滤的页面
	 * @return
	 */
	@Bean
	public DefaultShiroFilterChainDefinition defaultShiroFilterChainDefinition() {
		DefaultShiroFilterChainDefinition defaultShiroFilterChainDefinition = new DefaultShiroFilterChainDefinition();

		Map<String, String> HashMap = new HashMap<>(8);

		//配置对swigger权限放开
		HashMap.put("/doc.html", "anon");
		HashMap.put("/webjars/**", "anon");
		HashMap.put("/v2/**", "anon");
		HashMap.put("/swagger-resources/**", "anon");

		//系统自己需要放开的页面路径
		HashMap.put("/captcha.jpg", "anon");
		HashMap.put("/login", "anon");
		HashMap.put("/logout", "logout");
		HashMap.put("/sys/menu/nav", "anon");

		defaultShiroFilterChainDefinition.addPathDefinitions(HashMap);

		//表示所有的路径不拦截 调试使用
		defaultShiroFilterChainDefinition.addPathDefinition("/**", "anon");

		//表示需要认证才可以访问
//		defaultShiroFilterChainDefinition.addPathDefinition("/**", "authc");

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
	
	/**
	 * 生成切面的类 需要注入security manager
	 * @param securityManager
	 * @return
	 */
	@Bean   
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}
	
	/**
	 * 使用切面 强制使用cglib创建，因为controller里面没有接口
	 * @return
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}

}
