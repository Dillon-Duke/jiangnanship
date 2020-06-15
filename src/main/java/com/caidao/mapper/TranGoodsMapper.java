package com.caidao.mapper;

import com.caidao.pojo.TranGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Dillon
 * @since 2020-06-01
 */
public interface TranGoodsMapper extends BaseMapper<TranGoods> {

    /**
     * //更改分段绑定信息状态
     * @param objectId
     * @return
     */
    @Update("UPDATE tran_goods SET pro_code = 0 WHERE goods_id = #{objectId}")
    int updateGoodsBindState(Integer objectId);
}
