package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 假删除
     * @param id
     * @return
     */
    @Update("UPDATE sys_menu SET state = 0 WHERE menu_id = #{id}")
    Integer updateState(@Param("id") Serializable id);
}
