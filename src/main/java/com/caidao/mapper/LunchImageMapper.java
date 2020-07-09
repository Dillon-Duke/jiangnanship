package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.LunchImage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-04
 */
@Repository
public interface LunchImageMapper extends BaseMapper<LunchImage> {

    /**
     * 批量添加图片数据
     * @param lunchImages
     * @return
     */
    boolean insertBatches(@Param("lunchImages") List<LunchImage> lunchImages);

    /**
     * 更新图片的使用状态
     * @param imgId
     * @param isUse
     * @return
     */
    @Update("UPDATE lunch_image SET is_use = #{isUse} WHERE id = #{imgId}")
    Integer updateImageUseState(@Param("imgId") Integer imgId, @Param("isUse") Integer isUse);

    /**
     * 用户批量删除图片
     * @param ids
     * @return
     */
    Integer beachUpdateLunchImageState(@Param("ids") List<Integer> ids);
}
