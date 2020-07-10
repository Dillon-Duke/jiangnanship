package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.LunchImage;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
public interface LunchImageService extends IService<LunchImage> {

    /**
     * 手机端首页图片的列表
     * @param page
     * @param lunchImage
     * @return
     */
    IPage<LunchImage> getLunchImagePage(Page<LunchImage> page, LunchImage lunchImage);

    /**
     * 新增图片
     * @param lunchImage
     * @return
     */
    boolean addLunchImage(LunchImage lunchImage);

    /**
     * 修改图片
     * @param lunchImage
     * @return
     */
    boolean updateImageUseState(LunchImage lunchImage);

    /**
     * 批量删除图片 假删除
     * @param ids
     * @return
     */
    Boolean beachDeleteLunchImage(List<Integer> ids);

    /**
     * 发布或者取消发布app首页轮询图
     * @param lunchImage
     */
    void useOrNotLunchImage(LunchImage lunchImage);

    /**
     * 通过Id获取图片信息
     * @param id
     * @return
     */
    LunchImage getLunchImageById(Integer id);
}
