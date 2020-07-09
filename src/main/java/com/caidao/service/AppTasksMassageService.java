package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.AppTasksMassage;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
public interface AppTasksMassageService extends IService<AppTasksMassage> {

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    List<AppTasksMassage> getUserNotReadMassage(String username);

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    List<AppTasksMassage> getUserReadMassage(String username);

    /**
     * 批量删除用户的待办任务
     * @param idList
     * @return
     */
    boolean deleteTaskMassages(List<Integer> idList);

    /**
     * 通过Id获取详细的个人审批信息
     * @param approvalId
     * @return
     */
    AppTasksMassage getUserApprovalDetailInfoByApprovalId(Integer approvalId);
}
