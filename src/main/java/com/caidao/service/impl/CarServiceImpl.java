package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.CarMapper;
import com.caidao.mapper.DeptUserCarApplyMapper;
import com.caidao.mapper.PlatformApplyMapper;
import com.caidao.pojo.*;
import com.caidao.service.CarService;
import com.caidao.util.DateUtils;
import com.caidao.util.FastDfsClientUtils;
import com.caidao.util.MapUtils;
import com.caidao.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author Dillon
 * @since 2020-05-18
 */
@Service
@Slf4j
public class CarServiceImpl extends ServiceImpl<CarMapper, Car> implements CarService {

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private PlatformApplyMapper platformApplyMapper;

    @Autowired
    private DeptUserCarApplyMapper deptUserCarApplyMapper;

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
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<Car> carPage = carMapper.selectPage(page, new LambdaQueryWrapper<Car>()
                .eq(StringUtils.hasText(car.getCarName()), Car::getCarName, car.getCarName()));
        //处理展示第一张图片
        List<Car> cars = carPage.getRecords();
        List<Car> arrayList = new ArrayList<>(cars.size());
        for (Car car1 : cars) {
            String sourcePhoto = car1.getSourceImage();
            if (sourcePhoto != "" && sourcePhoto != null){
                String[] strings = sourcePhoto.split(";");
                car1.setSourceImage(strings[0]);
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
        Assert.notNull(cars,"批量删除车辆信息，车辆信息不能为空");
        log.info("批量删除车辆信息",cars);
        //删除图片
        for (Car car : cars) {
            for (String string : car.getSourceImage().split(PropertyUtils.STRING_SPILT_WITH_SEMICOLON)) {
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
        log.info("查询数据库可用车辆");
        Integer count = carMapper.getCarCount();
        return count;
    }

    /**
     * 根据条件查询可用车辆信息
     * 操作手册： = (等于用“eq”) ;> (大于用“gt”) ;<(小于用" lt") ;>=(大于等于用"ge") ;<=(小于等于用"le")
     * @return
     */
    @Override
    public List<Car> selectConditionCar(Car car) {

        Assert.notNull(car,"筛选条件不能为空");
        log.info("根据条件查询可用车辆信息");
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

        List<Car> carList = carMapper.selectList(new LambdaQueryWrapper<Car>()
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
        Assert.notNull(car,"车辆信息不能为空");
        log.info("新增车牌号为{}的车辆", car.getCarPlate());
        SysUser principal = (SysUser) SecurityUtils.getSubject().getPrincipal();
        car.setCreateId(principal.getUserId());
        car.setCreateDate(LocalDateTime.now());
        car.setState(1);

        //将多个图片真实资源路径隔开
        String sourcePhoto = car.getSourceImage();
        if (sourcePhoto != "" && sourcePhoto != null){
            String[] strings = sourcePhoto.split(";");
            ArrayList<String> arrayList = new ArrayList<>(strings.length);
            for (String string : strings) {
                arrayList.add(imgUploadPrifax + string);
            }
            String replaceAll = arrayList.toString().replaceAll(",", ";");
            car.setSourceImage(replaceAll.substring(1,replaceAll.length()-1));
        }
        return super.save(car);
    }

    /**
     * 复写编辑车辆，增加更新日期 ，状态
     * @param car
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateById(Car car) {
        Assert.notNull(car,"更新车辆信息 不能为空");
        log.info("更新车辆id为{}的车辆信息",car.getCarId());
        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        car.setUpdateId(principal.getUserId());
        car.setUpdateDate(LocalDateTime.now());
        //判断给那些新增的没有前缀的条目加上前缀
        String[] sourcePhoto = car.getSourceImage().split(";");
        List<Object> arrayList = new ArrayList<>();
        for (String string : sourcePhoto) {
            if (!string.contains(imgUploadPrifax + "group")){
                arrayList.add(imgUploadPrifax + string);
            }
        }
        car.setSourceImage(arrayList.toString());
        return super.updateById(car);
    }

    /**
     * 车辆与任务做绑定 多太车与一个任务作为绑定
     * @param deptUserCarApplies
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean saveOrBindTaskWithCar(List<DeptUserCarApply> deptUserCarApplies) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        Assert.notNull(deptUserCarApplies,"车辆绑定信息不能未空");
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
            log.info("车辆id为{}的车辆们绑定任务id为{}的任务",deptUserCarApply.getCarId(),deptUserCarApply.getBusinessKey());
        }
        //车辆与任务进行绑定 车辆为原子单位
        return deptUserCarApplyMapper.insertBatches(deptUserCarApplies);
    }

    /**
     * 根据id查询对应的条目
     * @param id
     * @return
     */
    @Override
    public Car getById(Serializable id) {
        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);
        return super.getById(id);
    }

    /**
     * 获得空闲的、在使用的车辆已经对应的司机信息
     * @return
     */
    @Override
    public Map<String, Object> getAllAndFreeCarWithDrivers() {
        //获取所有车辆列表
        List<Car> carList = carMapper.selectList(null);
        //获取所有的部门用户车辆中间表
        List<DeptUserCarApply> deptUserCarApplies = deptUserCarApplyMapper.selectList(null);
        //从部门车辆中间表中统计多少个车辆被绑定了
        Map<Integer, Integer> bindCarCount = new HashMap<>(deptUserCarApplies.size());
        Map<Integer, DeptUserCarApply> carDriver = new HashMap<>(deptUserCarApplies.size());
        Map<Integer, DeptUserCarApply> carOperate = new HashMap<>(deptUserCarApplies.size());
        //统计每辆车的任务次数
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
            Integer carId = deptUserCarApply.getCarId();
            Integer bindCarId = bindCarCount.get(carId);
            if (bindCarId == null) {
                bindCarCount.put(carId,1);
            } else {
                bindCarCount.put(carId,bindCarCount.get(carId) + 1);
            }
        }
        //返回显示车辆司机和操作员信息
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
            Integer carId = deptUserCarApply.getCarId();
            if (carId == null) {
                carDriver.put(carId, deptUserCarApply);
                carOperate.put(carId, deptUserCarApply);
            } else if (deptUserCarApply.getStartTime().isBefore(carDriver.get(carId).getStartTime())) {
                carDriver.remove(carId);
                carDriver.put(carId, deptUserCarApply);
                carOperate.remove(carId);
                carOperate.put(carId, deptUserCarApply);
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Car car : carList) {
            Integer catTaskCount = bindCarCount.get(car.getCarId());
            String carDrivers = carDriver.get(car.getCarId()).getDriverName();
            String carOperates = carOperate.get(car.getCarId()).getOperatorName();
            list.add(MapUtils.getMap("car",car,"catTaskCount",catTaskCount,"carDrivers",carDrivers,"carOperates",carOperates));
        }
        Integer carCount = bindCarCount.size();
        Integer useCarCount = carCount;
        Integer freeCarCount = carList.size() - carCount;
        List<Map<String, Object>> carDetails = list;
        return MapUtils.getMap("useCarCount",useCarCount,"freeCarCount",freeCarCount,"carDetails",carDetails);
    }

    /**
     * 获得所有的车辆信息，有车辆任务的显示车辆的任务，没有车辆任务的显示为空闲车辆
     * @param date
     * @return
     */
    @Override
    public Map<String, Object> getAllCarsWithHaveTasksAndNoTasks(LocalDateTime date) {
        //判断是否有传日期类，如果没有，则默认查询当天的所有任务
        if (date == null) {
            date = LocalDateTime.now();
        }
        //获取传入日期的0点时间
        LocalDateTime minDate = LocalDateTime.of(date.toLocalDate(), LocalTime.MIN);
        //获取传入日期的24点时间
        LocalDateTime maxDate = LocalDateTime.of(date.toLocalDate(), LocalTime.MAX);
        //获取所有的车辆信息
        List<Car> carList = carMapper.selectList(null);
        //获取当天所有的已经分分配好的车辆任务列表
        List<DeptUserCarApply> userCarApplyList = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                                                .gt(DeptUserCarApply::getStartTime,minDate)
                                                .lt(DeptUserCarApply::getEndTime,maxDate));
        //获取所有的申请任务Id
        LinkedList<Integer> applyIds = new LinkedList<>();
        //获取所有的车辆Id
        Set<Integer> carIds = new HashSet<>();
        for (DeptUserCarApply deptUserCarApply : userCarApplyList) {
            applyIds.add(deptUserCarApply.getBusinessKey());
            //获得有任务的车辆Id
            carIds.add(deptUserCarApply.getCarId());
        }
        //查询对应所有的申请任务
        List<PlatformApply> applyList = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                .in(PlatformApply::getPrsId, applyIds));
        //将有任务的车辆放到map中
        HashMap<String, Car> map = new HashMap<>(carIds.size());
        //排除所有车辆中有任务的车辆
        for (Car car : carList) {
            if (carIds.contains(car.getCarId())) {
                carList.remove(car);
                map.put("taskCars",car);
            }
        }
        //对空闲车辆按照车牌号进行从小到大排序
        carList.sort(Comparator.comparing(Car::getCarPlate).thenComparing(Car::getCarWight,Comparator.reverseOrder()));
        HashMap<String, Object> taskCars = new HashMap<>(carIds.size());
        //通过车辆Id获取对应的申请业务信息
        for (int i = 0; i < map.size(); i++) {
            Long totalTimeCount = null;
            List<PlatformApply> applies = new LinkedList<>();
            Car car = map.get(i);
            //获取到所有的业务ID
            List<Integer> businessKeys = new LinkedList<>();
            for (DeptUserCarApply userCarApply : userCarApplyList) {
                if (car.getCarId().equals(userCarApply.getCarId())) {
                    businessKeys.add(userCarApply.getBusinessKey());
                    //计算时长
                    totalTimeCount += DateUtils.getTimesLengthBetweenEndTimeAndStartTime(userCarApply.getEndTime(),userCarApply.getStartTime());
                }
            }
            //将对应的申请信息放在list列表中 并且计算时长
            for (PlatformApply platformApply : applyList) {
                if (businessKeys.contains(platformApply.getPrsId())) {
                    applies.add(platformApply);
                }
            }
            //对申请任务进行时间排序
            applies.sort((x,y) -> Long.compare(x.getStartTime(),y.getState()));
            //处理时长，转换为double
            double timeCount = totalTimeCount;
            taskCars.put("car",car);
            taskCars.put("applyList",applies);
            taskCars.put("totalTimeCount",timeCount/60000);
            taskCars.put("carTaskCount",businessKeys.size());
        }
        return MapUtils.getMap("freeCarCount",carList.size(),"taskCount",applyList.size(),"freeCars",carList,"taskCars",taskCars);
    }

    /**
     * 将已经绑定车辆的任务进行排序
     * @return
     */
    @Override
    public void sortBindApplyTasks(Integer businessKey) {
        //todo 看看算法是怎么实现这个自动排序的，到时候在写这个以时间为基准的排序方式
    }

    /**
     * 自动绑定车辆与申请的关系
     * @return
     */
    @Override
    public void autoCompareCarWithApply() {
        //todo 看看算法是怎么实现这个绑定的，插入的时候实行批量插入
        //最后进行批量的绑定插入
        List<DeptUserCarApply> applies = new ArrayList<>(0);
        deptUserCarApplyMapper.insertBatches(applies);
    }

    public static void main(String[] args) {
        Long a = 3000L;
        Long b = 3000L;
        System.out.println(((double)(a  + b))/60000);
    }
}
