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
 * @since 2020-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppCommonMsg对象", description="")
public class AppCommonMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增Id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "消息名称")
    private String massageName;

    @ApiModelProperty(value = "消息详情")
    private String massageDetail;

    @ApiModelProperty(value = "创建人Id")
    private Integer createId;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "文件资源路径")
    private String fileResource;

    @ApiModelProperty(value = "是否发布 1：发布 0 不发布")
    private Integer isPublish;

    @ApiModelProperty(value = "是否删除 1:正常 0 删除")
    private Integer state;

}
