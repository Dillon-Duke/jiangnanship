package com.caidao.mapper;

import com.caidao.pojo.Dept;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * @author Dillon
 * @since 2020-05-21
 */
@Repository
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 假删除部门信息
     * @param id
     * @return
     */
    @Update("UPDATE dept SET state = 0 WHERE dept_id = #{id}")
    Integer updateState(@Param("id") Serializable id);
}
