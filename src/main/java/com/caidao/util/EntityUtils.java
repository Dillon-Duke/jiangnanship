package com.caidao.util;

import com.caidao.pojo.*;

import java.time.LocalDateTime;

/**
 * @author tom
 */
public class EntityUtils {

    /**
     * 将信息封装为实体类返回
     * @param massageName
     * @param taskId
     * @param deptUsername
     * @return
     */
    public static AppTasksMassage getAppMassage(String massageName, Integer taskId, Integer userId, String deptUsername) {
        AppTasksMassage appTasksMassage = new AppTasksMassage();
        appTasksMassage.setIsRead(1);
        appTasksMassage.setMassageName(massageName);
        appTasksMassage.setTaskId(taskId);
        appTasksMassage.setCreateTime(LocalDateTime.now());
        appTasksMassage.setUserId(userId);
        appTasksMassage.setUsername(deptUsername);
        appTasksMassage.setState(1);
        return appTasksMassage;
    }

    /**
     * 将部门角色封装成实体类返回
     * @param deptId
     * @param roleId
     * @return
     */
    public static DeptDeptRole getDeptDeptRole(Integer deptId, Integer roleId) {
        DeptDeptRole role = new DeptDeptRole();
        role.setDeptId(deptId);
        role.setRoleId(roleId);
        return role;
    }

    /**
     * 将部门角色权限中间表封装为实体类后返回
     * @param deptId
     * @param roleId
     * @param configId
     * @return
     */
    public static DeptRoleAuthorisation getDeptRoleAuthorisation(Integer deptId, Integer roleId, Integer configId) {
        DeptRoleAuthorisation config = new DeptRoleAuthorisation();
        config.setDeptId(deptId);
        config.setRoleId(roleId);
        config.setConfigId(configId);
        return config;
    }

    /**
     * 将信息封装到实体类中返回
     * @param userId
     * @param roleId
     * @return
     */
    public static DeptUserRole getDeptUserRole(Integer userId, Integer roleId) {
        DeptUserRole userRole = new DeptUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
    }

    /**
     * 将内容封装到实体类中返回
     * @param taskId
     * @param reason
     * @param reasonDescription
     * @param createId
     * @return
     */
    public static PlatformReason getPlatformReason(String taskId, String reason, String reasonDescription,
                                                   Integer createId) {
        PlatformReason platformReason = new PlatformReason();
        platformReason.setTaskId(taskId);
        platformReason.setReason(reason);
        platformReason.setReasonDescription(reasonDescription);
        platformReason.setCreateDate(LocalDateTime.now());
        platformReason.setCreateId(createId);
        return platformReason;
    }

    /**
     * 将内容封装到实体类中返回
     * @param roleId
     * @param menuId
     * @return
     */
    public static SysRoleMenu getSysRoleMenu(Integer roleId, Integer menuId) {
        SysRoleMenu sysRoleMenu = new SysRoleMenu();
        sysRoleMenu.setRoleId(roleId);
        sysRoleMenu.setMenuId(menuId);
        return sysRoleMenu;
    }

    /**
     * 将内容封装到实体类中返回
     * @param userId
     * @param roleId
     * @return
     */
    public static SysUserRole getSysUserRole(Integer userId, Integer roleId) {
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(userId);
        sysUserRole.setRoleId(roleId);
        return sysUserRole;
    }

    /**
     * 将内容封装到实体类中返回
     * @param businessKey
     * @param carId
     * @param startTime
     * @param workNum
     * @param workShift
     * @param endTime
     * @param carPlant
     * @param driverId
     * @param driverName
     * @param operatorId
     * @param operatorName
     * @return
     */
    public static DeptUserCarApply getDeptUserCar(Integer businessKey, Integer carId, LocalDateTime startTime,
                                                  String workNum, String workShift, LocalDateTime endTime,
                                                  Integer carPlant, Integer driverId, String driverName,
                                                  Integer operatorId, String operatorName){
        DeptUserCarApply deptUserCarApply = new DeptUserCarApply();
        deptUserCarApply.setBusinessKey(businessKey);
        deptUserCarApply.setCarId(carId);
        deptUserCarApply.setCarPlant(carPlant);
        deptUserCarApply.setDriverId(driverId);
        deptUserCarApply.setDriverName(driverName);
        deptUserCarApply.setEndTime(endTime);
        deptUserCarApply.setOperatorId(operatorId);
        deptUserCarApply.setOperatorName(operatorName);
        deptUserCarApply.setStartTime(startTime);
        deptUserCarApply.setWorkNum(workNum);
        deptUserCarApply.setWorkShift(workShift);
        return deptUserCarApply;
    }

}

