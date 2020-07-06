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
    private String interfaceVersion;

    @ApiModelProperty(value = "应⽤版本")
    private String appVersion;

    @ApiModelProperty(value = "客户端⼿机品牌")
    private String clientBrand;

    @ApiModelProperty(value = "客户端⼿机型号")
    private String clientModel;

    @ApiModelProperty(value = "客户端操作系统")
    private String clientOs;

    @ApiModelProperty(value = "客户端操作系统版本")
    private String clientOsVersion;

    @ApiModelProperty(value = "客户端屏幕尺⼨")
    private Double clientScreenSize;

    @ApiModelProperty(value = "手机的唯一序列码")
    private String clientImel;

    @ApiModelProperty(value = "发送的时间(时间戳格式)")
    private Long submitTime;

}
