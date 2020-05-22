package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import javassist.SerialVersionUID;
import lombok.Data;

import java.io.Serializable;

/**
 * @author tom
 */

@Data
public class UsernamePasswordParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("用户旧密码")
    private String password;

    @ApiModelProperty("用户新密码")
    private String newPassword;


}
