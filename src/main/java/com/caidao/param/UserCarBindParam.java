package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author tom
 */
@Data
public class UserCarBindParam {

    @ApiModelProperty("司机Id")
    public Integer driverId;

    @ApiModelProperty("司机名称")
    public String driverName;

    @ApiModelProperty("操作员Id")
    public Integer[] operateId;

    @ApiModelProperty("操作员名称")
    public String[] operateName;

    @ApiModelProperty("车辆Id")
    public Integer carId;

    @ApiModelProperty("绑定班次")
    public String workShift;

    @ApiModelProperty("开始时间")
    public LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    public LocalDateTime endTime;
}
