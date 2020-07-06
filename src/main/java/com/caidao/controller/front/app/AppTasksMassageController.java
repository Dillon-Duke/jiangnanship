package com.caidao.controller.front.app;


import com.caidao.pojo.AppTasksMassage;
import com.caidao.service.AppTasksMassageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-06-22
 */
@RestController
@RequestMapping("/app/massage")
public class AppTasksMassageController {

    @Autowired
    private AppTasksMassageService appTasksMassageService;

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    @GetMapping("/getUserNotReadMassage/{username}")
    @ApiOperation("获得用户未读信息")
    public ResponseEntity<List<AppTasksMassage>> getUserNotReadMassage(@PathVariable("username") String username){
        List<AppTasksMassage> appTasksMassageList = appTasksMassageService.getUserNotReadMassage(username);
        return ResponseEntity.ok(appTasksMassageList);
    }

    /**
     * 获得用户已读信息列表
     * @param username
     * @return
     */
    @GetMapping("/getUserReadMassage/{username}")
    @ApiOperation("获得用已未读信息")
    public ResponseEntity<List<AppTasksMassage>> getUserReadMassage(@PathVariable("username") String username){
        List<AppTasksMassage> appTasksMassageList = appTasksMassageService.getUserReadMassage(username);
        return ResponseEntity.ok(appTasksMassageList);
    }

    /**
     * 批量删除用户的待办任务
     * @param idList
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除用户的消息")
    public ResponseEntity<List<AppTasksMassage>> deleteTaskMassages(@RequestBody List<Integer> idList){
        boolean result = appTasksMassageService.deleteTaskMassages(idList);
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
