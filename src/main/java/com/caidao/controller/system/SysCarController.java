package com.caidao.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.entity.SysCar;
import com.caidao.entity.SysUser;
import com.caidao.service.SysCarService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
@RequestMapping("/sys/car")
@Slf4j
public class SysCarController {

    @Autowired
    private SysCarService sysCarService;


    /**
     * 获取车辆信息
     * @param page
     * @param sysCar
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<IPage<SysCar>> getRoleList(Page<SysCar> page , SysCar sysCar){
        log.info("获取所有车辆的信息总共有{}页，每页展示{}个",page.getCurrent(),page.getSize());
        IPage<SysCar> sysRoles = sysCarService.findSysCarPage(page,sysCar);
        return ResponseEntity.ok(sysRoles);
    }

    /**
     * 新增车辆
     * @return
     */
    /** @RequiresPermissions("sys:car:save") */
    @SysLogs("新增车辆")
    @PostMapping
    public ResponseEntity<String> addCar(@RequestBody SysCar sysCar){
        Assert.notNull(sysCar,"车辆信息不能为空");
        log.info("新增车牌号为{}的车辆",sysCar.getCarPlate());
        SysUser principal = (SysUser)SecurityUtils.getSubject().getPrincipal();
        sysCar.setCreateId(principal.getUserId());
        Assert.notNull(sysCar.getCarName(),"车辆名称不能为空");
        boolean save = sysCarService.save(sysCar);
        if (save){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("新增车辆失败");

    }

    /**
     * 根据id查询对应的条目
     * 修改前查询
     */
    @GetMapping("info/{id}")
    public ResponseEntity<SysCar> getCarInfoById(@PathVariable("id") Integer id){
        Assert.notNull(id,"id 不能为空");
        SysCar sysCar = sysCarService.getById(id);
        return ResponseEntity.ok(sysCar);
    }

    /**
     * 更新车辆信息
     * @param sysCar
     * @return
     */
    @PutMapping
    public ResponseEntity<String> updateCar(@RequestBody SysCar sysCar){
        boolean updateCar = sysCarService.updateById(sysCar);
        if (updateCar){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("更新失败");
    }

    /**
     * 删除信息 真删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public ResponseEntity<String> deleteByIds(@RequestBody List<Integer> ids){
        boolean removeByIds = sysCarService.removeByIds(ids);
        if (removeByIds){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok("删除失败");
    }

}
