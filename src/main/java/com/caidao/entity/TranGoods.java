package com.caidao.entity;

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
 * @since 2020-06-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TranGoods对象", description="")
public class TranGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增")
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Integer goodsId;

    @ApiModelProperty(value = "工程编号")
    private String proCode;

    @ApiModelProperty(value = "物品类别")
    private String goodsType;

    @ApiModelProperty(value = "物品编码")
    private Integer goodsCode;

    @ApiModelProperty(value = "物品重量")
    private Double goodsWeight;

    @ApiModelProperty(value = "物品长")
    private Double goodsLength;

    @ApiModelProperty(value = "物品宽")
    private Double goodsWidth;

    @ApiModelProperty(value = "物品高")
    private Double goodsHigh;

    @ApiModelProperty(value = "物品图片名称")
    private String goodsImg;

    @ApiModelProperty(value = "物品图片真实路径")
    private String goodsSource;

    @ApiModelProperty(value = "是否为大型分段设备1：是 0 否")
    private Integer isBigGoods;

    @ApiModelProperty(value = "是否超宽高  1：是 0 否")
    private Integer isOverSize;

    @ApiModelProperty(value = "预留1")
    private String reserve1;

    @ApiModelProperty(value = "预留2")
    private String reserve2;

    @ApiModelProperty(value = "预留3")
    private String reserve3;

    @ApiModelProperty(value = "预留4")
    private String reserve4;

    @ApiModelProperty(value = "预留5")
    private String reserve5;

    @ApiModelProperty(value = "创建日期")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人")
    private Integer createId;

    @ApiModelProperty(value = "更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人")
    private Integer updateId;

    @ApiModelProperty(value = "是否可用 1：正常 0：禁用")
    private Integer state;


}
