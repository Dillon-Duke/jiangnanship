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
            String sourcePhoto = car1.getSourcePhoto();
            if (sourcePhoto != "" && sourcePhoto != null){
                String[] strings = sourcePhoto.split(";");
                car1.setSourcePhoto(strings[0]);
                arrayList.add(car1);
            }
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
     * 查询数据库可用车辆
     * @return
     */
    @Override
    public Integer getCarCount() {
        Integer count = sysCarMapper.getCarCount();
        return count;
    }

    /**
     * 根据条件查询可用车辆信息
     * 操作手册： = (等于用“eq”) ;> (大于用“gt”) ;<(小于用" lt") ;>=(大于等于用"ge") ;<=(小于等于用"le")
     * @return
     */
    @Override
    public List<Car> selectConditionCar(Car car) {

        //因为数字格式不能为空，所以 需要提前将数字转为string格式，StringUtils.hasText才会判定为没有
        String carLength = null;
        String carWight = null;
        String carHeight = null;
        if (car.getCarLength() != 0){
            carLength = car.getCarLength().toString();
        } else if (car.getCarWight() != 0){
            carWight = car.getCarWight().toString();
        }else if (car.getCarHeight() != 0){
            carHeight = car.getCarHeight().toString();
        }

        List<Car> carList = sysCarMapper.selectList(new LambdaQueryWrapper<Car>()
                //模糊查询车的名字
                .like(StringUtils.hasText(car.getCarName()), Car::getCarName, car.getCarName())
                //查询车辆载重大于等于多少吨的车
                .ge(StringUtils.hasText(car.getFullWeight().toString()), Car::getFullWeight, car.getFullWeight())
                //查询车辆长为多少的车
                .eq(StringUtils.hasText(carLength), Car::getCarLength, car.getCarLength())
                //查询车辆宽为多少的车
                .eq(StringUtils.hasText(carWight), Car::getCarWight, car.getCarWight())
                //查询车辆高为多少的车
                .eq(StringUtils.hasText(carHeight), Car::getCarHeight, car.getCarHeight()));
        return carList;
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
        if (sourcePhoto != "" && sourcePhoto != null){
            String[] strings = sourcePhoto.split(";");
            ArrayList<String> arrayList = new ArrayList<>(strings.length);
            for (String string : strings) {
                arrayList.add(imgUploadPrifax + string);
            }
            String replaceAll = arrayList.toString().replaceAll(",", ";");
            car.setSourcePhoto(replaceAll.substring(1,replaceAll.length()-1));
        }
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
