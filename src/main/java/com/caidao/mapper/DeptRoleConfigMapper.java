package com.caidao.mapper;

import com.caidao.pojo.DeptRoleAuthorisation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptRoleConfigMapper extends BaseMapper<DeptRoleAuthorisation> {

    /**
     * 批量插入部门用户权限中间表
     * @param authorisations
     * @return
     */
    Boolean insertBatches(List<DeptRoleAuthorisation> authorisations);

    /**
     * 批量删除角色配置中间表
     * @param idList
     * @return
     */
    boolean deleteBatchRoleIds(@Param("idList") Collection<? extends Serializable> idList);
}
