package com.caidao.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DeptRole对象", description="")
public class DeptRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增长")
    @TableId(value = "role_id", type = IdType.AUTO)
    private Integer roleId;

    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @ApiModelProperty(value = "角色所属部门ID")
    private String roleDept;

    @ApiModelProperty(value = "角色描述")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人")
    private Integer createId;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人")
    private Integer updateId;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "角色关联部门")
    @TableField(exist = false)
    private List<Integer> deptIdList = new ArrayList<Integer>(0);

    @ApiModelProperty(value = "角色关联权限")
    @TableField(exist = false)
    private List<Integer> powerIdList = new ArrayList<Integer>(0);

}
