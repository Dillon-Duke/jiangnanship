package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 批量假删除用户信息
     * @param idList
     * @return
     */
    Integer updateBatchesState(@Param("idList") Collection<? extends Serializable> idList);
}
