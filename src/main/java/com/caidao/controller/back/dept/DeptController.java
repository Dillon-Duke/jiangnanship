package com.caidao.controller.back.dept;

import com.caidao.pojo.Dept;
import com.caidao.pojo.SysUser;
import com.caidao.service.DeptService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.Assert;
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
 * @since 2020-05-21
 */
@RestController
@RequestMapping("/dept/dept")
@Slf4j
public class DeptController {

    public static final Logger logger = LoggerFactory.getLogger(DeptController.class);

    @Autowired
    private DeptService deptService;

    /**
     * 获取所有的部门信息
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("查询所有的部门信息")
    @RequiresPermissions("dept:dept:page")
    public ResponseEntity<List<Dept>> getTable(){
        log.info("查询所有部门信息");
        List<Dept> dept = deptService.findSysDept();
        return ResponseEntity.ok(dept);
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询菜单")
    @RequiresPermissions("dept:dept:list")
    public ResponseEntity<List<Dept>> addSysmenu(){
        List<Dept> findDept = deptService.getListDept();
        return ResponseEntity.ok(findDept);
    }

    /**
     * 新增部门列表
     * @return
     */
    @ApiOperation("新增部门列表")
    @PostMapping
    @RequiresPermissions("dept:dept:save")
    public ResponseEntity<String> addDept(@RequestBody Dept dept){

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        //如果部门为空，则抛异常
        Assert.notNull(dept,"新增部门信息为空");
        log.info("新增名为{}的部门", dept.getDeptName());

        dept.setCreateId(sysUser.getUserId());
        boolean save = deptService.save(dept);

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
    @RequiresPermissions("dept:dept:info")
    public ResponseEntity<Dept> getDeptById(@PathVariable("id") Integer id){

        Assert.notNull(id,"部门id不能为空");
        log.info("获取id为{}的不们信息",id);

        Dept dept = deptService.getById(id);
        return ResponseEntity.ok(dept);
    }

    /**
     * 部门的更新
     * @param dept
     * @return
     */
    @ApiOperation("更新部门信息")
    @PutMapping
    @RequiresPermissions("dept:dept:update")
    public ResponseEntity<String> updateDept(@RequestBody Dept dept){

        Assert.notNull(dept,"部门不能为空");
        log.info("获取id为{}的不们信息", dept.getDeptId());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        dept.setUpdateId(sysUser.getUserId());
        boolean update = deptService.updateById(dept);

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
    @RequiresPermissions("dept:dept:delete")
    public ResponseEntity<String> deleteDept(@PathVariable("id") Integer id){

        Assert.notNull(id,"删除部门id为空");
        log.info("删除id为{}的部门",id);

        boolean remove = deptService.removeById(id);
        if (remove){
            return ResponseEntity.ok("部门删除成功");
        }
        return ResponseEntity.ok("部门删除失败");

    }

}
