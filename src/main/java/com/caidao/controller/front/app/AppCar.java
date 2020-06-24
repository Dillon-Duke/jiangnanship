package com.caidao.controller.front.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.common.ResponseEntity;
import com.caidao.pojo.Car;
import com.caidao.pojo.CarConfig;
import com.caidao.service.CarConfigService;
import com.caidao.service.CarService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author tom
 */
@RestController
@RequestMapping("/appCar/car")
public class AppCar {

    public static final Logger logger =  LoggerFactory.getLogger(AppCar.class);

    @Autowired
    private CarService carService;

    @Autowired
    private CarConfigService carConfigService;

    /**
     * 获取车辆信息
     * @param page
     * @param car
     * @return
     */
    @PostMapping("/getCarPage")
    @ApiOperation("获取分页车辆信息")
    public ResponseEntity getCarPage(Page<Car> page , Car car){
        IPage<Car> sysRoles = carService.findSysCarPage(page, car);
        return ResponseEntity.ok(sysRoles);
    }

    /**
     * 查询数据库可用车辆
     * @return
     */
    @ApiOperation("查询数据库可用车辆")
    @GetMapping("/getCarCount")
    public ResponseEntity getCarCount(){
        Integer count = carService.getCarCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 根据条件查询可用车辆信息
     * @return
     */
    @ApiOperation("根据条件查询可用车辆信息")
    @PostMapping("/selectConditionCar")
    public ResponseEntity<List<Car>> selectConditionCar(@RequestBody Car car){
        List<Car> carList = carService.selectConditionCar(car);
        return ResponseEntity.ok(carList);
    }

    /**
     * 根据id查询对应的条目
     * @param id
     * @return
     */
    @GetMapping("getCarInfo/{id}")
    @ApiOperation("通过id查询车辆信息")
    public ResponseEntity<Car> getCarInfoById(@PathVariable("id") Integer id){
        Car car = carService.getById(id);
        return ResponseEntity.ok(car);
    }

    /**
     * 获得不同类型车辆的工作内容
     * @param configKey
     * @return
     */
    @GetMapping("getCarContent/{configKey}")
    @ApiOperation("获得不同类型车辆的工作内容")
    public ResponseEntity<List<CarConfig>> getCarContent(@PathVariable("configKey") String configKey){
        List<CarConfig> carConfigList = carConfigService.getCarContent(configKey);
        return ResponseEntity.ok(carConfigList);
    }

    /**
     * 获得空闲的、在使用的车辆已经对应的司机信息
     * @return
     */
    @GetMapping("getAllAndFreeCarWithDrivers")
    @ApiOperation("获得空闲的、在使用的车辆已经对应的司机信息")
    public ResponseEntity<Map<String, Object>> getAllAndFreeCarWithDrivers(){
        Map<String, Object> map = carService.getAllAndFreeCarWithDrivers();
        return ResponseEntity.ok(map);
    }

}
