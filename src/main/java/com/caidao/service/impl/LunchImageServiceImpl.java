package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
                .like(StringUtils.hasText(lunchImage.getFileImage()),LunchImage::getFileImage, lunchImage.getFileImage())
                .eq(LunchImage::getState, 1)
                .orderByDesc(LunchImage::getCreateDate));
        return imageIPage;
    }

    /**
     * 新增图片
     * @param lunchImage
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean addLunchImage(LunchImage lunchImage) {
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(sysUser,"用户登录超时，请重新登录");
        log.info("用户{}新增图片",sysUser.getUsername());
        lunchImage.setCreateDate(LocalDateTime.now());
        lunchImage.setCreateId(sysUser.getUserId());
        lunchImage.setIsUse(0);
        lunchImage.setState(1);
        //判断是否输入了自定义名称，如果没有，则设置与文件名称一致
        lunchImage.setCustomName("".equals(lunchImage.getCustomName()) ? lunchImage.getFileImage() : lunchImage.getCustomName());
        int insert = lunchImageMapper.insert(lunchImage);
        if (insert == 0) {
            return false;
        }
        return true;
    }

    /**
     * 修改图片
     * @param lunchImage
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateImageUseState(LunchImage lunchImage) {
        Assert.notNull(lunchImage,"图片状态不能为空");
        Integer num = lunchImageMapper.updateById(lunchImage);
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
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean beachDeleteLunchImage(List<Integer> ids) {
        Assert.notNull(ids,"ids不能为空");
        //获取需要删除的字段，判断是否有已发布的
        List<LunchImage> lunchImages = lunchImageMapper.selectBatchIds(ids);
        List<LunchImage> list = lunchImages.stream().filter((x) -> x.getIsUse() == 1).collect(Collectors.toList());
        if (list.size() != 0) {
            throw new MyException("删除的图片中包含了已发布的图片，无法删除");
        }
        Integer nums = lunchImageMapper.beachUpdateLunchImageState(ids);
        if (nums == 0) {
            return false;
        }
        return true;
    }

    /**
     * 发布或者取消发布app首页轮询图
     * @param lunchImage
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void useOrNotLunchImage(LunchImage lunchImage) {
        Assert.notNull(lunchImage,"修改消息不能为空");
        log.info("发布Id为{}的图片",lunchImage.getId());
        //获取取消或者是发布的状态
        lunchImageMapper.updateById(lunchImage);
    }

    /**
     * 通过Id获取图片信息
     * @param id
     * @return
     */
    @Override
    public LunchImage getLunchImageById(Integer id) {
        Assert.notNull(id,"不能为空");
        log.info("查询Id为{}的图片",id);
        LunchImage lunchImage = lunchImageMapper.selectById(id);
        return lunchImage;
    }
}
