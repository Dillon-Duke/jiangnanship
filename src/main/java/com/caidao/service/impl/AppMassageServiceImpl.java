package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.AppMassageMapper;
import com.caidao.pojo.AppMassage;
import com.caidao.service.AppMassageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@Service
@Slf4j
public class AppMassageServiceImpl extends ServiceImpl<AppMassageMapper, AppMassage> implements AppMassageService {

   @Autowired
   private AppMassageMapper appMassageMapper;

    /**
     * 获得用户app首页信息
     * @param username
     * @return
     */
    @Override
    public Integer getAppMassageCount(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息数量",username);
        Integer count = appMassageMapper.selectCount(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getDeptUsername, username)
                .eq(AppMassage::getIsRead, 1));
        return count;
    }

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    @Override
    public List<AppMassage> getUserNotReadMassage(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息列表",username);
        List<AppMassage> massages = appMassageMapper.selectList(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getDeptUsername, username)
                .eq(AppMassage::getIsRead, 1)
                .orderByDesc(AppMassage::getCreateTime));
        return massages;
    }

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    @Override
    public List<AppMassage> getUserReadMassage(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息列表",username);
        List<AppMassage> massages = appMassageMapper.selectList(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getDeptUsername, username)
                .eq(AppMassage::getIsRead, 0)
                .orderByDesc(AppMassage::getCreateTime));
        return massages;
    }
}
