package com.caidao.controller.back.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.LunchImage;
import com.caidao.service.LunchImageService;
import io.swagger.annotations.ApiOperation;
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
    public ResponseEntity<IPage<LunchImage>> getLunchImageList(Page<LunchImage> page, LunchImage lunchImage){
        IPage<LunchImage> imagePage = lunchImageService.getLunchImagePage(page, lunchImage);
        return ResponseEntity.ok(imagePage);
    }

    /**
     * 批量新增图片，一次行最多上传4张
     * @param lunchImage
     * @return
     */
    @PostMapping
    @ApiOperation("新增一张轮询图")
    public ResponseEntity<Void> addLunchImage(@RequestBody List<LunchImage> lunchImage) {
        boolean result = lunchImageService.addLunchImage(lunchImage);
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * 修改图片状态为使用或者未使用
     * @param imgId
     * @param isUse
     * @return
     */
    @GetMapping("update/{imgId}/{isUse}")
    @ApiOperation("修改图片状态为使用或者未使用")
    public ResponseEntity<Void> updateImageUseState(@PathVariable("imgId") Integer imgId, @PathVariable("isUse") Integer isUse) {
        boolean result = lunchImageService.updateImageUseState(imgId,isUse);
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
    public ResponseEntity<Boolean> beachDeleteLunchImage(@RequestBody List<Integer> ids){
        Boolean removeByIds = lunchImageService.beachDeleteLunchImage(ids);
        if (removeByIds) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

}
