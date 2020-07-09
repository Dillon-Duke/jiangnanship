package com.caidao.mapper;

import com.caidao.pojo.AppCommonMsg;
import com.caidao.pojo.AppUserCommonMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-09
 */
@Repository
public interface AppUserCommonMsgMapper extends BaseMapper<AppUserCommonMsg> {

    /**
     * 批量新增用户的消息
     * @param list
     * @return
     */
    boolean insertBatches(@Param("list") List<AppUserCommonMsg> list);

    /**
     * 通过公告消息ID更新已发布的消息
     * @param msg
     * @return
     */
    int updateByCommonId(@Param("msg") AppCommonMsg msg);

    /**
     * 批量删除用户表中的消息
     * @param ids
     * @return
     */
    boolean deleteBatchCommIds(@Param("ids")List<Integer> ids);
}
