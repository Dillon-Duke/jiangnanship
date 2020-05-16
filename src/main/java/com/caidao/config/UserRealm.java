package com.caidao.config;

import java.util.HashSet;
import java.util.List;

import com.caidao.entity.SysUser;
import com.caidao.service.SysMenuService;
import com.caidao.service.SysUserService;
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

import lombok.extern.slf4j.Slf4j;


@Configuration 
@Slf4j
public class UserRealm extends AuthorizingRealm {
	
	@Autowired
	private SysUserService sysUserService;
	
	@Autowired
	@Lazy //懒加载  先让springcontroller里面的类进行cglib加载，然后加载此类 jdk 
				//优点 生成两个代理类 不相互影响 缺点  占用内存
	private SysMenuService sysMenuService;
	
	/**
	 * 授权
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		
		SysUser user = (SysUser)principals.getPrimaryPrincipal();
		//获得登录用户的所有权限
		List<String> authorities = sysMenuService.getAuth2ByUslerId(user.getUserId());
		if (authorities == null || authorities.isEmpty()) {
			return null;
		}
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		simpleAuthorizationInfo.setStringPermissions(new HashSet<String>(authorities));
		return simpleAuthorizationInfo;  
	}

	/**
	 * 登录认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		
		log.info("{}登录了",token.getPrincipal().toString());

		SysUser user = sysUserService.getUserByUsername(token.getPrincipal().toString());
		if (user == null) {
			return null;
		}
		
		//动态的生成盐值
		ByteSource byteSource = ByteSource.Util.bytes(user.getUserSalt());
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user,user.getPassword(),byteSource,user.getUsername());
		return simpleAuthenticationInfo;
	}

}
