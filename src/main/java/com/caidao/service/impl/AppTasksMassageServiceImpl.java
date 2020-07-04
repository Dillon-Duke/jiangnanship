package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.AppTasksMassageMapper;
import com.caidao.pojo.AppTasksMassage;
import com.caidao.service.AppTasksMassageService;
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
public class AppTasksMassageServiceImpl extends ServiceImpl<AppTasksMassageMapper, AppTasksMassage> implements AppTasksMassageService {

   @Autowired
   private AppTasksMassageMapper appTasksMassageMapper;

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    @Override
    public List<AppTasksMassage> getUserNotReadMassage(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息列表",username);
        List<AppTasksMassage> massages = appTasksMassageMapper.selectList(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getDeptUsername, username)
                .eq(AppTasksMassage::getIsRead, 1)
                .orderByDesc(AppTasksMassage::getCreateTime));
        return massages;
    }

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    @Override
    public List<AppTasksMassage> getUserReadMassage(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息列表",username);
        List<AppTasksMassage> massages = appTasksMassageMapper.selectList(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getDeptUsername, username)
                .eq(AppTasksMassage::getIsRead, 0)
                .orderByDesc(AppTasksMassage::getCreateTime));
        return massages;
    }
}
