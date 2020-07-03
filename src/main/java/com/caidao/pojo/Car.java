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
    private String carNumber;

    @ApiModelProperty(value = "车辆牌照")
    private String carPlate;

    @ApiModelProperty(value = "车辆品牌名称")
    private String carModel;

    @ApiModelProperty(value = "车辆长 ：单位厘米")
    private Integer carLength;

    @ApiModelProperty(value = "车辆宽 ：单位厘米")
    private Integer carWight;

    @ApiModelProperty(value = "车辆高 ：单位厘米")
    private Integer carHeight;

    @ApiModelProperty(value = "车辆图片文件名称")
    private String fileImage;

    @ApiModelProperty(value = "车辆图片资源路径")
    private String sourceImage;

    @ApiModelProperty(value = "空车重量 ：单位千克")
    private Integer emptyWeight;

    @ApiModelProperty(value = "荷载重量 ：单位千克")
    private Integer fullWeight;

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
