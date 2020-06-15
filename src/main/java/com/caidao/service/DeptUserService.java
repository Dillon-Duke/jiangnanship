package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.DeptUserCar;

import java.util.HashMap;
import java.util.List;

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
     * @param deptUser
     * @return
     */
    boolean updatePassById(DeptUser deptUser);

    /**
     * 获取有空余时间的司机
     * @return
     */
    HashMap<String, Object> getFreeDriver(DeptUser deptUser);

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    List<DeptUserCar> getFreeDriverById(Integer id);

    /**
     * 用户车辆绑定
     * @param deptUserCars
     * @return
     */
    boolean userBindCar(List<DeptUserCar> deptUserCars);
}
