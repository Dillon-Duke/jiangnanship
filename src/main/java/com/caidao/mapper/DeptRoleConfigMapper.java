package com.caidao.mapper;

import com.caidao.pojo.DeptRoleAuthorition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Repository
public interface DeptRoleConfigMapper extends BaseMapper<DeptRoleAuthorition> {

    /**
     * 批量插入部门用户权限中间表
     * @param authorisations
     * @return
     */
    Boolean insertBatches(List<DeptRoleAuthorition> authorisations);
}
