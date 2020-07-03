package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.PlatformGoodsMapper;
import com.caidao.pojo.PlatformGoods;
import com.caidao.pojo.SysUser;
import com.caidao.service.PlatformGoodsService;
import com.caidao.util.FastDfsClientUtils;
import com.caidao.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-01
 */
@Service
@Slf4j
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
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean save(PlatformGoods platformGoods) {

        Assert.notNull(platformGoods,"新增分段信息不能为空");
        log.info("新增分段号为{}的分段", platformGoods.getGoodsCode());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        platformGoods.setCreateId(sysUser.getUserId());
        if (sysUser == null ) {
            throw new MyException("用户登录超时，请重新登录");
        }
        platformGoods.setCreateId(sysUser.getUserId());
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
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
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

        Assert.notNull(platformGoods,"删除的分段信息不能为空");
        log.info("删除分段为{}的分段", platformGoods);
        //删除图片
        for (PlatformGoods tranGood : platformGoods) {
            for (String string : tranGood.getSourceImage().split(PropertyUtils.STRING_SPILT_WITH_SEMICOLON)) {
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

        Assert.notNull(platformGoods,"更新运输分段信息 不能为空");
        log.info("更新车辆id为{}的运输分段信息", platformGoods.getGoodsId());
        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        platformGoods.setUpdateId(principal.getUserId());
        platformGoods.setUpdateDate(LocalDateTime.now());

        //判断给那些新增的没有前缀的条目加上前缀
        String[] strings = platformGoods.getSourceImage().split(";");
        List<Object> list = new ArrayList<>();
        for (String string : strings) {
            if (!string.contains(imgUploadPrifax + "group")){
                list.add(imgUploadPrifax + string);
            }
        }
        platformGoods.setSourceImage(list.toString());
        return super.updateById(platformGoods);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @Override
    public PlatformGoods getById(Serializable id) {
        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);
        return super.getById(id);
    }
}
