package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 * @since 2020-5-12
 */

@Data
public class UserParam {
	
	@ApiModelProperty("用戶账号")
	private String principal;
	
	@ApiModelProperty("用户密码")
	private String credentials;

	@ApiModelProperty("用户新密码")
	private String newCredentials;
	
	@ApiModelProperty("验证码")
	private String imageCode;

	@ApiModelProperty("手机号")
	private String phone;
	
	@ApiModelProperty("uuid对比")
	private String sessionUuid;

}
