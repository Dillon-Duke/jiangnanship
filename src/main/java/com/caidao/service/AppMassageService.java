package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.AppMassage;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
public interface AppMassageService extends IService<AppMassage> {

    /**
     * 获得用户app首页信息
     * @param username
     * @return
     */
    Integer getAppMassageCount(String username);

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    List<AppMassage> getUserNotReadMassage(String username);

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    List<AppMassage> getUserReadMassage(String username);
}
