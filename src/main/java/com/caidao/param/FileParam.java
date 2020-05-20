package com.caidao.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.File;

/**
 * @author tom
 * @since 2020-5-12
 */

@Data
public class FileParam {

    @ApiModelProperty(value = "需要上传的文件")
    private File file;

    @ApiModelProperty(value = "可上传的文件类型")
    private String fileType;

    @ApiModelProperty(value = "可上传文件最大尺寸")
    private Long maxFileSize;

    @ApiModelProperty(value = "可上传文件最大长")
    private Long length;

    @ApiModelProperty(value = "可上传文件最大宽")
    private Long width;
}
