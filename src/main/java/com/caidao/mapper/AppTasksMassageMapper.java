package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.AppTasksMassage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@Repository
public interface AppTasksMassageMapper extends BaseMapper<AppTasksMassage> {

    /**
     * 更新消息状态为已读取
     * @param taskId
     * @return
     */
    @Update("UPDATE app_massage SET is_read = -1 WHERE task_id = #{taskId}")
    Integer updateReadStateToReadied(@Param("taskId") String taskId);

    /**
     * 用户归还任务 更新信息阅读状态为未阅读
     * @param taskId
     * @return
     */
    @Update("UPDATE app_massage SET is_read = 1 WHERE task_id = #{taskId}")
    Integer backTasksWithReadStateToUnRead(@Param("taskId") String taskId);

    /**
     * 批量推送用户消息推送信息
     * @param appTasksMassages
     * @return
     */
    Boolean insertBatches(List<AppTasksMassage> appTasksMassages);

    /**
     * 批量更新消息
     * @param list
     * @return
     */
    Boolean updateBatches(List<AppTasksMassage> list);
}
