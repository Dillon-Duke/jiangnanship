package com.caidao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.caidao.param.UserParam;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.DeptUserCarApply;

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
     * 获取有空余时间的司机
     * @return
     */
    Map<String, Object> getFreeDrivers();

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    List<DeptUserCarApply> getFreeDriverById(Integer id);

    /**
     * 用户车辆绑定
     * @param deptUserCarApply
     * @return
     */
    Boolean userBindCar(List<DeptUserCarApply> deptUserCarApply);

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

    /**
     * 用户车辆解绑
     * @param ids 主键Id
     * @return
     */
    Boolean userNnBindCar(List<Integer> ids);

    /**
     * 用户的登录
     * @param userParam
     * @return
     */
    Map<String, String> login(UserParam userParam);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 向用户发送验证码
     * @param phone
     */
    void sendCheckCode(String phone);

    /**
     * 更新用户的密码
     * @param userParam
     * @return
     */
    boolean updateUserPassword(UserParam userParam);

    /**
     * 根据用户名和手机判断是否有这个人
     * @param userParam
     * @return
     */
    boolean checkNameAndPhone(UserParam userParam);

    /**
     * 登录首页
     * @param userId
     * @return
     */
    Map<String, Object> getHomePage(Integer userId);
}
