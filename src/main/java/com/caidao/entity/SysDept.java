package com.caidao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Dillon
 * @since 2020-05-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="SysDept对象", description="")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Integer deptId;

    @ApiModelProperty(value = "部门名称")
    private String deptName;

    @ApiModelProperty(value = "部门编号")
    private Integer deptCode;

    @ApiModelProperty(value = "父级部门ID，最高级部门ID为0")
    private Integer parentId;

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

    @ApiModelProperty(value = "是否可用 1：可用 0：禁用")
    private Integer state;


}
