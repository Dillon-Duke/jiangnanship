package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.DeptUser;
import com.caidao.entity.DeptUserRole;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.DeptUserRoleMapper;
import com.caidao.service.DeptUserService;
import com.caidao.util.Md5Utils;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Service
public class DeptUserServiceImpl extends ServiceImpl<DeptUserMapper, DeptUser> implements DeptUserService {

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptUserRoleMapper deptUserRoleMapper;

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    @Override
    public IPage<DeptUser> getDeptUserPage(Page<DeptUser> page, DeptUser deptUser) {
        IPage<DeptUser> selectPage = deptUserMapper.selectPage(page, new LambdaQueryWrapper<DeptUser>()
                .eq(StringUtils.hasText(deptUser.getUsername()), DeptUser::getUsername, deptUser.getUsername()));
        return selectPage;
    }

    /**
     *通过名字获取对应的用户信息
     * @param deptUser
     * @return
     */
    @Override
    public DeptUser getUserByUsername(String deptUser) {
        DeptUser user = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUsername, deptUser));
        return user;
    }

    /**
     * 新增用户 添加角色信息，部门信息
     * @param deptUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean save(DeptUser deptUser) {

        //查询数据库中是否有该用户名，如果有，则提示更换用户名
        DeptUser user = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUsername, deptUser.getUsername()));
        if (user != null){
            throw new RuntimeException("该名称已被注册，请更换其他名称");
        }

        deptUser.setCreateDate(LocalDateTime.now());

        String salt = UUID.randomUUID().toString();
        //生成盐值
        deptUser.setUserSalt(salt);
        deptUser.setState(1);

        //设置加盐密码
        String password = deptUser.getPassword();
        ByteSource bytes = ByteSource.Util.bytes(salt.getBytes());
        String saltPass = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
        deptUser.setPassword(saltPass);

        List<Integer> roleIdList = deptUser.getRoleIdList();

        //判断如果选中对应角色，新增用户角色信息
        if (roleIdList == null || roleIdList.isEmpty()){
            deptUser.setUserRoleName("空");
            deptUser.setUserDeptName("空");
            return super.save(deptUser);
        }

        //新增用户对应的角色和部门信息  此处需要多次调用数据库 ，使用自定义sql
        for (Integer integer : roleIdList) {
            Map<String, Object> map = deptUserMapper.selectDeptRole(integer);
            deptUser.setUserRoleId(integer);
            deptUser.setUserRoleName(map.get("role_name").toString());
            deptUser.setUserDeptId(Integer.parseInt(map.get("dept_id").toString()));
            deptUser.setUserDeptName(map.get("dept_name").toString());
        }

        //获得插入的数据id
        deptUserMapper.insert(deptUser);
        Integer userId = deptUser.getUserId();
        DeptUserRole userRole = new DeptUserRole();
        for (Integer integer : roleIdList) {
            userRole.setUserId(userId);
            userRole.setRoleId(integer);
            deptUserRoleMapper.insert(userRole);
        }

        //判断是否插入成功
        if (userId == 0){
            throw new RuntimeException("部门用户新增失败");
        }
        return true;
    }

    /**
     * 通过id获取用户数据
     * @param id
     * @return
     */
    @Override
    public DeptUser getById(Serializable id) {

        //获取用户角色中间表信息
        List<DeptUserRole> roleList = deptUserRoleMapper.selectList(new LambdaQueryWrapper<DeptUserRole>()
                .eq(DeptUserRole::getUserId, id));

        //将角色id 放到list列表里面去
        List<Integer> arrayList = new ArrayList<>(0);
        if ((roleList != null) && (!roleList.isEmpty())){
            for (DeptUserRole deptUserRole : roleList) {
                arrayList.add(deptUserRole.getRoleId());
            }
        }
        DeptUser deptUser = super.getById(id);
        deptUser.setRoleIdList(arrayList);
        return deptUser;
    }

    /**
     * 修改用户
     * @param deptUser
     * @return
     * @throws
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateById(DeptUser deptUser) {

        //查询数据库中是否有该用户名，如果有，则提示更换用户名
        DeptUser user1 = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUsername, deptUser.getUsername()));
        if (user1 != null){
            throw new RuntimeException("该名称已被注册，请更换其他名称");
        }

        //判断密码是否重新输入过，如果输入过，则改密码，若无，则直接存数据库里面
        String oldPassword = deptUser.getPassword();
        DeptUser user = deptUserMapper.selectById(deptUser.getUserId());
        if (user.getPassword() != oldPassword){
            //设置加盐密码
            String password = deptUser.getPassword();
            ByteSource bytes = ByteSource.Util.bytes(deptUser.getUserSalt().getBytes());
            String saltPass = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
            deptUser.setPassword(saltPass);
        }

        //设置更新日期
        deptUser.setUpdateDate(LocalDateTime.now());

        //更新前先删除用户角色中间表
        int delete = deptUserRoleMapper.delete(new LambdaQueryWrapper<DeptUserRole>()
                .eq(DeptUserRole::getUserId, deptUser.getUserId()));
        if (delete == 0){
            throw new RuntimeException("部门用户删除失败，请重试");
        }

        //判断为空 则直接返回
        List<Integer> roleIdList = deptUser.getRoleIdList();
        if (roleIdList == null || roleIdList.isEmpty()){
            deptUser.setUserRoleName("空");
            deptUser.setUserDeptName("空");
            return super.updateById(deptUser);
        }

        //新增用户角色中间表
        for (Integer integer : roleIdList) {
            DeptUserRole userRole = new DeptUserRole();
            userRole.setUserId(deptUser.getUserId());
            userRole.setRoleId(integer);
            deptUserRoleMapper.insert(userRole);
        }

        //从其他三张表中查询对应的角色部门信息，填到用户字段里面
        for (Integer integer : roleIdList) {
            Map<String, Object> map = deptUserMapper.selectDeptRole(integer);
            deptUser.setUserRoleId(integer);
            deptUser.setUserRoleName(map.get("role_name").toString());
            deptUser.setUserDeptId(Integer.parseInt(map.get("dept_id").toString()));
            deptUser.setUserDeptName(map.get("dept_name").toString());
        }

        return super.updateById(deptUser);
    }

    /**
     * 批量删除用户
     * 真删除
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {

        //判断中间表是否删除
        int count = 0;
        for (Serializable serializable : idList) {
            int delete = deptUserRoleMapper.delete(new LambdaQueryWrapper<DeptUserRole>()
                    .eq(DeptUserRole::getUserId, serializable));
            count+=delete;
        }
        if (count == 0){
            throw new RuntimeException("用户角色删除失败");
        }

        //判断部门用户表是否删除
        Integer batchIds = deptUserMapper.deleteBatchIds(idList);
        if (batchIds == 0){
            throw new RuntimeException("用户删除失败");
        }
        return true;
    }
}