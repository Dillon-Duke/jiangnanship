package com.caidao.controller.front.app;

import com.caidao.common.MyResponseEntity;
import com.caidao.pojo.Dept;
import com.caidao.pojo.DeptRole;
import com.caidao.pojo.DeptUser;
import com.caidao.service.DeptService;
import com.caidao.service.DeptUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tom
 */

@RestController
@RequestMapping("/appDept/user")
public class AppUser {


    @Autowired
    private DeptUserService deptUserService;

    @Autowired
    private DeptService deptService;

    /**
     * 根据id查询对应的条目
     * @param id
     * @return
     */
    @GetMapping("/chooseById/{id}")
    @ApiOperation("通过id获取用户数据")
    public MyResponseEntity<DeptUser> chooseById(@PathVariable("id") Integer id){
        DeptUser deptUser = deptUserService.getById(id);
        return MyResponseEntity.ok(deptUser);
    }

    /**
     * 根据用户名和部门模糊查找
     * @param deptUser
     * @return
     */
    @PostMapping("/getUsersByNameLikeAndDeptLike")
    @ApiOperation("按照类型选择运输分段信息")
    public MyResponseEntity<List<DeptUser>> getUsersByNameLikeAndDeptLike(@RequestBody DeptUser deptUser){
        List<DeptUser> userList = deptUserService.getUsersByNameLikeAndDeptLike(deptUser);
        return MyResponseEntity.ok(userList);
    }

    /**
     * 查询所有的部门信息
     * @return
     */
    @PostMapping("/getAllDept")
    @ApiOperation("查询所有的部门信息")
    public MyResponseEntity<List<Dept>> getAllDept(){
        List<Dept> deptList = deptService.getListDept();
        return MyResponseEntity.ok(deptList);
    }

    /**
     * 根据部门ID获取部门角色列表
     * @param deptId
     * @return
     */
    @PostMapping("/getDeptRoles/{deptId}")
    @ApiOperation("通过id查询运输分段信息")
    public MyResponseEntity<List<DeptRole>> getDeptRoles(@PathVariable("deptId") Integer deptId){
        List<DeptRole> deptRoles = deptService.getDeptRoles(deptId);
        return MyResponseEntity.ok(deptRoles);
    }

    /**
     * 根据部门和角色查询用户
     * @param deptUser
     * @return
     */
    @PostMapping("/getUsersByRoleAndDept")
    @ApiOperation("根据部门和角色查询用户")
    public MyResponseEntity<List<DeptUser>> getUsersByRoleAndDept(@RequestBody DeptUser deptUser){
        List<DeptUser> userList = deptUserService.getUsersByRoleAndDept(deptUser);
        return MyResponseEntity.ok(userList);
    }

}
