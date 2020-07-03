package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author tom
 */
@Data
public class FlatCarAdjustmentParam {

    @ApiModelProperty(value = "需要调整的业务Id")
    private Integer adjustmentBusinessKey;

    @ApiModelProperty(value = "需要调整的原因")
    private String adjustmentReason;

    @ApiModelProperty(value = "调整后司机的操作提示")
    private String driverTips;

    @ApiModelProperty(value = "需要调整的开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "需要调整的结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "结束点定位ID")
    private Long endPositionId;

    @ApiModelProperty(value = "结束点GPS经纬度，以逗号分割")
    private String endPositionGps;

    @ApiModelProperty(value = "需要调整的车辆")
    private String[] carId;

    @ApiModelProperty(value = "需要调整的司机Id")
    private Integer driverId;

    @ApiModelProperty(value = "需要调整的司机姓名")
    private String driverName;

    @ApiModelProperty(value = "需要调整的操作员Id")
    private Integer[] operatorId;

    @ApiModelProperty(value = "需要调整的操作员姓名")
    private String[] operatorName;

}
