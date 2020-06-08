package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 * @since 2020-06-08
 */
@Data
public class AppDateParam<T> {

    @ApiModelProperty("接⼝版本")
    public String interfaceVersion;

    @ApiModelProperty("应⽤版本")
    public String appVersion;

    @ApiModelProperty("客户端⼿机品牌")
    public String clientBrand;

    @ApiModelProperty("客户端⼿机型号")
    public String clientModel;

    @ApiModelProperty("客户端操作系统")
    public String clientOs;

    @ApiModelProperty("客户端操作系统版本")
    public String clientOsVersion;

    @ApiModelProperty("客户端屏幕尺⼨")
    public String clientScreenSize;

    @ApiModelProperty("操作的⽤户ID")
    public String userId;

    @ApiModelProperty("发送的消息内容 ")
    public String data;

    @ApiModelProperty("消息体加密内容")
    public String encryption;

    @ApiModelProperty("发送的时间(时间戳格式)")
    public Long submitTime;
}
