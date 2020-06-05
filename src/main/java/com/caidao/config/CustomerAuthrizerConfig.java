package com.caidao.config;

import com.caidao.entity.DeptUser;
import com.caidao.entity.SysUser;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * @author tom
 * @since 2020-06-02
 */
public class CustomerAuthrizerConfig extends ModularRealmAuthorizer {

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        assertRealmsConfigured();
        Object primaryPrincipal = principals.getPrimaryPrincipal();

        for (Realm realm : getRealms()) {
            if (!(realm instanceof Authorizer)) {
                continue;
            }
            if (primaryPrincipal instanceof SysUser) {
                if (realm instanceof BackUserRealmConfig) {
                    return ((BackUserRealmConfig) realm).isPermitted(principals, permission);
                }
            }
            if (primaryPrincipal instanceof DeptUser) {
                if (realm instanceof AppUserRealmConfig) {
                    return ((AppUserRealmConfig) realm).isPermitted(principals, permission);
                }
            }

        }
        return false;
    }
}