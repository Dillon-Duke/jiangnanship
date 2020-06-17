package com.caidao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.PlatformReason;

/**
 * @author Dillon
 * @since 2020-06-13
 */
public interface PlatformReasonService extends IService<PlatformReason> {

    /**
     * 完成审批
     * @param platformReason
     * @return
     */
    void completeApprovalWithOpinion(PlatformReason platformReason);
}
