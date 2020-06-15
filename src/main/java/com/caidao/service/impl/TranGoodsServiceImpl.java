package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.pojo.TranGoods;
import com.caidao.mapper.TranGoodsMapper;
import com.caidao.service.TranGoodsService;
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
public class TranGoodsServiceImpl extends ServiceImpl<TranGoodsMapper, TranGoods> implements TranGoodsService {

    @Autowired
    private TranGoodsMapper tranGoodsMapper;

    @Autowired
    private FastDfsClientUtils fastDfsClientUtils;

    @Value("${fdfs-imgUpload-prifax}")
    private String imgUploadPrifax;

    /**
     * 新增一个分段信息
     * @param tranGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TranGoods tranGoods) {
        tranGoods.setCreateDate(LocalDateTime.now());
        tranGoods.setState(1);
        return super.save(tranGoods);
    }

    /**
     * 获取运输分段的当前页，页大小
     * @param page
     * @param tranGoods
     * @return
     */
    @Override
    public IPage<TranGoods> findSysGoodsPage(Page<TranGoods> page, TranGoods tranGoods) {
        IPage<TranGoods> tranGoodsPage = tranGoodsMapper.selectPage(page, new LambdaQueryWrapper<TranGoods>()
                .like(StringUtils.hasText(tranGoods.getProCode()), TranGoods::getProCode, tranGoods.getProCode()));
        return tranGoodsPage;

    }

    /**
     * 删除对应的分段
     * @param tranGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByGoods(List<TranGoods> tranGoods) {

        //删除图片
        for (TranGoods tranGood : tranGoods) {
            for (String string : tranGood.getGoodsSource().split(";")) {
                if (string.contains(imgUploadPrifax + File.separator + "group")){
                    fastDfsClientUtils.deleteFile(string);
                }
            }
        }

        //删除商品信息
        List<Integer> list = new ArrayList<>(tranGoods.size());
        for (TranGoods tranGood : tranGoods) {
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
     * @param tranGoods
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TranGoods tranGoods) {
        tranGoods.setUpdateDate(LocalDateTime.now());

        //判断给那些新增的没有前缀的条目加上前缀
        String[] strings = tranGoods.getGoodsSource().split(";");
        List<Object> list = new ArrayList<>();
        for (String string : strings) {
            if (!string.contains(imgUploadPrifax + "group")){
                list.add(imgUploadPrifax + string);
            }
        }
        tranGoods.setGoodsSource(list.toString());
        return super.updateById(tranGoods);
    }

}
