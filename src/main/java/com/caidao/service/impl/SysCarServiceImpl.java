package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.Car;
import com.caidao.mapper.SysCarMapper;
import com.caidao.service.SysCarService;
import com.caidao.util.FastDfsClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private FastDfsClientUtils fastDfsClientUtils;

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

        //处理展示第一张图片
        List<Car> cars = carPage.getRecords();
        List<Car> arrayList = new ArrayList<>(cars.size());
        for (Car car1 : cars) {
            String[] strings = car1.getSourcePhoto().split(";");
            car1.setSourcePhoto(strings[0]);
            arrayList.add(car1);
        }
        carPage.setRecords(arrayList);
        return carPage;
    }

    /**
     * 删除信息 真删除
     * @param cars
     * @return
     */
    @Override
    public boolean batchRemoveByIds(List<Car> cars) {

        //删除图片
        for (Car car : cars) {
            for (String string : car.getSourcePhoto().split(";")) {
                if (string.contains(imgUploadPrifax + File.separator + "group")){
                    fastDfsClientUtils.deleteFile(string);
                }
            }
        }

        //便利变量，将id放到数组中，批量删除
        List<Integer> list = new ArrayList<>(cars.size());
        for (Car car : cars) {
            list.add(car.getCarId());
        }
        boolean remove = this.removeByIds(list);
        if (remove){
            return true;
        }
        return false;
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

        //将多个图片真实资源路径隔开
        String sourcePhoto = car.getSourcePhoto();
        String[] strings = sourcePhoto.split(";");
        ArrayList<String> arrayList = new ArrayList<>(strings.length);
        for (String string : strings) {
            arrayList.add(imgUploadPrifax + string);
        }
        String replaceAll = arrayList.toString().replaceAll(",", ";");
        car.setSourcePhoto(replaceAll.substring(1,replaceAll.length()-1));

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

        //判断给那些新增的没有前缀的条目加上前缀
        String[] sourcePhoto = car.getSourcePhoto().split(";");
        List<Object> arrayList = new ArrayList<>();
        for (String string : sourcePhoto) {
            if (!string.contains(imgUploadPrifax + "group")){
                arrayList.add(imgUploadPrifax + string);
            }
        }
        car.setSourcePhoto(arrayList.toString());
        return super.updateById(car);
    }

}
