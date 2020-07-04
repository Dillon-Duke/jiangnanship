package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.AppOperateMapper;
import com.caidao.pojo.AppOperate;
import com.caidao.service.AppOperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
@Service
public class AppOperateServiceImpl extends ServiceImpl<AppOperateMapper, AppOperate> implements AppOperateService {

    private static final Integer APP_USED_COUNT = 4;

    @Autowired
    private AppOperateMapper appOperateMapper;

    @Override
    public List<AppOperate> getAppOperate(Integer userId) {
        List<AppOperate> appOperates = appOperateMapper.selectList(new LambdaQueryWrapper<AppOperate>()
                .eq(AppOperate::getUserId, userId)
                .orderByDesc(AppOperate::getTimes));
        if (appOperates.size() > APP_USED_COUNT) {
            appOperates = appOperates.subList(0,APP_USED_COUNT);
        }
        return appOperates;
    }
}
