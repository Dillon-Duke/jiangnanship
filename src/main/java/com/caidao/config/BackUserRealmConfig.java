package com.caidao.config;

import com.caidao.entity.SysUser;
import com.caidao.service.SysMenuService;
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.UserLoginTokenUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author tom
 * @since 2020-5-12
 */
@Configuration
public class BackUserRealmConfig extends AuthorizingRealm {

	@Autowired
	private StringRedisTemplate redisTemplate;
	
	@Autowired
	private SysUserService sysUserService;

	/**
	 * @Lazy //懒加载  先让springcontroller里面的类进行cglib加载，然后加载此类 jdk
	 * //优点 生成两个代理类 不相互影响 缺点  占用内存
	 */
	@Autowired
	@Lazy
	private SysMenuService sysMenuService;

	@Override
	public String getName() {
		return PropertyUtils.BACK_USER_REALM;
	}
	
	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

		SysUser user = (SysUser)principals.getPrimaryPrincipal();
		//获得登录用户的所有权限
		List<String> authorities = sysMenuService.getAuth2ByUslerId(user.getUserId());
		if (authorities == null || authorities.isEmpty()) {
			return null;
		}

		//更新shiro里面账号信息的过期时间
		String token = SecurityUtils.getSubject().getSession().getId().toString();
		redisTemplate.expire(PropertyUtils.USER_SESSION + token, 30, TimeUnit.MINUTES);

		simpleAuthorizationInfo.setStringPermissions(new HashSet<String>(authorities));
		return simpleAuthorizationInfo;  
	}

	/**
	 * 登录认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

		//将token转换为我们自己的token
		UserLoginTokenUtils token = (UserLoginTokenUtils) authcToken;

		SysUser user = sysUserService.getUserByUsername(token.getPrincipal().toString());
		if (user == null) {
			return null;
		}

		//获取数据库中的盐值
		ByteSource byteSource = ByteSource.Util.bytes(user.getUserSalt().getBytes());
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user,user.getPassword(),byteSource,getName());
		return simpleAuthenticationInfo;
	}

}
