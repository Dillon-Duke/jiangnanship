package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.PlatformGoods;
import com.caidao.service.PlatformGoodsService;
import io.swagger.annotations.ApiOperation;
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
    public ResponseEntity<IPage<PlatformGoods>> getGoodsList(Page<PlatformGoods> page , PlatformGoods platformGoods){
        IPage<PlatformGoods> tranGoodsPage = platformGoodsService.findSysGoodsPage(page, platformGoods);
        return ResponseEntity.ok(tranGoodsPage);
    }

    /**
     * 新增一个分段信息
     * @param platformGoods
     * @return
     */
    @ApiOperation("新增一个分段信息")
    @PostMapping()
    public ResponseEntity<String> saveAppTransGoods(@RequestBody PlatformGoods platformGoods){

        boolean save = platformGoodsService.save(platformGoods);
        if (save){
            return ResponseEntity.ok("新增成功");
        }
        return ResponseEntity.ok("新增失败");
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id查询运输分段信息")
    @RequiresPermissions("car:goods:info")
    public ResponseEntity<PlatformGoods> getGoodsInfoById(@PathVariable("id") Integer id){
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

        boolean removeByIds = platformGoodsService.removeByGoods(platformGoods);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("删除失败");
    }


}
