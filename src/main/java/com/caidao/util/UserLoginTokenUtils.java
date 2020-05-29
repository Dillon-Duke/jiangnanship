package com.caidao.util;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 自定义shiro-token重写类,用于多类型用户校验
 * @author tom
 * @since 2020-05-28
 */
public class UserLoginTokenUtils extends UsernamePasswordToken {

    private static final long serialVersionUID = 1L;

    private String loginType;

    public UserLoginTokenUtils() {}

    public UserLoginTokenUtils(final String username, final String password, final String loginType) {
        super(username, password);
        this.loginType = loginType;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

}
