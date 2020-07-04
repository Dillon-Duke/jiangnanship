package com.caidao.mapper;

import com.caidao.pojo.PlatformGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

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
    @Update("UPDATE tran_goods SET pro_code = 0 WHERE goods_id = #{objectId}")
    int updateGoodsBindStateWithGoodsId(Integer objectId);
}
