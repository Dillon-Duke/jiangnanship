package com.caidao.controller.front.car;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.common.ResponseEntity;
import com.caidao.pojo.Car;
import com.caidao.service.SysCarService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appCar/car")
@Slf4j
public class AppCar {

    public static final Logger logger =  LoggerFactory.getLogger(AppCar.class);

    @Autowired
    private SysCarService sysCarService;


    /**
     * 获取车辆信息
     * @param page
     * @param car
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取分页车辆信息")
    public ResponseEntity getRoleList(Page<Car> page , Car car){
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<Car> sysRoles = sysCarService.findSysCarPage(page, car);
        return ResponseEntity.ok(sysRoles);
    }

    /**
     * 查询数据库可用车辆
     * @return
     */
    @ApiOperation("查询数据库可用车辆")
    @GetMapping("/count")
    public ResponseEntity getCarCount(){

        log.info("查询数据库可用车辆");
        Integer count = sysCarService.getCarCount();
        return ResponseEntity.ok(count);
    }

    /**
     * 根据条件查询可用车辆信息
     * @return
     */
    @ApiOperation("根据条件查询可用车辆信息")
    @PostMapping("/selectConditionCar")
    public ResponseEntity<List<Car>> selectConditionCar(@RequestBody Car car){

        Assert.notNull(car,"筛选条件不能为空");
        log.info("根据条件查询可用车辆信息");

        List<Car> carList = sysCarService.selectConditionCar(car);
        return ResponseEntity.ok(carList);
    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id查询车辆信息")
    public ResponseEntity<Car> getCarInfoById(@PathVariable("id") Integer id){

        Assert.notNull(id,"id 不能为空");
        log.info("查询车辆id为{}的车辆信息",id);

        Car car = sysCarService.getById(id);
        return ResponseEntity.ok(car);
    }

}
