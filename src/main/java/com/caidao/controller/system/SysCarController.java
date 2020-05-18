package com.caidao.controller.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysCar;
import com.caidao.entity.SysRole;
import com.caidao.service.SysCarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
public class SysCarController {

    @Autowired
    private SysCarService sysCarService;

    @GetMapping("/page")
    public ResponseEntity<IPage<SysCar>> getRoleList(Page<SysCar> page , SysCar sysCar){
        IPage<SysCar> sysRoles = sysCarService.findSysCarPage(page,sysCar);
        return ResponseEntity.ok(sysRoles);
    }

}
