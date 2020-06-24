package com.caidao.controller.back.car;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.Car;
import com.caidao.pojo.SysUser;
import com.caidao.service.CarService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@RestController
@RequestMapping("/car/car")
public class CarController {

    public static final Logger logger = LoggerFactory.getLogger(CarController.class);

    @Autowired
    private CarService carService;

    /**
     * 获取车辆信息
     * @param page
     * @param car
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取分页车辆信息")
    @RequiresPermissions("car:car:page")
    public ResponseEntity<IPage<Car>> getRoleList(Page<Car> page , Car car){
        IPage<Car> sysRoles = carService.findSysCarPage(page, car);
        return ResponseEntity.ok(sysRoles);
    }

    /**
     * 新增车辆
     * @param car
     * @return
     */
    @PostMapping
    @ApiOperation("新增车辆信息")
    @RequiresPermissions("car:car:save")
    public ResponseEntity<String> addCar(@RequestBody Car car){
        boolean save = carService.save(car);
        if (save){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("新增车辆失败");

    }

    /**
     * 根据id查询对应的条目
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id查询车辆信息")
    @RequiresPermissions("car:car:info")
    public ResponseEntity<Car> getCarInfoById(@PathVariable("id") Integer id){
        Car car = carService.getById(id);
        return ResponseEntity.ok(car);
    }

    /**
     * 更新车辆信息
     * @param car
     * @return
     */
    @PutMapping
    @ApiOperation("更新车辆信息")
    @RequiresPermissions("car:car:update")
    public ResponseEntity<String> updateCar(@RequestBody Car car){
        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        car.setUpdateId(principal.getUserId());

        boolean updateCar = carService.updateById(car);
        if (updateCar){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("更新失败");
    }

    /**
     * 删除信息 真删除
     * @param cars
     * @return
     */
    @SysLogs("删除车辆信息")
    @DeleteMapping
    @ApiOperation("删除车辆信息")
    @RequiresPermissions("car:car:delete")
    public ResponseEntity<String> deleteByIds(@RequestBody List<Car> cars){
        boolean removeByIds = carService.batchRemoveByIds(cars);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("删除失败");
    }

}
