package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.PlatformGoodsMapper;
import com.caidao.pojo.PlatformGoods;
import com.caidao.service.PlatformGoodsService;
import com.caidao.util.FastDfsClientUtils;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-01
 */
@Service
public class PlatformGoodsServiceImpl extends ServiceImpl<PlatformGoodsMapper, PlatformGoods> implements PlatformGoodsService {

    @Autowired
    private PlatformGoodsMapper platformGoodsMapper;

    @Autowired
    private FastDfsClientUtils fastDfsClientUtils;

    @Value("${fdfs-imgUpload-prifax}")
    private String imgUploadPrifax;

    /**
     * 新增一个分段信息
     * @param platformGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(PlatformGoods platformGoods) {
        platformGoods.setCreateDate(LocalDateTime.now());
        platformGoods.setState(1);
        return super.save(platformGoods);
    }

    /**
     * 获取运输分段的当前页，页大小
     * @param page
     * @param platformGoods
     * @return
     */
    @Override
    public IPage<PlatformGoods> findSysGoodsPage(Page<PlatformGoods> page, PlatformGoods platformGoods) {
        IPage<PlatformGoods> platformGoodsPage = platformGoodsMapper.selectPage(page, new LambdaQueryWrapper<PlatformGoods>()
                .like(StringUtils.hasText(platformGoods.getProCode()), PlatformGoods::getProCode, platformGoods.getProCode()));
        return platformGoodsPage;

    }

    /**
     * 删除对应的分段
     * @param platformGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByGoods(List<PlatformGoods> platformGoods) {

        //删除图片
        for (PlatformGoods tranGood : platformGoods) {
            for (String string : tranGood.getGoodsSource().split(";")) {
                if (string.contains(imgUploadPrifax + File.separator + "group")){
                    fastDfsClientUtils.deleteFile(string);
                }
            }
        }

        //删除商品信息
        List<Integer> list = new ArrayList<>(platformGoods.size());
        for (PlatformGoods tranGood : platformGoods) {
            list.add(tranGood.getGoodsId());
        }
        boolean remove = this.removeByIds(list);
        if (remove){
            return true;
        }
        return false;
    }

    /**
     * 复写编辑运输分段，增加更新日期 ，状态
     * @param platformGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(PlatformGoods platformGoods) {
        platformGoods.setUpdateDate(LocalDateTime.now());

        //判断给那些新增的没有前缀的条目加上前缀
        String[] strings = platformGoods.getGoodsSource().split(";");
        List<Object> list = new ArrayList<>();
        for (String string : strings) {
            if (!string.contains(imgUploadPrifax + "group")){
                list.add(imgUploadPrifax + string);
            }
        }
        platformGoods.setGoodsSource(list.toString());
        return super.updateById(platformGoods);
    }

}
