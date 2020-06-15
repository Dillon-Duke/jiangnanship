package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.DeptRole;
import com.caidao.pojo.SysUser;
import com.caidao.service.DeptRoleService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@RestController
@RequestMapping("/dept/role")
@Slf4j
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
        log.info("获取部门角色当前页{}，页大小{}",page.getCurrent(),page.getSize());
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
        log.info("获取部门角色列表");
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
    public ResponseEntity<String> addDeptRole(@RequestBody DeptRole deptRole){

        Assert.notNull(deptRole,"新增部门角色不能为空");
        log.info("新增角色名为{}的部门角色",deptRole.getRoleName());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptRole.setCreateId(sysUser.getUserId());
        boolean save = deptRoleService.save(deptRole);
        if (save){
            return ResponseEntity.ok("新增部门角色成功");
        }
        return ResponseEntity.ok("新增部门角色失败");
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

        Assert.notNull(id,"角色id{}不能为空");
        log.info("查询角色id为{}的部门角色",id);

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
    public ResponseEntity<String> updateDept(@RequestBody DeptRole deptRole){

        Assert.notNull(deptRole,"更新角色{}不能为空");
        log.info("更新角色名称为{}的部门角色",deptRole.getRoleName());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptRole.setUpdateId(sysUser.getUserId());

        boolean update = deptRoleService.updateById(deptRole);
        if (update){
            return ResponseEntity.ok("角色更新成功");
        }
        return ResponseEntity.ok("角色更新成功");
    }

    /**
     * 删除部门角色
     * @param ids
     * @return
     */
    @ApiOperation("删除、批量删除部门用户")
    @DeleteMapping
    @RequiresPermissions("dept:role:delete")
    public ResponseEntity<String> removeDepts(@RequestBody List<Integer> ids){

        Assert.notNull(ids,"删除角色{}不能为空");
        log.info("删除角色Id为{}的部门角色",ids);

        boolean removeByIds = deptRoleService.removeByIds(ids);
        if (removeByIds){
            return ResponseEntity.ok("删除更新成功");
        }
        return ResponseEntity.ok("删除更新成功");
    }

}
