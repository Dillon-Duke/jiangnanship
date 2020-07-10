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
 * @since 2020-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LunchImage对象", description="")
public class LunchImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "文件名称")
    private String fileImage;

    @ApiModelProperty(value = "文件真实路径")
    private String sourceImage;

    @ApiModelProperty(value = "自定义名称")
    private String customName;

    @ApiModelProperty(value = "创建人Id")
    private Integer createId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "是否应用到app首页 1：应用 0：未应用")
    private Integer isUse;

    @ApiModelProperty(value = "是否删除 1：正常 0：删除")
    private Integer state;

}
