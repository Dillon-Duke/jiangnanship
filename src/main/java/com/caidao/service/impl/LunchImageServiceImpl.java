package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.LunchImageMapper;
import com.caidao.pojo.LunchImage;
import com.caidao.service.LunchImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
@Service
public class LunchImageServiceImpl extends ServiceImpl<LunchImageMapper, LunchImage> implements LunchImageService {

    private static final int MAX_IMAGE_COUNT = 3;

    @Autowired
    private LunchImageMapper lunchImageMapper;

    /**
     * 获取手机首页的轮播图片
     * 最多返回3张轮播图片
     * 1  代表轮播图设置为显示状态
     * @return
     */
    @Override
    public List<LunchImage> getLunchImages() {
        List<LunchImage> imageList = lunchImageMapper.selectList(new LambdaQueryWrapper<LunchImage>()
                .eq(LunchImage::getIsUse, 1));
        //将图片按照id进行倒排序
        imageList.sort(Comparator.comparing(LunchImage::getId).reversed());
        //如果设置的图片超过3张，则返回最新设置的三张图片
        if (imageList.size() > MAX_IMAGE_COUNT) {
            imageList = imageList.subList(0,3);
        }
        return imageList;
    }
}
