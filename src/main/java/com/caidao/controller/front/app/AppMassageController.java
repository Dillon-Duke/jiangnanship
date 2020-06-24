package com.caidao.controller.front.app;


import com.caidao.common.ResponseEntity;
import com.caidao.pojo.AppMassage;
import com.caidao.service.AppMassageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@RestController
@RequestMapping("/app/massage")
public class AppMassageController {

    @Autowired
    private AppMassageService appMassageService;

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    @GetMapping("/getUserNotReadMassage/{username}")
    @ApiOperation("获得用户未读信息")
    public ResponseEntity<List<AppMassage>> getUserNotReadMassage(@PathVariable("username") String username){
        List<AppMassage> appMassageList = appMassageService.getUserNotReadMassage(username);
        return ResponseEntity.ok(appMassageList);
    }

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    @GetMapping("/getUserReadMassage/{username}")
    @ApiOperation("获得用已未读信息")
    public ResponseEntity<List<AppMassage>> getUserReadMassage(@PathVariable("username") String username){
        List<AppMassage> appMassageList = appMassageService.getUserReadMassage(username);
        return ResponseEntity.ok(appMassageList);
    }
}
