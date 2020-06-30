package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 */
@Data
public class ActivityQueryParam {

    @ApiModelProperty("是否强制执行该操作")
    private boolean isForced = false;

    @ApiModelProperty("流程定义名称")
    private String processDefinitionName;

    @ApiModelProperty("已发布的工作流Id")
    private String deploymentId;

    @ApiModelProperty("工作流任务人员姓名")
    private String userName;

    @ApiModelProperty("工作流当前任务名称")
    private String taskName;

    @ApiModelProperty("工作流当前任务状态")
    private String taskState;

    @ApiModelProperty("工作流历史任务开始时间")
    private String historyStartTime;

    @ApiModelProperty("工作流历史任务结束时间")
    private String historyEndTime;

}
