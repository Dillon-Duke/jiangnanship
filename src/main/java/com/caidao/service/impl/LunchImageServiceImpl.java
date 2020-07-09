package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.LunchImageMapper;
import com.caidao.pojo.LunchImage;
import com.caidao.pojo.SysUser;
import com.caidao.service.LunchImageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
@Service
@Slf4j
public class LunchImageServiceImpl extends ServiceImpl<LunchImageMapper, LunchImage> implements LunchImageService {

    @Autowired
    private LunchImageMapper lunchImageMapper;

    /**
     * 手机端首页图片的列表
     * @param page
     * @param lunchImage
     * @return
     */
    @Override
    public IPage<LunchImage> getLunchImagePage(Page<LunchImage> page, LunchImage lunchImage) {
        Assert.notNull(page,"分页数据不能为空");
        log.info("查询首页图片的当前页{}，页大小{}", page.getCurrent(),page.getSize());
        IPage<LunchImage> imageIPage = lunchImageMapper.selectPage(page, new LambdaQueryWrapper<LunchImage>()
                .like(LunchImage::getFileImage, lunchImage.getFileImage())
                .eq(LunchImage::getState, 1));
        return imageIPage;
    }

    /**
     * 批量新增图片，一次行最多上传4张
     * @param lunchImage
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean addLunchImage(List<LunchImage> lunchImage) {
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(sysUser,"用户登录超时，请重新登录");
        log.info("用户{}新增图片",sysUser.getUsername());
        List<LunchImage> lunchImages = new ArrayList<>(lunchImage.size());
        for (LunchImage image : lunchImage) {
            image.setCreateDate(LocalDateTime.now());
            image.setCreateId(sysUser.getCreateId());
            image.setState(1);
            lunchImages.add(image);
        }
        return lunchImageMapper.insertBatches(lunchImages);
    }

    /**
     * 修改图片状态为使用或者未使用
     * @param imgId
     * @param isUse
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateImageUseState(Integer imgId, Integer isUse) {
        Assert.notNull(imgId,"id不能为空");
        Assert.notNull(isUse,"图片状态不能为空");
        Integer num = lunchImageMapper.updateImageUseState(imgId,isUse);
        if (num == 0) {
            return false;
        }
        return true;
    }

    /**
     * 批量删除图片 假删除
     * @param ids
     * @return
     */
    @Override
    public Boolean beachDeleteLunchImage(List<Integer> ids) {
        Assert.notNull(ids,"ids不能为空");
        Integer nums = lunchImageMapper.beachUpdateLunchImageState(ids);
        if (nums == 0) {
            return false;
        }
        return true;
    }
}
