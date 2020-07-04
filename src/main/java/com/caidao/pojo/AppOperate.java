package com.caidao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Dillon
 * @since 2020-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppOperate对象", description="")
public class AppOperate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增Id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "常用操作的权限对应 ，做Id使用")
    private String operateAuthorisation;

    @ApiModelProperty(value = "常用操作的名字")
    private String operateName;

    @ApiModelProperty(value = "操作用户的Id")
    private Integer userId;

    @ApiModelProperty(value = "统计操作次数")
    private Integer times;


}
