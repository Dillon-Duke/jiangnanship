package com.caidao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author Dillon
 * @since 2020-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppBaseMsg对象", description="")
public class AppBaseMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "接⼝版本")
    @TableField("interfaceVersion")
    private String interfaceVersion;

    @ApiModelProperty(value = "应⽤版本")
    @TableField("appVersion")
    private String appVersion;

    @ApiModelProperty(value = "客户端⼿机品牌")
    @TableField("clientBrand")
    private String clientBrand;

    @ApiModelProperty(value = "客户端⼿机型号")
    @TableField("clientModel")
    private String clientModel;

    @ApiModelProperty(value = "客户端操作系统")
    @TableField("clientOs")
    private String clientOs;

    @ApiModelProperty(value = "客户端操作系统版本")
    @TableField("clientOsVersion")
    private String clientOsVersion;

    @ApiModelProperty(value = "客户端屏幕尺⼨")
    @TableField("clientScreenSize")
    private Double clientScreenSize;

    @ApiModelProperty(value = "发送的消息内容")
    private String data;

    @ApiModelProperty("消息体加密内容")
    private String encryption;

    @ApiModelProperty(value = "操作的⽤户ID")
    @TableField("userId")
    private Integer userId;

    @ApiModelProperty(value = "发送的时间(时间戳格式)")
    @TableField("submitTime")
    private Long submitTime;

    @ApiModelProperty("UUID")
    @TableField(exist = false)
    private String uuid;

}
