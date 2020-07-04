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
 * <p>
 * 
 * </p>
 *
 * @author Dillon
 * @since 2020-07-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="LunchImage对象", description="")
public class LunchImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String fileImage;

    private String sourceImage;

    private Integer createId;

    private LocalDateTime createDate;

    private String isUse;


}
