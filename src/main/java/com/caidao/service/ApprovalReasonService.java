package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.ApprovalReason;

/**
 * @author Dillon
 * @since 2020-06-13
 */
public interface ApprovalReasonService extends IService<ApprovalReason> {

    /**
     * 完成审批
     * @param approvalReason
     * @return
     */
    void completeApprovalWithOpinion(ApprovalReason approvalReason);

    /**
     * 计划任务的完成
     * @param approvalReason
     * @return
     */
    void endFlatCarPlanTask(ApprovalReason approvalReason);
}
