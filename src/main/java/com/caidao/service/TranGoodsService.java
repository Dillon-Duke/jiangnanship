package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.TranGoods;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-06-01
 */
public interface TranGoodsService extends IService<TranGoods> {

    /**
     * 获取运输分段的当前页，页大小
     * @param page
     * @param tranGoods
     * @return
     */
    IPage<TranGoods> findSysGoodsPage(Page<TranGoods> page, TranGoods tranGoods);

    /**
     * 删除运输分段信息
     * @param tranGoods
     * @return
     */
    boolean removeByGoods(List<TranGoods> tranGoods);
}
