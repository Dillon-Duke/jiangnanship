package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.CarConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author jinpeng
 * @since 2020-03-25
 */
@Repository
public interface CarConfigMapper extends BaseMapper<CarConfig> {

    /**
     * 批量假删除车辆配置
     * @param idList
     * @return
     */
    boolean updateBatchesState(@Param("idList") Collection<? extends Serializable> idList);
}
