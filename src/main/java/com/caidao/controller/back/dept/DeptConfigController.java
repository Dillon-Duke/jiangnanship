package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.anno.SysLogs;
import com.caidao.pojo.DeptAuthorisation;
import com.caidao.pojo.SysUser;
import com.caidao.service.DeptConfigService;
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
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Dillon
 * @since 2020-05-27
 */
@RestController
@RequestMapping("/dept/config")
@Slf4j
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

        Assert.notNull(deptAuthorisation, "sysConfig must not be null");
        log.info("查询配置类的当前页{}，页大小{}",page.getCurrent(),page.getSize());

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
    public ResponseEntity<String> addSysConfig(@RequestBody DeptAuthorisation deptAuthorisation){

        Assert.notNull(deptAuthorisation,"新增数据字典参数不能为空");
        log.info("新增参数名为{}的数据", deptAuthorisation.getParamKey());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptAuthorisation.setCreateId(sysUser.getUserId());

        boolean save = deptConfigService.save(deptAuthorisation);
        if (save){
            return ResponseEntity.ok("新增权限字典成功");
        }
        return ResponseEntity.ok("新增权限字典失败");
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

        Assert.state(id !=null, "Id不能为空");
        log.info("新增参数ID为{}的数据",id);

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
    public ResponseEntity<String> updateSysConfig(@RequestBody DeptAuthorisation deptAuthorisation){

        Assert.notNull(deptAuthorisation,"更新数据字典参数不能为空");
        log.info("更新参数名为{}的数据", deptAuthorisation.getParamKey());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptAuthorisation.setUpdateId(sysUser.getUserId());

        boolean update = deptConfigService.updateById(deptAuthorisation);
        if (update){
            return ResponseEntity.ok("更新权限字典成功");
        }
        return ResponseEntity.ok("更新权限字典失败");
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
    public ResponseEntity<String> deleteSysConfig(@RequestBody List<Integer> idList){

        Assert.state(idList.size() !=0, "Id不能为空");
        log.info("删除参数ID为{}的数据",idList);

        boolean remove = deptConfigService.removeByIds(idList);
        if (remove){
            return ResponseEntity.ok("删除权限字典成功");
        }
        return ResponseEntity.ok("删除权限字典失败");
    }

}
