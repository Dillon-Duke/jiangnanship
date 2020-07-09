package com.caidao.mapper;

import com.caidao.pojo.AppCommonMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-07
 */
@Repository
public interface AppCommonMsgMapper extends BaseMapper<AppCommonMsg> {

    /**
     * 批量删除对应的消息，假删除
     * @param ids
     * @return
     */
    boolean removeBatchIds(@Param("ids") List<Integer> ids);
}
