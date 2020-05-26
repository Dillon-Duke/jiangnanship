package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.Car;
import com.caidao.mapper.SysCarMapper;
import com.caidao.service.SysCarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

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

    @Value("${fdfs-imgUpload-prifax}")
    private String imgUploadPrifax;

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
        car.setSourcePhoto(imgUploadPrifax + car.getSourcePhoto());
        return super.save(car);
    }

    /**
     * 复写编辑车辆，增加更新日期 ，状态
     * @param car
     * @return
     */
    @Override
    public boolean updateById(Car car) {
        car.setUpdateDate(LocalDateTime.now());
        String sourcePhoto = car.getSourcePhoto();
        boolean contains = sourcePhoto.contains(imgUploadPrifax);
        if (!contains){
            car.setSourcePhoto(imgUploadPrifax + car.getSourcePhoto());
        }
        return super.updateById(car);
    }

    /**
     * 复写车辆信息删除，删除车辆图片
     * @param idList
     * @return
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        //TODO 想办法前端值（sourceurl） 直接删除，不需要再调用数据库
        return super.removeByIds(idList);
    }
}
