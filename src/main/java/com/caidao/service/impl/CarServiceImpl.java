package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.mapper.CarMapper;
import com.caidao.mapper.DeptUserCarApplyMapper;
import com.caidao.mapper.PlatformApplyMapper;
import com.caidao.pojo.Car;
import com.caidao.pojo.DeptUserCarApply;
import com.caidao.pojo.PlatformApply;
import com.caidao.pojo.SysUser;
import com.caidao.service.CarService;
import com.caidao.util.DateUtils;
import com.caidao.util.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
                .eq(Car::getState,1)
                .like(StringUtils.hasText(car.getCarName()), Car::getCarName, car.getCarName())
                .eq(Car::getState,1)
                .orderByDesc(Car::getCreateDate));
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
     * 批量删除对应车辆信息 假删除
     * @param cars
     * @return
     */
    @Override
    public boolean batchRemoveByIds(List<Car> cars) {
        Assert.notNull(cars,"批量删除车辆信息，车辆信息不能为空");
        log.info("批量删除车辆信息",cars);
        List<Integer> collect = cars.stream().map(Car::getCarId).collect(Collectors.toList());
        boolean result = carMapper.updateBatchesState(collect);
        return result;
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
                .eq(StringUtils.hasText(carHeight), Car::getCarHeight, car.getCarHeight())
                .eq(Car::getState,1));
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
            } else if (deptUserCarApply.getRealStartTime().isBefore(carDriver.get(carId).getRealStartTime())) {
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
    public Map<String, Object> getAllCarsWithHaveTasksAndNoTasks(Long date) {
        //判断是否有传日期类，如果没有，则默认查询当天的所有任务
        LocalDateTime dateTime = null;
        if (date == null) {
            dateTime = LocalDateTime.now();
        } else {
            dateTime = DateUtils.secondTimeStamp2LocalDateTime(date);
        }
        //获取传入日期的0点时间
        LocalDateTime minDate = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN);
        //获取传入日期的24点时间
        LocalDateTime maxDate = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX);
        //获取所有的车辆信息
        List<Car> carList = carMapper.selectList(null);
        //获取当天所有的已经分分配好的车辆任务列表
        List<DeptUserCarApply> userCarApplyList = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                                                .gt(DeptUserCarApply::getRealStartTime,minDate)
                                                .lt(DeptUserCarApply::getRealEndTime,maxDate));
        //如果没有分配好的车辆，返回所有的车辆
        if (userCarApplyList.size() == 0) {
            return MapUtils.getMap("freeCarCount",carList.size(),"taskCount",0,"freeCars",carList,"taskCars",null);
        } else {
            //获取所有的申请任务Id
            List<Integer> applyIds = userCarApplyList.stream().map((x) -> x.getBusinessKey()).collect(Collectors.toList());
            //获取所有的车辆Id
            Set<Integer> carIds = userCarApplyList.stream().map((x) -> x.getCarId()).collect(Collectors.toSet());
            //查询对应所有的申请任务
            List<PlatformApply> applyList = platformApplyMapper.selectList(new LambdaQueryWrapper<PlatformApply>()
                    .in(PlatformApply::getPrsId, applyIds));
            //将有任务的车辆放到list中
            List<Car> taskCars = carList.stream().filter((x) -> carIds.contains(x.getCarId())).collect(Collectors.toList());
            //排除所有车辆中有任务的车辆
            carList = carList.stream().filter((x) -> !carIds.contains(x.getCarId())).collect(Collectors.toList());
            //对空闲车辆按照车牌号进行从小到大排序
            carList.sort(Comparator.comparing(Car::getCarPlate).thenComparing(Car::getCarWight,Comparator.reverseOrder()));
            Map<String, Object> taskCar = null;
            for (Car car : taskCars) {
                List<DeptUserCarApply> userCarApplies = userCarApplyList.stream().filter((x) -> car.getCarId().equals(x.getCarId())).collect(Collectors.toList());
                userCarApplies.stream().sorted(Comparator.comparing(DeptUserCarApply::getRealStartTime));
                List<Integer> businessKeys =userCarApplies.stream().map(x -> x.getBusinessKey()).collect(Collectors.toList());
                Long totalTimeCount = 0L;
                for (DeptUserCarApply userCarApply : userCarApplies) {
                    totalTimeCount += DateUtils.getTimesLengthBetweenEndTimeAndStartTimeMailSecond(userCarApply.getRealEndTime(), userCarApply.getRealStartTime());
                }
                //将对应的申请信息放在list列表中
                List<PlatformApply> applies = applyList.stream().filter((x) -> businessKeys.contains(x.getPrsId())).collect(Collectors.toList());
                //根据真实的开始时间进行排序
                List<Map<String, Object>> maps = new ArrayList<>(userCarApplies.size());
                for (DeptUserCarApply userCarApply : userCarApplies) {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("realStartTime",userCarApply.getRealStartTime());
                    map.put("platformApplyEntity",applies.stream().filter((x) -> x.getPrsId().equals(userCarApply.getBusinessKey())).collect(Collectors.toList()).get(0));
                    maps.add(map);
                }
                //处理时长，转换为double
                double timeCount = totalTimeCount;
                //保留后面两位小数
                DecimalFormat df = new DecimalFormat("######0.00");
                String format = df.format(timeCount / 3600000);
                taskCar = MapUtils.getMap("car",car,"applyList",maps,"totalTimeCount",format,"carTaskCount",businessKeys.size());
            }
            return MapUtils.getMap("freeCarCount",carList.size(),"taskCount",applyList.size(),"freeCars",carList,"taskCars",taskCar);
        }
    }

    /**
     * 将已经绑定车辆的任务进行排序
     * @param sourceId
     * @param targetId
     * @return
     */
    @Override
    public Map<String, Object> changeBindTaskSort(Integer sourceId, Integer targetId) {
        Assert.notNull(sourceId,"对象Id不能为空");
        Assert.notNull(targetId,"目标Id不能为空");
        //获取所有的车辆任务
        List<DeptUserCarApply> applies = deptUserCarApplyMapper.selectList(null);
        //获取对应的资源对象
        DeptUserCarApply sourceTask = applies.stream().filter((x) -> x.getId().equals(sourceId)).collect(Collectors.toList()).get(0);
        //获取对应的目标对象
        DeptUserCarApply targetTask = applies.stream().filter((x) -> x.getId().equals(targetId)).collect(Collectors.toList()).get(0);
        //获取该车的所有任务
        List<DeptUserCarApply> carTasks = applies.stream().filter((x) -> x.getCarId().equals(sourceTask.getCarId())).collect(Collectors.toList());
        //获取所有的任务Ids
        List<Integer> platformIds = carTasks.stream().map(DeptUserCarApply::getBusinessKey).collect(Collectors.toList());
        //获取所有的对应的所有任务
        List<PlatformApply> platformApplies = platformApplyMapper.selectBatchIds(platformIds);
        //过滤掉车辆任务里面目标车辆和对象车辆任务Id
        List<Integer> otherTaskIds = platformIds.stream().filter((x) -> !x.equals(sourceTask.getBusinessKey())).filter((y) -> !y.equals(targetTask.getBusinessKey())).collect(Collectors.toList());
        //获取对应的资源任务
        PlatformApply sourcePlatformApply = platformApplies.stream().filter((x) -> x.getPrsId().equals(sourceTask.getBusinessKey())).collect(Collectors.toList()).get(0);
        Map<String, Object> sourceMassage = MapUtils.getMap("sourceTask", sourceTask, "sourcePlatformApply", sourcePlatformApply);
        //获取对应的目标任务
        PlatformApply targetPlatformApply = platformApplies.stream().filter((x) -> x.getPrsId().equals(targetTask.getBusinessKey())).collect(Collectors.toList()).get(0);
        Map<String, Object> targetMassage = MapUtils.getMap("targetTask", targetTask, "targetPlatformApply", targetPlatformApply);
        //其他任务集合
        ArrayList<Map<String, Object>> list = new ArrayList<>(carTasks.size() - 2);
        for (Integer id : otherTaskIds) {
            PlatformApply otherPlatformApply = platformApplies.stream().filter((x) -> x.getPrsId().equals(id)).collect(Collectors.toList()).get(0);
            DeptUserCarApply otherTask = applies.stream().filter((x) -> x.getBusinessKey().equals(id)).collect(Collectors.toList()).get(0);
            list.add(MapUtils.getMap("otherPlatformApply", otherPlatformApply, "otherTask", otherTask));
        }
        return MapUtils.getMap("sourceMassage",sourceMassage, "targetMassage",targetMassage, "otherMassages",list);
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

}
