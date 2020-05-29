package com.caidao.config;

import com.caidao.util.PropertyUtils;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author tom
 * @since 2020-5-12
 */

@Configuration
public class TokenSessionManageConfig extends DefaultWebSessionManager {

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
