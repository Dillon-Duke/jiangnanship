package com.caidao.controller.back.system;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.pojo.AppCommonMsg;
import com.caidao.service.AppCommonMsgService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-07-07
 */
@RestController
@RequestMapping("/app/common/msg")
public class AppCommonMsgController {

    @Autowired
    private AppCommonMsgService appCommonMsgService;

    /**
     * 获取分页的用户数据
     * @param page
     * @param appCommonMsg
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("获取分页的消息数据")
    public ResponseEntity<IPage<AppCommonMsg>> getAppCommonPage(Page<AppCommonMsg> page, AppCommonMsg appCommonMsg){
        IPage<AppCommonMsg> appCommonPage = appCommonMsgService.getAppCommonPage(page,appCommonMsg);
        return ResponseEntity.ok(appCommonPage);
    }

    /**
     * 新增通用消息
     * @param appCommonMsg
     * @return
     */
    @PostMapping
    @ApiOperation("新增通用消息")
    public ResponseEntity<Void> saveAppCommonMassage(@RequestBody AppCommonMsg appCommonMsg){
        appCommonMsgService.saveAppCommonMassage(appCommonMsg);
        return ResponseEntity.ok().build();
    }

    /**
     * 通过id获取消息数据
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    @ApiOperation("通过id获取消息数据")
    public ResponseEntity<AppCommonMsg> getAppCommonMassageById(@PathVariable("id") Integer id){
        AppCommonMsg appCommonMsg = appCommonMsgService.getAppCommonMassageById(id);
        return ResponseEntity.ok(appCommonMsg);
    }

    /**
     * 批量删除消息 假删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除消息")
    public ResponseEntity<Void> beachDeleteAppCommonMassage(@RequestBody List<Integer> ids){
        appCommonMsgService.beachDeleteAppCommonMassage(ids);
        return ResponseEntity.ok().build();
    }

    /**
     * 修改消息
     * @param appCommonMsg
     * @return
     */
    @PutMapping
    @ApiOperation("修改消息")
    public ResponseEntity<Boolean> updateAppCommonMassage(@RequestBody AppCommonMsg appCommonMsg) {
        Boolean updateById = appCommonMsgService.updateAppCommonMassage(appCommonMsg);
        return ResponseEntity.ok(updateById);
    }

    /**
     * 发布或取消发布消息
     * @param appCommonMsg
     * @return
     */
    @PutMapping("publishOrNot")
    @ApiOperation("修改消息")
    public ResponseEntity<Boolean> publishOrNotAppCommonMassage(@RequestBody AppCommonMsg appCommonMsg) {
        appCommonMsgService.publishAppCommonMassage(appCommonMsg);
        return ResponseEntity.ok().build();
    }

}
