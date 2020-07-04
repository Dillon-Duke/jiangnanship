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
 * @author Dillon
 * @since 2020-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppMassage对象", description="")
public class AppTasksMassage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "消息描述名称")
    private String massageName;

    @ApiModelProperty(value = "任务ID")
    private Integer taskId;

    @ApiModelProperty(value = "关联的用户名称")
    private String deptUsername;

    @ApiModelProperty(value = "是否已读 1：未读 0：已读")
    private Integer isRead;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;


}
