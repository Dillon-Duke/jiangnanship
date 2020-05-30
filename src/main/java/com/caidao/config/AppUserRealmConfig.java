package com.caidao.config;

import com.caidao.entity.DeptUser;
import com.caidao.service.DeptConfigService;
import com.caidao.service.DeptUserService;
import com.caidao.util.PropertyUtils;
import com.caidao.util.UserLoginTokenUtils;
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

/**
 * @author tom
 * @since 2020-05-28
 */
@Configuration
public class AppUserRealmConfig extends AuthorizingRealm {


    @Autowired
    private StringRedisTemplate redisTemplate;
    //TODO 在用户进行前后端交互的时候需要刷新redis里面的存储时间

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

        DeptUser user = (DeptUser)principals.getPrimaryPrincipal();
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

       //将token转换为我们自己的token
       UserLoginTokenUtils token = (UserLoginTokenUtils) authcToken;
       DeptUser deptUser = deptUserService.getUserByUsername(token.getPrincipal().toString());
       if (deptUser == null) {
           return null;
       }

        //获取数据库中的盐值
        ByteSource byteSource = ByteSource.Util.bytes(deptUser.getUserSalt().getBytes());
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(deptUser,deptUser.getPassword(),byteSource,getName());
        return simpleAuthenticationInfo;
    }

}
