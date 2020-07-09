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
 * @since 2020-07-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppUserCommonMsg对象", description="")
public class AppUserCommonMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增Id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "主键自增Id")
    private Integer commId;

    @ApiModelProperty(value = "消息名称")
    private String massageName;

    @ApiModelProperty(value = "消息详情")
    private String massageDetail;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "用户Id")
    private Integer userId;

    @ApiModelProperty(value = "是否已读 1：未读 0：已读")
    private Integer isRead;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件资源路径")
    private String fileResource;


}
