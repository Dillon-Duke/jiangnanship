package com.caidao.service;

import com.caidao.pojo.LunchImage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
public interface LunchImageService extends IService<LunchImage> {

    /**
     * 获取手机首页的轮播图片
     * @return
     */
    List<LunchImage> getLunchImages();
}
