package com.caidao.controller.front.app;

import com.caidao.common.MyResponseEntity;
import com.caidao.pojo.PlatformGoods;
import com.caidao.service.PlatformGoodsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author tom
 * @since 2020-06-11
 */
@RestController
@RequestMapping("/appTrans/goods")
public class AppTransGoodsController {

    @Autowired
    private PlatformGoodsService platformGoodsService;

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("/chooseById/{id}")
    @ApiOperation("通过id查询运输分段信息")
    public MyResponseEntity<PlatformGoods> chooseById(@PathVariable("id") Integer id){
        PlatformGoods platformGoods = platformGoodsService.getById(id);
        return MyResponseEntity.ok(platformGoods);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @PostMapping("/chooseByType")
    @ApiOperation("按照类型选择运输分段信息")
    public MyResponseEntity<PlatformGoods> chooseByType(@RequestBody PlatformGoods platformGoods){
        return MyResponseEntity.ok().build();
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @PostMapping("/chooseByInput")
    @ApiOperation("通过手动输入名称选择运输分段信息")
    public MyResponseEntity<PlatformGoods> chooseByInput(@RequestBody PlatformGoods platformGoods){
        return MyResponseEntity.ok().build();
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @PostMapping("/chooseByMap")
    @ApiOperation("通过id查询运输分段信息")
    public MyResponseEntity<PlatformGoods> chooseByMap(@RequestBody PlatformGoods platformGoods){
        return MyResponseEntity.ok().build();
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @PostMapping("/chooseByMapQrCode")
    @ApiOperation("通过id查询运输分段信息")
    public MyResponseEntity<PlatformGoods> chooseByMapQrCode(@RequestBody PlatformGoods platformGoods){
        return MyResponseEntity.ok().build();
    }

}
