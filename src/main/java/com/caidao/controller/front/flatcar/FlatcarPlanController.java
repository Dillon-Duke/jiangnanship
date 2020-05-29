package com.caidao.controller.front.flatcar;

import com.caidao.entity.FlatcarPlan;
import com.caidao.entity.SysUser;
import com.caidao.service.FlatcarPlanService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-23
 */
@RestController
@RequestMapping("/flatcar/plan")
@Slf4j
public class FlatcarPlanController {

    public static final Logger logger =  LoggerFactory.getLogger(FlatcarPlanController.class);

    @Autowired
    private FlatcarPlanService flatcarPlanService;

    /**
     * 保存一个平板车计划任务
     * @param flatcarPlan
     * @return
     */
    @ApiOperation("保存一个平板车计划任务,未提交状态")
    @PostMapping("/save")
    public ResponseEntity<String> saveFlatcarPlanTask(FlatcarPlan flatcarPlan){

        Assert.notNull(flatcarPlan,"平板车计划任务前端传值为空");
        log.info("保存一个任务名为{}的平板车任务",flatcarPlan.getJobName());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        flatcarPlan.setCreateId(sysUser.getUserId());
        flatcarPlan.setApplyName(sysUser.getUsername());
        boolean result = flatcarPlanService.save(flatcarPlan);
        if (result){
            return ResponseEntity.ok("操作成功");
        }
        return ResponseEntity.ok("操作失败");
    }

    /**
     * 提交一个平板车计划任务  //返回一个
     * @param flatcarPlan
     * @return
     */
    @ApiOperation("提交一个平板车计划任务")
    @PostMapping("/apply")
    public ResponseEntity<Integer> applyFlatcarPlanTask(FlatcarPlan flatcarPlan){
        Assert.notNull(flatcarPlan,"平板车计划任务前端传值为空");
        log.info("申请一个任务名为{}的平板车任务",flatcarPlan.getJobName());

        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        Integer result = flatcarPlanService.applyFlatcarPlan(flatcarPlan,sysUser);
        if (result == -1){
            //如果返回-1 则表示新增用户申请失败
            return ResponseEntity.ok(-1);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 通过平板车申请任务查询具体的任务内容
     * @param id
     * @return
     */
    @ApiOperation("通过任务ID查询具体的任务")
    @GetMapping("{id}")
    public ResponseEntity<FlatcarPlan> getFlatcarPlanDetilByTaskId(@PathVariable("id") Integer id){
        Assert.notNull(id,"平板车计划任务ID不能为空");
        log.info("查询一个任务名为{}的平板车任务",id);
        FlatcarPlan flatcarPlan = flatcarPlanService.getById(id);
        return ResponseEntity.ok(flatcarPlan);
    }

    /**
     * 默认不需要传id 查询的是自己提交的任务列表，如果需要查询别人的任务列表，输入Id即可
     * @param id
     * @return
     */
    @ApiOperation("通过用户id获取用户用户的任务列表")
    @GetMapping("usertasklist/{id}")
    public ResponseEntity<List<FlatcarPlan>> getFlatcarPlanListByUser(@PathVariable("id") Integer id){

        SysUser sysUser = (SysUser)SecurityUtils.getSubject().getPrincipal();
        log.info("获取用户{}的任务列表",sysUser.getUsername());

        //判断是否查看别人的任务
        if (id == null){
            id = sysUser.getUserId();
        }

        List<FlatcarPlan> planList = flatcarPlanService.selectListByApplyId(id);
        return ResponseEntity.ok(planList);
    }

    /**
     * 完成给人任务的审批
     * @param flatcarPlan
     * @param taskId
     * @return
     */
    @ApiOperation("完成个人任务的审批")
    public ResponseEntity<String> complayUserTask(@RequestBody FlatcarPlan flatcarPlan,String taskId, String reasion){

        Assert.notNull(flatcarPlan,"审批平板车计划任务不能为空");
        log.info("审批一个任务名为{}的平板车任务",flatcarPlan.getJobName());

        Boolean result = flatcarPlanService.complayUserTask(flatcarPlan,taskId,reasion);

        if (result){
            return ResponseEntity.ok("审批成功");
        }
        return ResponseEntity.ok("审批失败");
    }

}
