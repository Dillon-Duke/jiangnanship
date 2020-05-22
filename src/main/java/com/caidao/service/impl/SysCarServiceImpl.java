package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.Car;
import com.caidao.mapper.SysCarMapper;
import com.caidao.service.SysCarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@Service
public class SysCarServiceImpl extends ServiceImpl<SysCarMapper, Car> implements SysCarService {

    @Autowired
    private SysCarMapper sysCarMapper;

    /**
     * 获取车辆的当前页，页大小
     * @param page
     * @param car
     * @return
     */
    @Override
    public IPage<Car> findSysCarPage(Page<Car> page, Car car) {
        IPage<Car> carPage = sysCarMapper.selectPage(page, new LambdaQueryWrapper<Car>()
                .eq(StringUtils.hasText(car.getCarName()), Car::getCarName, car.getCarName()));
        return carPage;
    }

    /**
     * 复写新增车辆，增加创建日期 ，状态
     * @param car
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean save(Car car) {
        car.setCreateDate(LocalDateTime.now());
        car.setState(1);
        return super.save(car);
    }
}
