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
 * @since 2020-06-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TranGoods对象", description="")
public class PlatformGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增")
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Integer goodsId;

    @ApiModelProperty(value = "工程编号")
    private String proCode;

    @ApiModelProperty(value = "物品类别")
    private String goodsType;

    @ApiModelProperty(value = "物品编码 (物品的编码)")
    private String goodsCode;

    @ApiModelProperty(value = "物品重量")
    private Integer goodsWeight;

    @ApiModelProperty(value = "物品长")
    private Integer goodsLength;

    @ApiModelProperty(value = "物品宽")
    private Integer goodsWidth;

    @ApiModelProperty(value = "物品高")
    private Integer goodsHigh;

    @ApiModelProperty(value = "物品图片名称")
    private String fileImage;

    @ApiModelProperty(value = "物品图片真实路径")
    private String sourceImage;

    @ApiModelProperty(value = "物品目前所在位置ID")
    private String goodsPositionId;

    @ApiModelProperty(value = "物品目前所在地的GPS位置")
    private String goodsPositionGps;

    @ApiModelProperty(value = "是否为大型分段设备1：是 0 否")
    private Integer isBigGoods;

    @ApiModelProperty(value = "是否超宽高  1：是 0 否")
    private Integer isOverSize;

    @ApiModelProperty(value = "是否绑定 1：绑定 0：未绑定")
    private Integer isBinder;

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
