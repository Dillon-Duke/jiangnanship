package com.caidao.service;

import com.caidao.entity.Image;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.File;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
public interface ImageService extends IService<Image> {

    boolean saveImage(File file);
}
