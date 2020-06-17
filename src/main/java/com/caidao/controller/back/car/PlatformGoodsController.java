package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.SysUser;
import com.caidao.pojo.PlatformGoods;
import com.caidao.service.PlatformGoodsService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-01
 */
@RestController
@RequestMapping("/car/goods")
@Slf4j
public class PlatformGoodsController {

    public static final Logger logger = LoggerFactory.getLogger(PlatformGoodsController.class);

    @Autowired
    private PlatformGoodsService platformGoodsService;


    /**
     * 获取分页运输分段信息
     * @param page
     * @param platformGoods
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取分页运输分段信息")
    @RequiresPermissions("car:goods:page")
    public ResponseEntity<IPage<PlatformGoods>> getDoodsList(Page<PlatformGoods> page , PlatformGoods platformGoods){
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<PlatformGoods> tranGoodsPage = platformGoodsService.findSysGoodsPage(page, platformGoods);
        return ResponseEntity.ok(tranGoodsPage);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id查询运输分段信息")
    @RequiresPermissions("car:goods:info")
    public ResponseEntity<PlatformGoods> getGoodsInfoById(@PathVariable("id") Integer id){
        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);
        PlatformGoods platformGoods = platformGoodsService.getById(id);
        return ResponseEntity.ok(platformGoods);
    }

    /**
     * 更新车辆信息
     * @param platformGoods
     * @return
     */
    @PutMapping
    @ApiOperation("更新运输分段信息")
    @RequiresPermissions("car:goods:update")
    public ResponseEntity<String> updateGoods(@RequestBody PlatformGoods platformGoods){

        Assert.notNull(platformGoods,"更新运输分段信息 不能为空");
        log.info("更新车辆id为{}的运输分段信息", platformGoods.getGoodsId());

        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        platformGoods.setUpdateId(principal.getUserId());

        boolean updateCar = platformGoodsService.updateById(platformGoods);
        if (updateCar){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("更新失败");
    }

    /**
     * 删除运输分段信息
     * @param platformGoods
     * @return
     */
    @SysLogs("删除运输分段信息")
    @DeleteMapping
    @ApiOperation("删除运输分段信息")
    @RequiresPermissions("car:goods:delete")
    public ResponseEntity<String> deleteByIds(@RequestBody List<PlatformGoods> platformGoods){

        Assert.notNull(platformGoods,"删除的分段信息不能为空");
        log.info("删除分段为{}的分段", platformGoods);

        boolean removeByIds = platformGoodsService.removeByGoods(platformGoods);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("删除失败");
    }

}