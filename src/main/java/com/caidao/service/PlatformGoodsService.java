package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.PlatformGoods;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-06-01
 */
public interface PlatformGoodsService extends IService<PlatformGoods> {

    /**
     * 获取运输分段的当前页，页大小
     * @param page
     * @param platformGoods
     * @return
     */
    IPage<PlatformGoods> findSysGoodsPage(Page<PlatformGoods> page, PlatformGoods platformGoods);

    /**
     * 删除运输分段信息
     * @param platformGoods
     * @return
     */
    boolean removeByGoods(List<PlatformGoods> platformGoods);
}
