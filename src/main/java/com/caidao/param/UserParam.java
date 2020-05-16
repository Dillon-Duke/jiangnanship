package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserParam {
	
	@ApiModelProperty("用戶账号")
	private String principal;
	
	@ApiModelProperty("用户密码")
	private String credentials;
	
	@ApiModelProperty("验证码")
	private String imageCode;
	
	@ApiModelProperty("uuid对比")
	private String sessionUUID;

}
