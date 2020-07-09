package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.PlatformAdjustmentParam;
import com.caidao.param.PlatformCancelParam;
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
    boolean completeApprovalWithOpinion(PlatformReason platformReason);

    /**
     * 完成取消任务的审批
     * @param param
     * @return
     */
    boolean completeCancelApplyTask(PlatformCancelParam param);

    /**
     * 完成不带意见的审批
     * @param taskId
     * @return
     */
    boolean completeApprovalWithNoOpinion(String taskId);

    /**
     * 开始一个取消任务申请
     * @param param
     * @return
     */
    Map<String, String> cancelApplyTaskStart(PlatformCancelParam param);

    /**
     * 取消任务司机接单和执行
     * @param param
     * @return
     */
    void cancelApplyTaskDriverWorking(PlatformCancelParam param);

    /**
     * 未开始执行的平板车任务调整
     * @param param
     * @return
     */
    Boolean flatcarAdjustmentWithNoStart(PlatformAdjustmentParam param);

    /**
     * 已开始执行的平板车任务调整
     * @param param
     * @return
     */
    Boolean flatcarAdjustmentWithStart(PlatformAdjustmentParam param);

    /**
     * 司机开始执行任务
     * @param taskId
     * @return
     */
    boolean driverStartTask(String taskId);
}
