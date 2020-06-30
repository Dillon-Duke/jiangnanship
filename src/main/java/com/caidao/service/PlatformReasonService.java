package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.FlatCarCancelParam;
import com.caidao.pojo.PlatformReason;

import java.util.Map;

/**
 * @author Dillon
 * @since 2020-06-13
 */
public interface PlatformReasonService extends IService<PlatformReason> {

    /**
     * 完成带意见的审批
     * @param platformReason
     * @return
     */
    String completeApprovalWithOpinion(PlatformReason platformReason);

    /**
     * 完成取消任务的审批
     * @param param
     * @return
     */
    String completeCancelApplyTask(FlatCarCancelParam param);

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    String completeApprovalWithNoOpinion(String taskId);

    /**
     * 开始一个取消任务申请
     * @param param
     * @return
     */
    Map<String, String> cancelApplyTaskStart(FlatCarCancelParam param);

    /**
     * 取消任务司机接单和执行
     * @param param
     * @return
     */
    void cancelApplyTaskDriverWorking(FlatCarCancelParam param);
}
