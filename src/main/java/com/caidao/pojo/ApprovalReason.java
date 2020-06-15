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
 * <p>
 * 
 * </p>
 *
 * @author Dillon
 * @since 2020-06-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ApplyReason对象", description="")
public class ApprovalReason implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增ID")
    @TableId(value = "reason_id", type = IdType.AUTO)
    private Integer reasonId;

    @ApiModelProperty(value = "业务申请ID 业务主键")
    private String requestId;

    @ApiModelProperty(value = "是否同意 1：同意 0：不同意")
    private Integer opinion;

    @ApiModelProperty(value = "原因")
    private String reason;

    @ApiModelProperty(value = "原因详情")
    @TableField("reasionDescription")
    private String reasionDescription;

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
