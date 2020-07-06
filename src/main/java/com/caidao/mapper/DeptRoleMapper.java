package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptRoleMapper extends BaseMapper<DeptRole> {

    /**
     * 批量假删除用户角色
     * @param idList
     * @return
     */
    boolean updateBatchesState(@Param("idList") Collection<? extends Serializable> idList);
}
