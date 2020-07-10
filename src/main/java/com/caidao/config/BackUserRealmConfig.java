package com.caidao.config;

import com.caidao.pojo.SysUser;
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

import java.util.HashSet;
import java.util.List;

/**
 * 后端PC页面realm登录授权使用
 * @author tom
 * @since 2020-5-12
 */
@Configuration
public class BackUserRealmConfig extends AuthorizingRealm {
	
	@Autowired
	private SysUserService sysUserService;

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

	/**
	 * 重写方法,清除当前用户的的 授权缓存
	 * @param principals
	 */
	@Override
	public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
		super.clearCachedAuthorizationInfo(principals);
	}

	/**
	 * 重写方法，清除当前用户的 认证缓存
	 * @param principals
	 */
	@Override
	public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
		super.clearCachedAuthenticationInfo(principals);
	}

	/**
	 * 自定义方法：清除所有的  认证缓存  和 授权缓存
	 */
	public void clearCache(){
		PrincipalCollection principals = SecurityUtils.getSubject().getPrincipals();
		super.clearCache(principals);
	}


}
