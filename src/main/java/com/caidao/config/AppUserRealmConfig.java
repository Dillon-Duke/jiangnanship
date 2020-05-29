package com.caidao.config;

import com.caidao.entity.DeptUser;
import com.caidao.entity.SysUser;
import com.caidao.service.DeptConfigService;
import com.caidao.service.DeptUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.UserLoginTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.List;

/**
 * @author tom
 * @since 2020-05-28
 */
@Configuration
@Slf4j
public class AppUserRealmConfig extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(AppUserRealmConfig.class);

    @Autowired
    private DeptUserService deptUserService;

    /**
     * @Lazy //懒加载  先让springcontroller里面的类进行cglib加载，然后加载此类 jdk
     * //优点 生成两个代理类 不相互影响 缺点  占用内存
     */
    @Autowired
    @Lazy
    private DeptConfigService deptConfigService;

    @Override
    public String getName() {
        return PropertyUtils.APP_USER_REALM;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        SysUser user = (SysUser)principals.getPrimaryPrincipal();
        //获得登录用户的所有权限
        List<String> powerByUserId = deptConfigService.getPowerByUserId(user.getUserId());
        if (powerByUserId == null || powerByUserId.isEmpty()) {
            return null;
        }
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        simpleAuthorizationInfo.setStringPermissions(new HashSet<>(powerByUserId));
        return simpleAuthorizationInfo;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

        log.info("{}登录了",authcToken.getPrincipal().toString());
        //将token转换为我们自己的token
        UserLoginTokenUtils token = (UserLoginTokenUtils) authcToken;
        DeptUser user = deptUserService.getUserByUsername(token.getPrincipal().toString());
        if (user == null) {
            return null;
        }

        //动态的生成盐值
        ByteSource byteSource = ByteSource.Util.bytes(user.getUserSalt());
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user,user.getPassword(),byteSource,getName());
        return simpleAuthenticationInfo;
    }

}
