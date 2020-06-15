package com.caidao.controller.front.goods;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.common.ResponseEntity;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.SysUser;
import com.caidao.pojo.TranGoods;
import com.caidao.service.TranGoodsService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tom
 * @since 2020-06-11
 */
@RestController
@RequestMapping("/appTrans/goods")
@Slf4j
public class AppTransGoodsController {

    @Autowired
    private TranGoodsService tranGoodsService;

    /**
     * 获取分页运输分段信息
     * @param page
     * @param tranGoods
     * @return
     */
    @GetMapping("/Page")
    @ApiOperation("获取分页运输分段信息")
    @RequiresPermissions("goodsRecord:goods:page")
    public ResponseEntity<IPage<TranGoods>> getAppDoodsList(Page<TranGoods> page , TranGoods tranGoods){
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<TranGoods> tranGoodsPage = tranGoodsService.findSysGoodsPage(page, tranGoods);
        return ResponseEntity.ok(tranGoodsPage);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("/Info/{id}")
    @ApiOperation("通过id查询运输分段信息")
    @RequiresPermissions("goodsRecord:goods:info")
    public ResponseEntity<TranGoods> getAppGoodsInfoById(@PathVariable("id") Integer id){
        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);
        TranGoods tranGoods = tranGoodsService.getById(id);
        return ResponseEntity.ok(tranGoods);
    }

    /**
     * 新增一个分段信息
     * @param tranGoods
     * @return
     */
    @ApiOperation("新增一个分段信息")
    @PostMapping("/save")
    @RequiresPermissions("goodsRecord:goods:save")
    public ResponseEntity<String> saveAppTransGoods(@RequestBody TranGoods tranGoods){

        Assert.notNull(tranGoods,"新增分段信息不能为空");
        log.info("新增分段号为{}的分段",tranGoods.getGoodsCode());

        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        tranGoods.setCreateId(deptUser.getUserId());
        boolean save = tranGoodsService.save(tranGoods);

        if (save){
            return ResponseEntity.ok("新增成功");
        }
        return ResponseEntity.error("新增失败");
    }

    /**
     * 更新车辆信息
     * @param tranGoods
     * @return
     */
    @PutMapping
    @ApiOperation("更新运输分段信息")
    @RequiresPermissions("car:goods:update")
    public ResponseEntity updateGoods(@RequestBody TranGoods tranGoods){

        com.baomidou.mybatisplus.core.toolkit.Assert.notNull(tranGoods,"更新运输分段信息 不能为空");
        log.info("更新车辆id为{}的运输分段信息",tranGoods.getGoodsId());

        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        tranGoods.setUpdateId(principal.getUserId());

        boolean updateCar = tranGoodsService.updateById(tranGoods);
        if (updateCar){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.error("更新失败");
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

        com.baomidou.mybatisplus.core.toolkit.Assert.notNull(tranGoods,"删除的分段信息不能为空");
        log.info("删除分段为{}的分段",tranGoods);

        boolean removeByIds = tranGoodsService.removeByGoods(tranGoods);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.error("删除失败");
    }


}
