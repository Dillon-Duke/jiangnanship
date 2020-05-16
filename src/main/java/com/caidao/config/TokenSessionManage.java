package com.caidao.config;

import java.io.Serializable;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.caidao.util.PropertyUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenSessionManage extends DefaultWebSessionManager {

	/**
	 * 使用自定义的token
	 */
	@Override
	protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
		String token = WebUtils.toHttp(request).getHeader(PropertyUtils.TOKEN_HEADER);
		if (!StringUtils.hasText(token)) {
			token = UUID.randomUUID().toString();
		}
		return token;
	}
}
