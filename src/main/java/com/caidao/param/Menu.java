package com.caidao.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Menu implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("菜单id")
	private Integer menuId;
	
	@ApiModelProperty("菜单名称")
	private String name;
	
	@ApiModelProperty("菜单地址")
	private String url;
	
	@ApiModelProperty("子菜单列表")
	private List<Menu> list = new ArrayList<Menu>(0);

}
