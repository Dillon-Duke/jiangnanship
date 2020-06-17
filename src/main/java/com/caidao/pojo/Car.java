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
 * <p>
 * 
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SysCar对象", description="")
public class Car implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "car_id", type = IdType.AUTO)
    private Integer carId;

    @ApiModelProperty(value = "车辆名称")
    private String carName;

    @ApiModelProperty(value = "车辆编号")
    private Integer carNumber;

    @ApiModelProperty(value = "车辆牌照")
    private String carPlate;

    @ApiModelProperty(value = "车辆长")
    private Double carLength;

    @ApiModelProperty(value = "车辆宽")
    private Double carWight;

    @ApiModelProperty(value = "车辆高")
    private Double carHeight;

    @ApiModelProperty(value = "车辆图片")
    private String carPhoto;

    @ApiModelProperty(value = "车辆图片资源路径")
    private String sourcePhoto;

    @ApiModelProperty(value = "空车重量")
    private Double emptyWeight;

    @ApiModelProperty(value = "荷载重量")
    private Double fullWeight;

    @ApiModelProperty(value = "绑定的任务id 默认为未绑定 默认值0")
    private Double bindTaskId;

    @ApiModelProperty(value = "1：正常，2：占用，3：维修，4：报废")
    private Integer carState;

    @ApiModelProperty(value = "创建日期")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人")
    private Integer createId;

    @ApiModelProperty(value = "更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人")
    private Integer updateId;

    @ApiModelProperty(value = "是否可用1：可用  0：禁用")
    private Integer state;

}