package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.UserCarBindParam;
import com.caidao.param.UserParam;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.DeptUserCar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-28
 */
public interface DeptUserService extends IService<DeptUser> {

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    IPage<DeptUser> getDeptUserPage(Page<DeptUser> page, DeptUser deptUser);

    /**
     *通过名字获取对应的用户信息
     * @param toString
     * @return
     */
    DeptUser getUserByUsername(String toString);

    /**
     * 通过用户名和手机号判断用户是否存在
     * @param username
     * @param phone
     * @return
     */
    DeptUser findUserByUsernameAndPhone(String username, String phone);

    /**
     * 忘记密码，更新用户的密码
     * @param userParam
     * @return
     */
    boolean updatePassByPhone(UserParam userParam);

    /**
     * 获取有空余时间的司机
     * @return
     */
    HashMap<String, Object> getFreeDrivers();

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    List<DeptUserCar> getFreeDriverById(Integer id);

    /**
     * 用户车辆绑定
     * @param param
     * @param taskId
     * @return
     */
    boolean userBindCar(UserCarBindParam param, String taskId);

    /**
     * 获得用户的app首页个人信息
     * @param deptUser
     * @return
     */
    Map<String, String> getDeptUserMassage(DeptUser deptUser);

    /**
     * 根据用户名和部门模糊查找
     * @param deptUser
     * @return
     */
    List<DeptUser> getUsersByNameLikeAndDeptLike(DeptUser deptUser);

    /**
     * 根据部门和角色查询用户
     * @param deptUser
     * @return
     */
    List<DeptUser> getUsersByRoleAndDept(DeptUser deptUser);
}
