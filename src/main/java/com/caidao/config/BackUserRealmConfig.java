package com.caidao.config;

import com.caidao.entity.SysUser;
import com.caidao.service.SysMenuService;
import com.caidao.service.SysUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.UserLoginTokenUtils;
import org.apache.shiro.authc.*;
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
 * @author tom
 * @since 2020-5-12
 */
@Configuration
public class BackUserRealmConfig extends AuthorizingRealm {
	
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
