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
 * @author Dillon
 * @since 2020-05-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="FlatcarPlan对象", description="")
public class FlatcarPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增")
    @TableId(value = "flatcar_id", type = IdType.AUTO)
    private Integer flatcarId;

    @ApiModelProperty(value = "任务名称")
    private String jobName;

    @ApiModelProperty(value = "任务工单号")
    private String jobNumber;

    @ApiModelProperty(value = "预计开始时间")
    private LocalDateTime jobStarttime;

    @ApiModelProperty(value = "预计到达时间")
    private LocalDateTime jobEndtime;

    @ApiModelProperty(value = "申请部门")
    private String applyDept;

    @ApiModelProperty(value = "驳运部门")
    private String tranDept;

    @ApiModelProperty(value = "起始地")
    private String startAddr;

    @ApiModelProperty(value = "目的地")
    private String endAddr;

    @ApiModelProperty(value = "工作内容")
    private String content;

    @ApiModelProperty(value = "是否重要 1重要 0 不重要")
    private Integer isImportant;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系人电话")
    private Long contactPhone;

    @ApiModelProperty(value = "物品分段 ")
    private String goodsPart;

    @ApiModelProperty(value = "附件名称")
    private String fujianName;

    @ApiModelProperty(value = "附件上传的真实地址")
    private String fujianUrl;

    @ApiModelProperty(value = "预估时间")
    private Integer estTime;

    @ApiModelProperty(value = "流程实例ID")
    private Integer instenceId;

    @ApiModelProperty(value = "审批人姓名")
    private String apprName;

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

    @ApiModelProperty(value = "申请人姓名")
    private String applyName;

    @ApiModelProperty(value = "审批状态 0：未提交 1：审批中 2：审批完成 3;审批未通过")
    private Integer applyState;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createDate;

    @ApiModelProperty(value = "创建人id")
    private Integer createId;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateDate;

    @ApiModelProperty(value = "更新人id")
    private Integer updateId;

    @ApiModelProperty(value = "状态 1：可用 0 禁用")
    private Integer state;


}
