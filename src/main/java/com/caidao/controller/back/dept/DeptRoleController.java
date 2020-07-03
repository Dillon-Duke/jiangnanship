package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.DeptRole;
import com.caidao.service.DeptRoleService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@RestController
@RequestMapping("/dept/role")
public class DeptRoleController {

    public static final Logger logger = LoggerFactory.getLogger(DeptRoleController.class);

    @Autowired
    private DeptRoleService deptRoleService;

    /**
     * 获得部门角色的分页数据
     * @param page
     * @param deptRole
     * @return
     */
    @ApiOperation("获得部门角色的分页数据")
    @GetMapping("/page")
    @RequiresPermissions("dept:dept:page")
    public ResponseEntity<IPage<DeptRole>> getDeptRolePage(Page<DeptRole> page , DeptRole deptRole){
        IPage<DeptRole> deptRolePage = deptRoleService.getDeptRolePage(page, deptRole);
        return ResponseEntity.ok(deptRolePage);
    }

    /**
     * 获取部门所有的角色
     * @return
     */
    @ApiOperation("获取部门所有的角色")
    @GetMapping("list")
    @RequiresPermissions("dept:role:list")
    public ResponseEntity<List<DeptRole>> getDeptRoleList(){
        List<DeptRole> roleList = deptRoleService.getDeptRoleList();
        return ResponseEntity.ok(roleList);
    }

    /**
     * 新增用户角色列表
     * @param deptRole
     * @return
     */
    @ApiOperation("新增用户角色列表")
    @PostMapping
    @RequiresPermissions("dept:role:save")
    public ResponseEntity<Boolean> addDeptRole(@RequestBody DeptRole deptRole){
        Boolean save = deptRoleService.save(deptRole);
        return ResponseEntity.ok(save);
    }

    /**
     * 编辑前获取被编辑的角色
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id获取角色")
    @RequiresPermissions("dept:role:info")
    public ResponseEntity<DeptRole> getSysRoleById(@PathVariable("id") Integer id){
        DeptRole deptRole = deptRoleService.getById(id);
        return ResponseEntity.ok(deptRole);
    }

    /**
     * 更新部门角色
     * @param deptRole
     * @return
     */
    @ApiOperation("更新部门用户")
    @PutMapping
    @RequiresPermissions("dept:role:update")
    public ResponseEntity<Boolean> updateDept(@RequestBody DeptRole deptRole){
        Boolean update = deptRoleService.updateById(deptRole);
        return ResponseEntity.ok(update);
    }

    /**
     * 删除部门角色
     * @param ids
     * @return
     */
    @ApiOperation("删除、批量删除部门用户")
    @DeleteMapping
    @RequiresPermissions("dept:role:delete")
    public ResponseEntity<Boolean> removeDepts(@RequestBody List<Integer> ids){
        Boolean removeByIds = deptRoleService.removeByIds(ids);
        return ResponseEntity.ok(removeByIds);
    }

}
