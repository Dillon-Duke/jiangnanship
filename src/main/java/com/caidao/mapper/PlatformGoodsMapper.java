package com.caidao.mapper;

import com.caidao.pojo.PlatformGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-01
 */
@Repository
public interface PlatformGoodsMapper extends BaseMapper<PlatformGoods> {

    /**
     * //更改分段绑定信息状态
     * @param objectId
     * @return
     */
    @Update("UPDATE platform_goods SET pro_code = 0 WHERE goods_id = #{objectId}")
    int updateGoodsBindStateWithGoodsId(@Param("objectId") Integer objectId);

    /**
     * 批量更新物品状态为删除
     * @param list
     * @return
     */
    Integer updateBatchesState(@Param("idList") List<Integer> list);
}
