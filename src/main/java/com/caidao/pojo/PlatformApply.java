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
 * @author Dillon
 * @since 2020-06-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="PlatformRequestSubmit对象", description="")
public class PlatformApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增")
    @TableId(value = "prs_id", type = IdType.AUTO)
    private Integer prsId;

    @ApiModelProperty(value = "申请部门ID")
    private Integer requestDepartmentId;

    @ApiModelProperty(value = "申请部门名称")
    private String requestDepartmentName;

    @ApiModelProperty(value = "驳运部门ID")
    private Integer lighteningDepartmentId;

    @ApiModelProperty(value = "驳动部门名称")
    private String lighteningDepartmentName;

    @ApiModelProperty(value = "起始点定位ID")
    private Long startPositionId;

    @ApiModelProperty(value = "起始点的GPS经纬度，以逗号分割")
    private String startPositionGps;

    @ApiModelProperty(value = "结束点定位ID")
    private Long endPositionId;

    @ApiModelProperty(value = "结束点GPS经纬度，以逗号分割")
    private String endPositionGps;

    @ApiModelProperty(value = "起始时间(时间戳格式，到秒)")
    private Long startTime;

    @ApiModelProperty(value = "结束时间(时间戳格式，到秒)")
    private Long endTime;

    @ApiModelProperty(value = "对象ID")
    private Integer objectId;

    @ApiModelProperty(value = "对象重量")
    private Integer objectWight;

    @ApiModelProperty(value = "工作内容")
    private String jobContent;

    @ApiModelProperty(value = "联系人ID")
    private Integer contactsId;

    @ApiModelProperty(value = "备注")
    private String memo;

    @ApiModelProperty(value = "重要程度 1:重要 0 ： 不重要")
    private Integer importance;

    @ApiModelProperty(value = "申请单类型")
    private Integer requestType;

    @ApiModelProperty(value = "申请工单号")
    private String requestOddNumber;

    @ApiModelProperty(value = "审批状态 0：未提交 1：审批中 2：审批完成 3;审批未通过")
    private Integer applyState;

    @ApiModelProperty(value = "返回的请求名称")
    private String requestName;

    @ApiModelProperty(value = "申请人姓名")
    private String applyName;

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

    @ApiModelProperty(value = "按钮状态 保存任务:save 提交任务:submit")
    @TableField(exist = false)
    private String operateState;
}
