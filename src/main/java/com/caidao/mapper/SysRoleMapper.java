package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.SysRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 批量假删除系统角色表
     * @param idList
     * @return
     */
    Integer updateBatchesState(@Param("idList") Collection<? extends Serializable> idList);
}
