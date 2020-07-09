package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.AppTasksMassage;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@Repository
public interface AppTasksMassageMapper extends BaseMapper<AppTasksMassage> {

    /**
     * 批量推送用户消息推送信息
     * @param appTasksMassages
     * @return
     */
    Integer insertBatches(@Param("appTasksMassages") List<AppTasksMassage> appTasksMassages);

    /**
     * 批量更新消息
     * @param list
     * @return
     */
    Boolean updateBatches(@Param("list") List<AppTasksMassage> list);

    /**
     * 批量假删除用户待办事项任务
     * @param idList
     * @return
     */
    Boolean updateBatchesState(@Param("idList") List<Integer> idList);
}
