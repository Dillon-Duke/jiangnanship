package com.caidao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2020-06-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="CarPlatformApply对象", description="")
public class CarPlatformApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增Id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "车辆Id")
    private Integer carId;

    @ApiModelProperty(value = "平板车申请Id")
    private Integer prsId;

    @ApiModelProperty(value = "绑定开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "绑定结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "绑定人Id")
    private Integer bindUserId;
}
