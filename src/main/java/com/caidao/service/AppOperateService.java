package com.caidao.service;

import com.caidao.pojo.AppOperate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Dillon
 * @since 2020-07-04
 */
public interface AppOperateService extends IService<AppOperate> {

    /**
     * 获取手机的常用操作
     * @param userId
     * @return
     */
    List<AppOperate> getAppOperate(Integer userId);
}
