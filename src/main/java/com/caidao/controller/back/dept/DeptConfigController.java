package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.DeptAuthorisation;
import com.caidao.service.DeptConfigService;
import io.swagger.annotations.ApiOperation;
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
 * @since 2020-05-27
 */
@RestController
@RequestMapping("/dept/config")
public class DeptConfigController {

    public static final Logger logger = LoggerFactory.getLogger(DeptConfigController.class);

    @Autowired
    private DeptConfigService deptConfigService;

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptAuthorisation
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取当前页权限字典数据")
    @RequiresPermissions("dept:config:page")
    public ResponseEntity<IPage<DeptAuthorisation>> getSysConfigPage(Page<DeptAuthorisation> page, DeptAuthorisation deptAuthorisation){
        IPage<DeptAuthorisation> configPage = deptConfigService.findPage(page, deptAuthorisation);
        return ResponseEntity.ok(configPage);
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询菜单")
    @RequiresPermissions("dept:config:list")
    public ResponseEntity<List<DeptAuthorisation>> addSysmenu(){
        List<DeptAuthorisation> findDeptAuthorisation = deptConfigService.getListDept();
        return ResponseEntity.ok(findDeptAuthorisation);
    }

    /**
     * 新增字典值
     * @param deptAuthorisation
     * @return
     */
    @PostMapping
    @ApiOperation("新增权限字典数据")
    @RequiresPermissions("dept:config:save")
    public ResponseEntity<Boolean> addSysConfig(@RequestBody DeptAuthorisation deptAuthorisation){
        Boolean save = deptConfigService.save(deptAuthorisation);
        return ResponseEntity.ok(save);
    }

    /**
     * 更新前获取对象
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    @ApiOperation("通过ID查询权限字典数据")
    @RequiresPermissions("dept:config:info")
    public ResponseEntity<DeptAuthorisation> beforeUpdate(@PathVariable("id") Integer id){
        DeptAuthorisation deptAuthorisation = deptConfigService.getById(id);
        return ResponseEntity.ok(deptAuthorisation);
    }

    /**
     * 更新字典值  看看是否需要进行必须值判断
     * @param deptAuthorisation
     * @return
     */
    @PutMapping
    @ApiOperation("更新权限字典数据")
    @RequiresPermissions("dept:config:update")
    public ResponseEntity<Boolean> updateSysConfig(@RequestBody DeptAuthorisation deptAuthorisation){
        Boolean update = deptConfigService.updateById(deptAuthorisation);
        return ResponseEntity.ok(update);
    }

    /**
     * 批量删除
     * @param idList
     * @return
     */
    @SysLogs("批量删除权限字典数据")
    @DeleteMapping
    @ApiOperation("批量删除权限字典数据")
    @RequiresPermissions("dept:config:delete")
    public ResponseEntity<Boolean> deleteSysConfig(@RequestBody List<Integer> idList){
        Boolean remove = deptConfigService.removeByIds(idList);
        return ResponseEntity.ok(remove);
    }

}
