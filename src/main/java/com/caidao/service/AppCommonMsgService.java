package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.AppCommonMsg;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-07
 */
public interface AppCommonMsgService extends IService<AppCommonMsg> {

    /**
     * 获取分页的用户数据
     * @param page
     * @param appCommonMsg
     * @return
     */
    IPage<AppCommonMsg> getAppCommonPage(Page<AppCommonMsg> page, AppCommonMsg appCommonMsg);

    /**
     * 新增通用消息
     * @param appCommonMsg
     */
    void saveAppCommonMassage(AppCommonMsg appCommonMsg);

    /**
     * 通过id获取消息数据
     * @param id
     * @return
     */
    AppCommonMsg getAppCommonMassageById(Integer id);

    /**
     * 批量删除消息 假删除
     * @param ids
     * @return
     */
    void beachDeleteAppCommonMassage(List<Integer> ids);

    /**
     * 修改消息
     * @param appCommonMsg
     * @return
     */
    Boolean updateAppCommonMassage(AppCommonMsg appCommonMsg);

    /**
     * 发布消息
     * @param appCommonMsg
     * @return
     */
    void publishAppCommonMassage(AppCommonMsg appCommonMsg);
}
