package com.caidao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ToString
@ApiModel(value="DeptUser对象", description="")
public class DeptUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增长ID")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realName;

    @ApiModelProperty(value = "密码 加密密码")
    private String password;

    @ApiModelProperty(value = "密码加密盐值 由uuid生成")
    private String userSalt;

    @ApiModelProperty(value = "1:男，2:女")
    private Integer sex;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "讨论工号长度、生成规则")
    private Integer jobNum;

    @ApiModelProperty(value = "个人头像文件名称")
    private String fileImage;

    @ApiModelProperty(value = "头像资源库文件名称+路径")
    private String sourceImage;

    @ApiModelProperty(value = "用户对应的角色id")
    private Integer userRoleId;

    @ApiModelProperty(value = "用户对应的角色名称")
    private String userRoleName;

    @ApiModelProperty(value = "用户对应的部门id")
    private Integer userDeptId;

    @ApiModelProperty(value = "用户对应的部门名称")
    private String userDeptName;

    @ApiModelProperty(value = "创建日期")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人")
    private Integer createId;

    @ApiModelProperty(value = "更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人")
    private Integer updateId;

    @ApiModelProperty(value = "是否可用 1：可用 0：停用")
    private Integer state;

    @ApiModelProperty(value = "用户角色对应的列表")
    @TableField(exist = false)
    private List<Integer> roleIdList = new ArrayList<Integer>(0);

}
