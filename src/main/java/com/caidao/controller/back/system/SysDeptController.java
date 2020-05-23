package com.caidao.controller.back.system;

import com.caidao.entity.SysDept;
import com.caidao.entity.SysUser;
import com.caidao.service.SysDeptService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
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
 * @since 2020-05-21
 */
@RestController
@RequestMapping("/sys/dept")
@Slf4j
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 获取所有的部门信息
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("查询所有的部门信息")
    @RequiresPermissions("sys:dept:page")
    public ResponseEntity<List<SysDept>> getTable(){
        log.info("查询所有部门信息");
        List<SysDept> sysDept = sysDeptService.findSysDept();
        return ResponseEntity.ok(sysDept);
    }

    /**
     * 获取整个部门的列表
     * @return
     */
    @ApiOperation("新增部门列表")
    @PostMapping("/save")
    @RequiresPermissions("sys:dept:save")
    public ResponseEntity<String> addDept(SysDept sysDept){

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        //如果部门为空，则抛异常
        Assert.notNull(sysDept,"新增部门信息为空");
        log.info("新增名为{}的部门",sysDept.getDeptName());

        sysDept.setCreateId(sysDept.getDeptId());
        boolean save = sysDeptService.save(sysDept);

        if (save){
            return ResponseEntity.ok("新增部门成功");
        }
        return ResponseEntity.ok("新增部门失败");
    }

    /**
     * 更新前通过id查询对应的部门信息
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    @ApiOperation("通过id获取部门")
    @RequiresPermissions("sys:dept:info")
    public ResponseEntity<SysDept> getDeptById(@PathVariable("id") Integer id){

        Assert.notNull(id,"部门id不能为空");
        log.info("获取id为{}的不们信息",id);

        SysDept sysDept = sysDeptService.getById(id);
        return ResponseEntity.ok(sysDept);
    }

    /**
     * 部门的更新
     * @param sysDept
     * @return
     */
    @ApiOperation("更新部门信息")
    @PutMapping("/update")
    @RequiresPermissions("sys:dept:update")
    public ResponseEntity<String> updateDept(@RequestBody SysDept sysDept){

        Assert.notNull(sysDept,"部门不能为空");
        log.info("获取id为{}的不们信息",sysDept.getDeptId());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        sysDept.setUpdateId(sysUser.getUserId());
        boolean update = sysDeptService.updateById(sysDept);

        if (update){
            return ResponseEntity.ok("部门更新成功");
        }
        return ResponseEntity.ok("部门更新失败");
    }

    /**
     * 删除部门信息
     * @param id
     * @return
     */
    @ApiOperation("删除系统部门")
    @DeleteMapping("{id}")
    @RequiresPermissions("sys:dept:delete")
    public ResponseEntity<String> deleteDept(@PathVariable("id") Integer id){

        Assert.notNull(id,"删除部门id为空");
        log.info("删除id为{}的部门",id);

        boolean remove = sysDeptService.removeById(id);
        if (remove){
            return ResponseEntity.ok("部门删除成功");
        }
        return ResponseEntity.ok("部门删除失败");

    }

}
