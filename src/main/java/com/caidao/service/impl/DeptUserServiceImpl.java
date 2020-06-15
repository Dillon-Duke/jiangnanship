package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptUserCarMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.DeptUserRoleMapper;
import com.caidao.pojo.DeptUser;
import com.caidao.pojo.DeptUserCar;
import com.caidao.pojo.DeptUserRole;
import com.caidao.service.DeptUserService;
import com.caidao.util.Md5Utils;
import com.caidao.util.PropertyUtils;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private DeptUserCarMapper deptUserCarMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
            throw new MyException("该名称已被注册，请更换其他名称");
        }

        deptUser.setCreateDate(LocalDateTime.now());

        String salt = UUID.randomUUID().toString().replaceAll("-","");
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
            throw new MyException("部门用户新增失败");
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
        DeptUser user = deptUserMapper.selectById(deptUser.getUserId());
        if (!user.getUsername().equals(deptUser.getUsername())){
            DeptUser selectOne = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                    .eq(DeptUser::getUsername, deptUser.getUsername()));
            if (selectOne != null){
                throw new MyException("该名称已被注册，请更换其他名称");
            }
        }

        //判断密码是否重新输入过，如果输入过，则改密码，若无，则直接存数据库里面
        String password = deptUser.getPassword();
        if (password != null && password != ""){
            //设置加盐密码
            ByteSource bytes = ByteSource.Util.bytes(deptUser.getUserSalt().getBytes());
            String saltPass = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
            deptUser.setPassword(saltPass);
        } else {
            deptUser.setPassword(user.getPassword());
        }

        //设置更新日期
        deptUser.setUpdateDate(LocalDateTime.now());

        //更新前先删除用户角色中间表
        List<DeptUserRole> deptUserRoles = deptUserRoleMapper.selectList(new LambdaQueryWrapper<DeptUserRole>()
                                        .eq(DeptUserRole::getUserId, deptUser.getUserId()));
        if ((deptUserRoles != null) && (!deptUserRoles.isEmpty())){
            int delete = deptUserRoleMapper.delete(new LambdaQueryWrapper<DeptUserRole>()
                    .eq(DeptUserRole::getUserId, deptUser.getUserId()));
            if (delete == 0){
                throw new MyException("部门用户删除失败，请重试");
            }
        }

        //判断为空 则直接返回
        List<Integer> roleIdList = deptUser.getRoleIdList();
        if (roleIdList == null || roleIdList.isEmpty()){
            deptUser.setUserRoleName("无");
            deptUser.setUserDeptName("无");
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

        //获取被删除用户的token
        Object token = redisTemplate.opsForHash().get(PropertyUtils.ALL_USER_TOKEN, user.getUserSalt());

        //判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
        if (token != null) {
            redisTemplate.delete(PropertyUtils.USER_SESSION+token);
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
    @Transactional(rollbackFor = RuntimeException.class, propagation = Propagation.REQUIRES_NEW)
    public boolean removeByIds(Collection<? extends Serializable> idList) {

        //判断中间表是否删除
        int count = 0;
        for (Serializable serializable : idList) {
            int delete = deptUserRoleMapper.delete(new LambdaQueryWrapper<DeptUserRole>()
                    .eq(DeptUserRole::getUserId, serializable));
            count+=delete;
        }
        if (count == 0){
            throw new MyException("用户角色删除失败");
        }

        //判断部门用户表是否删除
        Integer batchIds = deptUserMapper.deleteBatchIds(idList);
        if (batchIds == 0){
            throw new MyException("用户删除失败");
        }
        return true;
    }

    /**
     * 通过用户名和手机号判断用户是否存在
     * @param username
     * @param phone
     * @return
     */
    @Override
    public DeptUser findUserByUsernameAndPhone(String username, String phone) {
        DeptUser deptUser = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUsername, username)
                .or(false)
                .eq(DeptUser::getPhone, phone));
        return deptUser;
    }

    /**
     * 忘记密码，更新用户的密码
     * @param deptUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class, propagation= Propagation.REQUIRES_NEW)
    public boolean updatePassById(DeptUser deptUser) {

        //设置更新盐值
        String password = deptUser.getPassword();
        ByteSource bytes = ByteSource.Util.bytes(deptUser.getUserSalt().getBytes());
        String saltPass = Md5Utils.getHashAndSaltAndTime(password, bytes, 1024);
        deptUser.setPassword(saltPass);

        //更新用户密码
        Integer update = deptUserMapper.updatePassById(deptUser);
        if (update == 0){
            return false;
        }
        return true;
    }

    /**
     * 获取有空余时间的司机
     * @return
     */
    @Override
    public HashMap<String, Object> getFreeDriver(DeptUser deptUser) {

        //查询用户车辆表当天所有有任务的人
        List<DeptUserCar> deptUserCarList = deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                                        .orderByAsc(DeptUserCar::getUsereId));

        //获取该部门所有的空闲人员
        List<DeptUser> deptUserList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                                                    .eq(DeptUser::getUserDeptId, deptUser.getUserDeptId())
                                                    .eq(DeptUser::getUserRoleId, deptUser.getUserRoleId()));

        HashMap<String, Object> map = new HashMap<>();
        map.put("freeDriver",deptUserList);
        map.put("taskDriver",deptUserCarList);

        return map;

    }

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    @Override
    public List<DeptUserCar> getFreeDriverById(Integer id) {

        List<DeptUserCar> deptUserCars = deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                .eq(DeptUserCar::getUsereId, id));

        return deptUserCars;

    }

    /**
     * 用户车辆绑定
     * @param deptUserCars
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean userBindCar(List<DeptUserCar> deptUserCars) {

        int count = 0;
        for (DeptUserCar deptUserCar : deptUserCars) {
            int insert = deptUserCarMapper.insert(deptUserCar);
            count += insert;
        }

        if (deptUserCars.size() != count){
            throw new MyException("绑定用户失败");
        }
        return true;

    }
}