package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.DeptUserCarApply;
import com.caidao.service.DeptUserService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@RestController
@RequestMapping("/dept/user")
public class DeptUserController {

    public static final Logger logger = LoggerFactory.getLogger(DeptUserController.class);

    @Autowired
    private DeptUserService deptUserService;

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("查询部门用户的分页数据")
    @RequiresPermissions("dept:user:page")
    public ResponseEntity<IPage<DeptUser>> getDeptUserPage(Page<DeptUser> page,DeptUser deptUser){
        IPage<DeptUser> userPage = deptUserService.getDeptUserPage(page, deptUser);
        return ResponseEntity.ok(userPage);
    }

    /**
     * 新增用户
     * @param deptUser
     * @return
     */
    @RequiresPermissions("dept:user:save")
    @PostMapping
    @ApiOperation("新增用户")
    public ResponseEntity<String> addUser(@RequestBody DeptUser deptUser){
        boolean save = deptUserService.save(deptUser);
        if(save){
            return ResponseEntity.ok("新增部门用户成功");
        }
        return ResponseEntity.ok("新增部门用户失败");
    }

    /**
     * 通过id获取用户数据
     * @param id
     * @return
     */
    @RequiresPermissions("dept:user:info")
    @GetMapping("info/{id}")
    @ApiOperation("通过id获取用户数据")
    public ResponseEntity<DeptUser> getUserById(@PathVariable("id") Integer id){
        DeptUser service = deptUserService.getById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * 修改用户
     * @param deptUser
     * @return
     * @throws IOException
     */
    @PutMapping
    @ApiOperation("修改部门用户")
    @RequiresPermissions("dept:user:update")
    public ResponseEntity<String> updateUser(@RequestBody DeptUser deptUser) {
        boolean update = deptUserService.updateById(deptUser);
        if (update){
            return ResponseEntity.ok("更新部门用户成功");
        }
        return ResponseEntity.ok("更新部门用户失败");
    }

    /**
     * 批量删除用户
     * 真删除
     * @param ids
     * @return
     */
    @ApiOperation("批量删除用户")
    @RequiresPermissions("dept:user:delete")
    @DeleteMapping
    public ResponseEntity<String> beachDel(@RequestBody List<Long> ids){
        boolean remove = deptUserService.removeByIds(ids);
        if (remove){
            return ResponseEntity.ok("删除部门用户成功");
        }
        return ResponseEntity.ok("删除部门用户失败");
    }


    /**
     * 可以做条件查询 ，开始时间与结束时间
     * 获取有空余时间的司机
     * @return
     */
    @ApiOperation("获取有空余时间的司机")
    @GetMapping("/getFreeDrivers")
    public ResponseEntity<Map<String, Object>> getFreeDrivers(){
        Map<String, Object> driver = deptUserService.getFreeDrivers();
        return ResponseEntity.ok(driver);
    }

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    @ApiOperation("获取司机的任务")
    @GetMapping("/getFreeDriverById/{id}")
    public ResponseEntity<List<DeptUserCarApply>> getFreeDriverById(@PathVariable("id") Integer id){
        List<DeptUserCarApply> driverById = deptUserService.getFreeDriverById(id);
        return ResponseEntity.ok(driverById);
    }

    /**
     * 用户车辆绑定
     * @param deptUserCarApply
     * @return
     */
    @ApiOperation("用户车辆绑定")
    @PostMapping("/userBindCar")
    public ResponseEntity<Boolean> userBindCar(@RequestBody List<DeptUserCarApply> deptUserCarApply){
        Boolean bindCar = deptUserService.userBindCar(deptUserCarApply);
        return ResponseEntity.ok(bindCar);
    }
}
