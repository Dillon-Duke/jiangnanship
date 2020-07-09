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
     * 批量新增图片，一次行最多上传4张
     * @param lunchImage
     * @return
     */
    boolean addLunchImage(List<LunchImage> lunchImage);

    /**
     * 修改图片状态为使用或者未使用
     * @param imgId
     * @param isUse
     * @return
     */
    boolean updateImageUseState(Integer imgId, Integer isUse);

    /**
     * 批量删除图片 假删除
     * @param ids
     * @return
     */
    Boolean beachDeleteLunchImage(List<Integer> ids);
}
