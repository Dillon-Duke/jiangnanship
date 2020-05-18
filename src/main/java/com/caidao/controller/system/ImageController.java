package com.caidao.controller.system;

import com.caidao.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * 单个图片文件上传到数据库
     * @return
     */

    @PostMapping("/addImage")
    public ResponseEntity<String> fileToDB(File file){

        log.info("新增文件名为{}的图片",file.getName());
        boolean saveImage = imageService.saveImage(file);
        if (saveImage){
            return ResponseEntity.ok("新增成功");
        }
        return ResponseEntity.ok("新增失败");
    }

    /**
     * 从数据库中获取文件
     * @param filePath
     */
    public static void getFile(String filePath){

    }

}
