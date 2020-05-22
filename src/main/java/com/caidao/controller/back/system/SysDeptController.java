package com.caidao.controller.back.system;


import com.caidao.entity.SysDept;
import com.caidao.service.SysDeptService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
    @ApiOperation("查询所有的菜单信息")
    @RequiresPermissions("sys:dept:page")
    public ResponseEntity<List<SysDept>> getTable(){
        log.info("查询所有部门信息");
        List<SysDept> sysDept = sysDeptService.findSysDept();
        return ResponseEntity.ok(sysDept);
    }

}
