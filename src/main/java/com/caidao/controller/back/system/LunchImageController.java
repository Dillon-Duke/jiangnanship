package com.caidao.controller.back.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.LunchImage;
import com.caidao.service.LunchImageService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
@RestController
@RequestMapping("/lunch/image")
public class LunchImageController {

    @Autowired
    private LunchImageService lunchImageService;

    /**
     * 手机端首页图片的列表
     * @param page
     * @param lunchImage
     * @return
     */
    @GetMapping("page")
    @ApiOperation("手机端首页图片的列表")
    @RequiresPermissions("sys:appimage:page")
    public ResponseEntity<IPage<LunchImage>> getLunchImageList(Page<LunchImage> page, LunchImage lunchImage){
        IPage<LunchImage> imagePage = lunchImageService.getLunchImagePage(page, lunchImage);
        return ResponseEntity.ok(imagePage);
    }

    /**
     * 新增图片
     * @param lunchImage
     * @return
     */
    @PostMapping
    @ApiOperation("新增一张轮询图")
    @RequiresPermissions("sys:appimage:save")
    public ResponseEntity<Void> addLunchImage(@RequestBody LunchImage lunchImage) {
        boolean result = lunchImageService.addLunchImage(lunchImage);
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 通过Id获取图片信息
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    @ApiOperation("新增一张轮询图")
    @RequiresPermissions("sys:appimage:info")
    public ResponseEntity<LunchImage> getLunchImageById(@PathVariable("id") Integer id) {
        LunchImage image = lunchImageService.getLunchImageById(id);
        return ResponseEntity.ok(image);

    }

    /**
     * 修改图片
     * @param lunchImage
     * @return
     */
    @PutMapping
    @ApiOperation("修改图片")
    @RequiresPermissions("sys:appimage:update")
    public ResponseEntity<Void> updateImageUseState(@RequestBody LunchImage lunchImage) {
        boolean result = lunchImageService.updateImageUseState(lunchImage);
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 批量删除图片 假删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除用户")
    @RequiresPermissions("sys:appimage:delete")
    public ResponseEntity<Boolean> beachDeleteLunchImage(@RequestBody List<Integer> ids){
        Boolean removeByIds = lunchImageService.beachDeleteLunchImage(ids);
        if (removeByIds) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 是否发布lunch图
     * @param lunchImage
     * @return
     */
    @PutMapping("useOrNot")
    @ApiOperation("发布或者取消发布信息")
    @RequiresPermissions({"sys:appimage:use","sys:appimage:unuse"})
    public ResponseEntity<Void> useOrNotLunchImage(@RequestBody LunchImage lunchImage) {
        lunchImageService.useOrNotLunchImage(lunchImage);
        return ResponseEntity.ok().build();
    }

}
