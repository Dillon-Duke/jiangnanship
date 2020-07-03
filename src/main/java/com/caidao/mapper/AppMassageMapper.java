package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.AppMassage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@Repository
public interface AppMassageMapper extends BaseMapper<AppMassage> {

    /**
     * 更新消息状态为已拾取
     * @param taskId
     * @return
     */
    @Update("UPDATE app_massage SET is_read = -1 WHERE task_id = #{taskId}")
    Integer updateReanState(@Param("taskId") String taskId);

    /**
     * 用户归还任务
     * @param taskId
     * @return
     */
    @Update("UPDATE app_massage SET is_read = 1 WHERE task_id = #{taskId}")
    Integer backTasks(@Param("taskId") String taskId);

    /**
     * 批量推送用户消息推送信息
     * @param appMassages
     * @return
     */
    Boolean insertBatches(List<AppMassage> appMassages);

    /**
     * 批量更新消息
     * @param list
     * @return
     */
    Boolean updateBatches(List<AppMassage> list);
}
