package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 */
@Data
public class FlatCarCancelParam {

    @ApiModelProperty("任务ID")
    private String taskId;

    @ApiModelProperty("流程实例ID")
    private String instanceId;

    @ApiModelProperty("取消任务ID")
    private String cancelTaskId;

    @ApiModelProperty("司机是否开始执行")
    private String isExecute;

    @ApiModelProperty("取消业务Id")
    private String cancelBusinessKey;

    @ApiModelProperty("取消的原因")
    private String cancelReason;

    @ApiModelProperty("司机如何操作")
    private String driverOperation;
}
