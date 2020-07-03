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
import java.time.LocalDateTime;

/**
 * @author Dillon
 * @since 2020-06-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DeptUserCar对象", description="")
public class DeptUserCar implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "业务Id")
    private Integer businessKey;

    @ApiModelProperty(value = "任务Id")
    @TableField(exist = false)
    private Integer taskId;

    @ApiModelProperty(value = "司机Id")
    private Integer driverId;

    @ApiModelProperty(value = "司机名称")
    private String driverName;

    @ApiModelProperty(value = "操作员Id")
    private Integer operatorId;

    @ApiModelProperty(value = "操作员名称")
    private String operatorName;

    @ApiModelProperty(value = "车辆Id")
    private Integer carId;

    @ApiModelProperty(value = "车辆牌照")
    private Integer carPlant;

    @ApiModelProperty(value = "排班班次")
    private String workShift;

    @ApiModelProperty(value = "排班工单号")
    private String workNum;

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;


}
