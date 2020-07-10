package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.AppCommonMsg;
import com.caidao.pojo.AppUserCommonMsg;
import org.apache.ibatis.annotations.Delete;
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

    /**
     * 批量删除用户表中的消息
     * @param id
     * @return
     */
    @Delete("DELETE FROM app_user_common_msg WHERE comm_id = #{id}")
    Integer deleteBatchCommId(@Param("id") Integer id);
}
