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
 * @since 2020-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DeptConfig对象", description="")
public class DeptConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "conf_id", type = IdType.AUTO)
    private Integer confId;

    @ApiModelProperty(value = "key")
    private String paramKey;

    @ApiModelProperty(value = "value")
    private String paramValue;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建日期")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人")
    private Integer createId;

    @ApiModelProperty(value = "更新日期")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人")
    private Integer updateId;

    @ApiModelProperty(value = "是否可用 1：可用 0 ：禁用")
    private Integer state;


}
