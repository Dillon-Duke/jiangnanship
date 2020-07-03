package com.caidao.controller.back.dept;

import com.caidao.pojo.Dept;
import com.caidao.service.DeptService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<List<Dept>> selectList(){
        List<Dept> pages = deptService.selectList();
        return ResponseEntity.ok(pages);
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询菜单")
    @RequiresPermissions("dept:dept:list")
    public ResponseEntity<List<Dept>> selectDeptList(){
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
    public ResponseEntity<Boolean> addDept(@RequestBody Dept dept){
        Boolean save = deptService.save(dept);
        return ResponseEntity.ok(save);
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
    public ResponseEntity<Boolean> updateDept(@RequestBody Dept dept){
        Boolean update = deptService.updateById(dept);
        return ResponseEntity.ok(update);
    }

    /**
     * 删除部门信息
     * @param id
     * @return
     */
    @ApiOperation("删除系统部门")
    @DeleteMapping("{id}")
    @RequiresPermissions("dept:dept:delete")
    public ResponseEntity<Boolean> deleteDept(@PathVariable("id") Integer id){
        Boolean remove = deptService.removeById(id);
        return ResponseEntity.ok(remove);

    }

}
