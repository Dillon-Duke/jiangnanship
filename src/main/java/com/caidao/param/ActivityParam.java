package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tom
 * @since 2020-06-05
 */
@Data
public class ActivityParam {

    @ApiModelProperty("已发布的工作流Id")
    public String DeploymentId;

    @ApiModelProperty("是否强制执行该操作")
    public boolean isFoucede;

    @ApiModelProperty("流程定义名称")
    public String processDefinitionName;

    @ApiModelProperty("工作流当前任务名称")
    public String taskName;

}
