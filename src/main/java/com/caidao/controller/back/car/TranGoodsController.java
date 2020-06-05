package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.entity.SysUser;
import com.caidao.entity.TranGoods;
import com.caidao.service.TranGoodsService;
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
public class TranGoodsController {

    public static final Logger logger = LoggerFactory.getLogger(TranGoodsController.class);

    @Autowired
    private TranGoodsService tranGoodsService;


    /**
     * 获取分页运输分段信息
     * @param page
     * @param tranGoods
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取分页运输分段信息")
    @RequiresPermissions("car:goods:page")
    public ResponseEntity<IPage<TranGoods>> getDoodsList(Page<TranGoods> page , TranGoods tranGoods){
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<TranGoods> tranGoodsPage = tranGoodsService.findSysGoodsPage(page, tranGoods);
        return ResponseEntity.ok(tranGoodsPage);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id查询运输分段信息")
    @RequiresPermissions("car:goods:info")
    public ResponseEntity<TranGoods> getGoodsInfoById(@PathVariable("id") Integer id){
        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);
        TranGoods tranGoods = tranGoodsService.getById(id);
        return ResponseEntity.ok(tranGoods);
    }

    /**
     * 更新车辆信息
     * @param tranGoods
     * @return
     */
    @PutMapping
    @ApiOperation("更新运输分段信息")
    @RequiresPermissions("car:goods:update")
    public ResponseEntity<String> updateGoods(@RequestBody TranGoods tranGoods){

        Assert.notNull(tranGoods,"更新运输分段信息 不能为空");
        log.info("更新车辆id为{}的运输分段信息",tranGoods.getGoodsId());

        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        tranGoods.setUpdateId(principal.getUserId());

        boolean updateCar = tranGoodsService.updateById(tranGoods);
        if (updateCar){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("更新失败");
    }

    /**
     * 删除运输分段信息
     * @param tranGoods
     * @return
     */
    @SysLogs("删除运输分段信息")
    @DeleteMapping
    @ApiOperation("删除运输分段信息")
    @RequiresPermissions("car:goods:delete")
    public ResponseEntity<String> deleteByIds(@RequestBody List<TranGoods> tranGoods){

        Assert.notNull(tranGoods,"删除的分段信息不能为空");
        log.info("删除分段为{}的分段",tranGoods);

        boolean removeByIds = tranGoodsService.removeByGoods(tranGoods);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("删除失败");
    }

}
