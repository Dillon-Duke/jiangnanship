package com.caidao.controller.back.dept;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.DeptUser;
import com.caidao.entity.SysUser;
import com.caidao.service.DeptUserService;
import com.caidao.util.PropertyUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@RestController
@RequestMapping("/dept/user")
@Slf4j
public class DeptUserController {

    public static final Logger logger = LoggerFactory.getLogger(DeptUserController.class);

    @Autowired
    private DeptUserService deptUserService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("查询部门用户的分页数据")
    @RequiresPermissions("dept:user:page")
    public ResponseEntity<IPage<DeptUser>> getDeptUserPage(Page<DeptUser> page,DeptUser deptUser){

        Assert.notNull(page != null,"分页数据不能为空");
        log.info("查询部门分页数据，当前页{}，页大小{}",page.getCurrent(),page.getSize());

        IPage<DeptUser> userPage = deptUserService.getDeptUserPage(page, deptUser);
        return ResponseEntity.ok(userPage);
    }

    /**
     * 新增用户
     * @param deptUser
     * @return
     */
    @RequiresPermissions("dept:user:save")
    @PostMapping
    @ApiOperation("新增用户")
    public ResponseEntity<String> addUser(@RequestBody DeptUser deptUser){

        Assert.notNull(deptUser,"新增用户信息不能为空");
        log.info("新增用户名为{}的用户",deptUser.getUsername());

        SysUser sysUser2 = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptUser.setCreateId(sysUser2.getUserId());
        boolean save = deptUserService.save(deptUser);
        if(save){
            return ResponseEntity.ok("新增部门用户成功");
        }
        return ResponseEntity.ok("新增部门用户失败");
    }

    /**
     * 通过id获取用户数据
     * @param id
     * @return
     */
    @RequiresPermissions("dept:user:info")
    @GetMapping("info/{id}")
    @ApiOperation("通过id获取用户数据")
    public ResponseEntity<DeptUser> getUserById(@PathVariable("id") Integer id){

        Assert.notNull(id,"查询部门id信息不能为空");
        log.info("查询用户id为{}的用户",id);

        DeptUser service = deptUserService.getById(id);
        return ResponseEntity.ok(service);
    }

    /**
     * 修改用户
     * @param deptUser
     * @return
     * @throws IOException
     */
    @PutMapping
    @ApiOperation("修改部门用户")
    @RequiresPermissions("dept:user:update")
    public ResponseEntity<String> updateUser(@RequestBody DeptUser deptUser) {

        Assert.notNull(deptUser,"更新部门用户信息不能为空");
        log.info("更新用户id为{}的用户",deptUser.getUserId());

        SysUser sysUser = (SysUser)SecurityUtils.getSubject().getPrincipal();

        //设置更新人id
        deptUser.setUpdateId(sysUser.getUserId());
        boolean update = deptUserService.updateById(deptUser);

        //获取对应的登录用户session
        String sessionKey = redisTemplate.opsForValue().get(PropertyUtils.APP_USER_LOGIN_SESSION_ID+sysUser.getUsername());

        //判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
        if (sessionKey != null) {
            redisTemplate.delete(PropertyUtils.USER_SESSION+sessionKey);
            redisTemplate.delete(PropertyUtils.APP_USER_LOGIN_SESSION_ID+sysUser.getUsername());
        }

        if (update){
            return ResponseEntity.ok("更新部门用户成功");
        }
        return ResponseEntity.ok("更新部门用户失败");
    }

    /**
     * 批量删除用户
     * 真删除
     * @param ids
     * @return
     */
    @RequiresPermissions("dept:user:delete")
    @DeleteMapping
    public ResponseEntity<String> beachDel(@RequestBody List<Long> ids){

        Assert.notNull(ids,"删除id不能为空");
        log.info("删除id为{}的用户",ids);
        boolean remove = deptUserService.removeByIds(ids);
        if (remove){
            return ResponseEntity.ok("删除部门用户成功");
        }
        return ResponseEntity.ok("删除部门用户失败");
    }

}
