package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 */
@Data
public class ActivityQueryParam {

    @ApiModelProperty("已发布的工作流Id")
    private String deploymentId;

    @ApiModelProperty("工作流任务人员姓名")
    private String userName;

    @ApiModelProperty("工作流当前任务名称")
    public String taskName;

    @ApiModelProperty("工作流当前任务状态")
    public String taskState;

    @ApiModelProperty("工作流历史任务开始时间")
    public String historyStartTime;

    @ApiModelProperty("工作流历史任务结束时间")
    public String historyEndTime;

}
