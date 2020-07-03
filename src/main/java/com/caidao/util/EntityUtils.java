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
    public static AppMassage getAppMassage(String massageName, Integer taskId, String deptUsername) {
        AppMassage appMassage = new AppMassage();
        appMassage.setIsRead(1);
        appMassage.setMassageName(massageName);
        appMassage.setTaskId(taskId);
        appMassage.setCreateTime(LocalDateTime.now());
        appMassage.setDeptUsername(deptUsername);
        return appMassage;
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
     * @param bindUserId
     * @param carId
     * @param startTime
     * @param endTime
     * @param prsId
     * @return
     */
    public static CarPlatformApply getCarPlatformApply(Integer bindUserId, Integer carId, LocalDateTime startTime,
                                                       LocalDateTime endTime, Integer prsId){
        CarPlatformApply apply = new CarPlatformApply();
        apply.setBindUserId(bindUserId);
        apply.setCarId(carId);
        apply.setStartTime(startTime);
        apply.setEndTime(endTime);
        apply.setPrsId(prsId);
        return apply;
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
    public static DeptUserCar getDeptUserCar(Integer businessKey, Integer carId, LocalDateTime startTime,
                                             String workNum, String workShift, LocalDateTime endTime,
                                             Integer carPlant, Integer driverId, String driverName,
                                             Integer operatorId, String operatorName){
        DeptUserCar deptUserCar = new DeptUserCar();
        deptUserCar.setBusinessKey(businessKey);
        deptUserCar.setCarId(carId);
        deptUserCar.setCarPlant(carPlant);
        deptUserCar.setDriverId(driverId);
        deptUserCar.setDriverName(driverName);
        deptUserCar.setEndTime(endTime);
        deptUserCar.setOperatorId(operatorId);
        deptUserCar.setOperatorName(operatorName);
        deptUserCar.setStartTime(startTime);
        deptUserCar.setWorkNum(workNum);
        deptUserCar.setWorkShift(workShift);
        return deptUserCar;
    }

}

