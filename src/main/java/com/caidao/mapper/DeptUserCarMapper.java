package com.caidao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.pojo.DeptUserCar;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-06
 */
@Repository
public interface DeptUserCarMapper extends BaseMapper<DeptUserCar> {

    /**
     * 批量插入车辆用户绑定信息
     * @param deptUserCars
     * @return
     */
    Boolean insertBatches(List<DeptUserCar> deptUserCars);
}
