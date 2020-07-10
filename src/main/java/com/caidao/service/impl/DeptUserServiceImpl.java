package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.config.AppUserRealmConfig;
import com.caidao.exception.MyException;
import com.caidao.mapper.*;
import com.caidao.param.UserParam;
import com.caidao.pojo.*;
import com.caidao.service.DeptUserService;
import com.caidao.util.*;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Service
@Slf4j
public class DeptUserServiceImpl extends ServiceImpl<DeptUserMapper, DeptUser> implements DeptUserService {

    /** 轮播图最大的返回数量 */
    private static final int MAX_IMAGE_COUNT = 3;

    /** 轮播图最大的返回数量 */
    private static final Integer APP_USED_COUNT = 4;

    @Autowired
    private AppTasksMassageMapper appTasksMassageMapper;

    @Autowired
    private AppOperateMapper appOperateMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptUserCarApplyMapper deptUserCarApplyMapper;

    @Autowired
    private CustomActivitiMapper customActivitiMapper;

    @Autowired
    private Jedis jedis;

    @Autowired
    private LunchImageMapper lunchImageMapper;

    @Autowired
    private DeptUserRoleMapper deptUserRoleMapper;

    @Autowired
    private DeptConfigMapper deptConfigMapper;

    @Autowired
    private AppUserCommonMsgMapper appUserCommonMsgMapper;

    /**
     * 获取部门用户的分页数据
     * @param page
     * @param deptUser
     * @return
     */
    @Override
    public IPage<DeptUser> getDeptUserPage(Page<DeptUser> page, DeptUser deptUser) {
        Assert.notNull(page != null,"分页数据不能为空");
        log.info("查询部门分页数据，当前页{}，页大小{}",page.getCurrent(),page.getSize());
        IPage<DeptUser> selectPage = deptUserMapper.selectPage(page, new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getState,1)
                .orderByDesc(DeptUser::getCreateDate)
                .like(StringUtils.hasText(deptUser.getUsername()), DeptUser::getUsername, deptUser.getUsername()));
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
                .eq(DeptUser::getState,1)
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
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptUser.setCreateId(sysUser.getUserId());
        Assert.notNull(deptUser,"新增用户信息不能为空");
        log.info("新增用户名为{}的用户",deptUser.getUsername());
        //查询数据库中是否有该用户名，如果有，则提示更换用户名
        DeptUser user = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUsername, deptUser.getUsername()));
        if (user != null){
            throw new MyException("该名称已被注册，请更换其他名称");
        }
        String salt = UUID.randomUUID().toString().replaceAll("-","");
        //生成盐值
        deptUser.setUserSalt(salt);
        deptUser.setState(1);
        //设置加盐密码
        String password = deptUser.getPassword();
        String saltPass = Md5Utils.getHashAndSaltAndTime(password, salt, 1024);
        deptUser.setPassword(saltPass);
        deptUser.setCreateDate(LocalDateTime.now());
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
        //设置工号
        String userDeptName = deptUser.getUserDeptName();
        String firstUpperCase = getFirstUpperCase(userDeptName);
        deptUser.setJobNum(firstUpperCase + DateUtils.getYyyyMm() + (int)((Math.random()+1)*1000));
        //批量获得插入的数据id
        deptUserMapper.insert(deptUser);
        Integer userId = deptUser.getUserId();
        List<DeptUserRole> deptUserRoles = roleIdList.stream().map((x) -> EntityUtils.getDeptUserRole(userId,x)).collect(Collectors.toList());
        Boolean result = deptUserRoleMapper.insertBatches(deptUserRoles);
        //判断是否插入成功
        if (result && userId == 0){
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
        Assert.notNull(id,"部门用户id不能为空");
        log.info("查询用户id为{}的用户",id);
        //获取用户角色中间表信息
        List<DeptUserRole> roleList = deptUserRoleMapper.selectList(new LambdaQueryWrapper<DeptUserRole>()
                .eq(DeptUserRole::getUserId, id));
        //将角色id 放到list列表里面去
        List<Integer> arrayList = new ArrayList<>(0);
        if ((roleList != null) && (!roleList.isEmpty())){
            arrayList = roleList.stream().map((x) -> x.getRoleId()).collect(Collectors.toList());
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
        Assert.notNull(deptUser,"更新部门用户信息不能为空");
        log.info("更新用户id为{}的用户",deptUser.getUserId());
        SysUser sysUser = (SysUser)SecurityUtils.getSubject().getPrincipal();
        //设置更新人id
        deptUser.setUpdateId(sysUser.getUserId());
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
            String saltPass = Md5Utils.getHashAndSaltAndTime(password, deptUser.getUserSalt(), 1024);
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
        boolean batches = true;
        if (roleIdList == null || roleIdList.isEmpty()){
            deptUser.setUserRoleName("无");
            deptUser.setUserDeptName("无");
        } else {
            //批量新增用户角色中间表
            List<DeptUserRole> deptUserRoles1 = roleIdList.stream().map((x) -> EntityUtils.getDeptUserRole(deptUser.getUserId(),x)).collect(Collectors.toList());
            batches = deptUserRoleMapper.insertBatches(deptUserRoles1);
            //从其他三张表中查询对应的角色部门信息，填到用户字段里面
            for (Integer integer : roleIdList) {
                Map<String, Object> map = deptUserMapper.selectDeptRole(integer);
                deptUser.setUserRoleId(integer);
                deptUser.setUserRoleName(map.get("role_name").toString());
                deptUser.setUserDeptId(Integer.parseInt(map.get("dept_id").toString()));
                deptUser.setUserDeptName(map.get("dept_name").toString());
            }
        }
        //获取被删除用户的token
        Object token = jedis.hget(PropertyUtils.ALL_USER_TOKEN, user.getUserSalt());
        //判断该用户目前是否登录 登录 则删除对应session 没有登录 则不需要操作
        if (token != null) {
            //删除用户对应的session个人信息 删除用户储存的密钥
            jedis.del(PropertyUtils.USER_SESSION+token,PropertyUtils.MD5_PREFIX + token);
            //删除用户的缓存
            new AppUserRealmConfig().getAppClearAllCache(token);

        }
        return batches;
    }

    /**
     * 批量删除用户
     * 真删除
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class, propagation = Propagation.REQUIRES_NEW)
    public boolean removeByIds(Collection<? extends Serializable> ids) {
        Assert.notNull(ids,"删除id不能为空");
        log.info("删除id为{}的用户",ids);
        //删除用户角色中间表
        deptUserRoleMapper.deleteBatchIds(ids);
        //假删除用户
        Integer result = deptUserMapper.updateBatchesState(ids);
        if (result == 0){
            return false;
        }
        return true;
    }

    /**
     * 获取有空余时间的司机
     * @return
     */
    @Override
    public Map<String, Object> getFreeDrivers() {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUser,"用户登录超时，请重新登录");
        //查询用户车辆表当天所有有任务的人
        List<DeptUserCarApply> deptUserCarApplyList = deptUserCarApplyMapper.selectList(null);
        List<Integer> integers = new ArrayList<>();
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplyList) {
            Integer driverId = deptUserCarApply.getDriverId();
            Integer operatorId = deptUserCarApply.getOperatorId();
            if (!integers.contains(driverId)) {
                integers.add(driverId);
            }
            if (!integers.contains(operatorId)) {
                integers.add(operatorId);
            }
        }
        //TODO 获取所有的空闲人员 后来可能会需要加条件，比如说部门条件
        List<DeptUser> freeDriver = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .notIn(DeptUser::getUserId, integers));
        //TODO 获取所有的不空闲人员 后来可能会需要加条件，比如说部门条件
        List<DeptUser> taskDriver = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .in(DeptUser::getUserId, integers));
        return MapUtils.getMap("freeDriver",freeDriver,"taskDriver",taskDriver);
    }

    /**
     * 获取司机的任务
     * @param id
     * @return
     */
    @Override
    public List<DeptUserCarApply> getFreeDriverById(Integer id) {
        Assert.notNull(id,"部门用户Id不能为空");
        log.info("查询部门用户id为{}的空闲时间",id);
        //获得该司机的所有任务 包括司机和操作员的
        List<DeptUserCarApply> deptUserCarApplies = deptUserCarApplyMapper.selectList(new LambdaQueryWrapper<DeptUserCarApply>()
                .eq(DeptUserCarApply::getDriverId, id)
                .or(true)
                .eq(DeptUserCarApply::getOperatorId,id));
        //获得对应的车辆ID 去重
        Set<Integer> integers = deptUserCarApplies.stream().map((x) -> x.getCarId()).collect(Collectors.toSet());
        //TODO 获得对应的车辆任务 之后逻辑确定是否是获取对应的申请任务，再写逻辑
        return deptUserCarApplies;
    }

    /**
     * 用户车辆绑定
     * @param deptUserCarApplies
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean userBindCar(List<DeptUserCarApply> deptUserCarApplies) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUserCarApplies,"绑定数据不能为空");
        log.info("用户绑定车辆", deptUserCarApplies);
        List<DeptUserCarApply> userCars = new ArrayList<>(deptUserCarApplies.size());
        List<String> username = new ArrayList<>(deptUserCarApplies.size() + 1);
        List<AppTasksMassage> massages = new ArrayList<>(username.size());
        Integer taskId = null;
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
            //自定义工号
            deptUserCarApply.setWorkNum(PropertyUtils.USER_BIND_CAR_TASK_PREFIX + DateUtils.getYyyyMm() + deptUserCarApply.getCarPlant());
            userCars.add(deptUserCarApply);
            //将姓名收集到姓名列表中
            if (!username.contains(deptUserCarApply.getDriverName())) {
                username.add(deptUserCarApply.getDriverName());
            }
            username.add(deptUserCarApply.getOperatorName());
            taskId = deptUserCarApply.getTaskId();
        }
        deptUserCarApplyMapper.insertBatches(userCars);
        for (String name : username) {
            massages.add(EntityUtils.getAppMassage(name, taskId,null, "平板车操作任务",null));
        }
        Integer integer = appTasksMassageMapper.insertBatches(massages);
        if (integer == 0) {
            return false;
        }
        return true;
    }

    /**
     * 根据用户名和部门模糊查找
     * @param deptUser
     * @return
     */
    @Override
    public List<DeptUser> getUsersByNameLikeAndDeptLike(DeptUser deptUser) {
        Assert.notNull(deptUser,"部门用户不能为空");
        log.info("手机端模糊插叙部门用户");
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getState,1)
                .like(StringUtils.hasText(deptUser.getUsername()), DeptUser::getUsername, deptUser.getUsername())
                .like(StringUtils.hasText(deptUser.getUserDeptName()), DeptUser::getUserDeptName, deptUser.getUserDeptName()));
        return userList;
    }

    /**
     * 根据部门和角色查询用户
     * @param deptUser
     * @return
     */
    @Override
    public List<DeptUser> getUsersByRoleAndDept(DeptUser deptUser) {
        Assert.notNull(deptUser,"部门用户不能为空");
        log.info("手机端根据部门和角色查询用户插叙部门用户");
        List<DeptUser> userList = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getState,1)
                .eq(DeptUser::getUserDeptName,deptUser.getUserDeptName())
                .eq(DeptUser::getUserRoleName,deptUser.getUserRoleName()));
        return userList;
    }

    /**
     * 用户车辆解绑
     * @param ids 主键Id
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean userNnBindCar(List<Integer> ids) {
        Assert.notNull(ids,"主键Id不能为空");
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        if (deptUser == null) {
            throw new MyException("用户登录超时，请重新登录");
        }
        log.info("用户{}解绑了用户车辆绑定id为{}的绑定",deptUser.getUsername(),ids);
        //获取需要解绑的信息
        List<DeptUserCarApply> deptUserCarApplies = deptUserCarApplyMapper.selectBatchIds(ids);
        //获取对应的业务Id以及司机和操作员
        Integer businessKey = null;
        List<String> username = new LinkedList<>();
        for (DeptUserCarApply deptUserCarApply : deptUserCarApplies) {
            businessKey = deptUserCarApply.getBusinessKey();
            if (!username.contains(deptUserCarApply.getDriverName())) {
                username.add(deptUserCarApply.getDriverName());
            }
            username.add(deptUserCarApply.getOperatorName());
        }
        //通过业务主键获取对应的任务Id
        String taskId = customActivitiMapper.selectTaskIdWithBusinessKey(businessKey);
        List<AppTasksMassage> appTasksMassages = appTasksMassageMapper.selectList(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getTaskId, taskId)
                .in(AppTasksMassage::getUsername, username));
        //获取对应的id
        List<AppTasksMassage> list = new LinkedList<>();
        for (AppTasksMassage appTasksMassage : appTasksMassages) {
            list.add(EntityUtils.getAppMassage(appTasksMassage.getMassageName(),Integer.parseInt(taskId), appTasksMassage.getUserId(), appTasksMassage.getUsername(),null));
        }
        return appTasksMassageMapper.updateBatches(list);
    }

    /**
     * 用户登录
     * @param userParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Map<String, String> login(UserParam userParam) {
        org.apache.shiro.util.Assert.notNull(userParam.getPrincipal(),"用户名不能为空");
        log.info("用户名为{}请求登录",userParam.getPrincipal());
        try {
            Subject subject = SecurityUtils.getSubject();
            UserLoginTokenUtils userLoginTokenUtils = new UserLoginTokenUtils(userParam.getPrincipal(), userParam.getCredentials(),PropertyUtils.APP_USER_REALM);
            //校验登录信息
            subject.login(userLoginTokenUtils);
            String token = subject.getSession().getId().toString();
            DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
            String userSalt = deptUser.getUserSalt();
            //将密钥放到redis中
            jedis.set(PropertyUtils.MD5_PREFIX + token,userSalt);
            //将之前的公钥密钥删除
            jedis.del(PropertyUtils.APP_USER_PRIVATE_KEY + userParam.getSessionUuid(),PropertyUtils.APP_USER_PUBLIC_KEY + userParam.getSessionUuid());
            //将个人信息放在hashSet中，后来修改密码的时候抹掉个人的缓存信息以及session信息;
            jedis.hset(PropertyUtils.ALL_USER_TOKEN,userSalt,token);
            return MapUtils.getMap("salt",userSalt,"token",token);
        } catch (Exception e){
            throw new MyException("账号或者密码错误，登录失败");
        }
    }

    /**
     * 用户登出
     */
    @Override
    public void logout() {
        //删除用户在redis 里面的token
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        jedis.hdel(PropertyUtils.ALL_USER_TOKEN,deptUser.getUserSalt());
        //获取对应的登录用户session
        String token = SecurityUtils.getSubject().getSession().getId().toString();
        //删除用户在redis 里面的token
        jedis.del(PropertyUtils.USER_SESSION + token,PropertyUtils.MD5_PREFIX + token);
        //清空缓存中的信息
        new AppUserRealmConfig().getAppClearAllCache(token);
    }

    /**
     * 向用户发送验证码
     * @param phone
     */
    @Override
    public void sendCheckCode(String phone) {
       org.apache.shiro.util.Assert.notNull(phone,"手机号不能为空");
        //TODO 是否要使用短信业务 使用哪个短信业务系统 ，或者是只能是后台工作人员改密码
        //生成随机6位数字
        int code = (int)((Math.random()*9+1)*100000);
        //向redis 里面存6位的随机数组 redis储存时间为60S
        jedis.setex(PropertyUtils.MASSAGE_CODE + phone,60,String.valueOf(code));
    }

    /**
     * 更新用户的密码
     * @param userParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateUserPassword(UserParam userParam) {
        Assert.notNull(userParam,"参数不能为空");
        log.info("用户手机号为{}的用户更新了密码",userParam.getPhone());
        //校验验证码是否正确
        String phone = PropertyUtils.MASSAGE_CODE + userParam.getPhone();
        String code = userParam.getImageCode();
        if (!org.apache.shiro.util.StringUtils.hasText(code) || !org.apache.shiro.util.StringUtils.hasText(phone)) {
            throw new MyException("验证码校验失败");
        }
        //获取redis里面的验证码并校验
        String redisImageCode = jedis.get(PropertyUtils.VALCODE_PRIFAX + phone);
        if (!org.apache.shiro.util.StringUtils.hasText(redisImageCode)) {
            throw new MyException("验证码超时，请重新验证");
        }
        if (!code.equals(redisImageCode)) {
            throw new MyException("验证码错误，请重新输入");
        }
        Integer integer = this.updatePassByPhone(userParam);
        if (integer == 0) {
            return false;
        }
        return true;
    }

    /**
     * 根据用户名和手机判断是否有这个人
     * @param userParam
     * @return
     */
    @Override
    public boolean checkNameAndPhone(UserParam userParam) {
        Assert.notNull(userParam.getPrincipal(),"用户名不能为空");
        Assert.notNull(userParam.getPhone(),"手机号不能为空");
        log.info("查询用户名为{}，手机号为{}的用户",userParam.getPrincipal(),userParam.getPhone());
        DeptUser deptUser = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getState,1)
                .eq(DeptUser::getUsername, userParam.getPrincipal())
                .or(false)
                .eq(DeptUser::getPhone, userParam.getPhone()));
        if (deptUser == null){
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getHomePage(Integer userId) {
        //获取用户的权限列表
        List<String> authorities = getPowerByUserId(userId);
        //获得用户首页个人信息列表
        Map<String,String> userMassage = getDeptUserMassage(userId);
        //获取用户的首页信息数量
        List<AppTasksMassage> handledTasks = getUserNotReadMassage(userMassage.get("username"));
        //获取手机轮播图片，默认3张
        List<String> lunchImages = getLunchImages();
        //获取手机的常用操作
        List<AppOperate> usualOperate = getAppOperate(userId);
        //获取通用消息个数
        Integer commonMsgCount = getUnReadUserCommonMsgCount(userId);
        return MapUtils.getMap("authorities", authorities,"userMassage",userMassage,"UnHandleTasks",handledTasks,"lunchImages",lunchImages,"usualOperate",usualOperate,"commonMsgCount",commonMsgCount);
    }

    /**
     * 获得用户通用消息数量
     * @param userId
     * @return
     */
    private Integer getUnReadUserCommonMsgCount(Integer userId) {
        List<AppUserCommonMsg> msgList = appUserCommonMsgMapper.selectList(new LambdaQueryWrapper<AppUserCommonMsg>()
                .eq(AppUserCommonMsg::getUserId, userId)
                .eq(AppUserCommonMsg::getIsRead, 1));
        return msgList.size();
    }

    /**
     * 获得常用的操作信息
     * @param userId
     * @return
     */
    public List<AppOperate> getAppOperate(Integer userId) {
        List<AppOperate> appOperates = appOperateMapper.selectList(new LambdaQueryWrapper<AppOperate>()
                .eq(AppOperate::getUserId, userId)
                .orderByDesc(AppOperate::getTimes));
        if (appOperates.size() > APP_USED_COUNT) {
            appOperates = appOperates.subList(0,APP_USED_COUNT);
        }
        return appOperates;
    }

    /**
     * 获得用户未读信息列表
     * @param username
     * @return
     */
    private List<AppTasksMassage> getUserNotReadMassage(String username) {
        Assert.notNull(username,"用户名不能未空");
        log.info("用户{}查询未读消息列表",username);
        List<AppTasksMassage> massages = appTasksMassageMapper.selectList(new LambdaQueryWrapper<AppTasksMassage>()
                .eq(AppTasksMassage::getUsername, username)
                .eq(AppTasksMassage::getIsRead, 1)
                .orderByDesc(AppTasksMassage::getCreateTime));
        return massages;
    }

    /**
     * 获取用户的所有权限
     * @param userId
     * @return
     */
    private List<String> getPowerByUserId(Integer userId) {
        //todo 等一下跑一下看看有问题没
        //获取权限ID
        List<Integer> list = deptConfigMapper.getUserPowerIdsWithUserId(userId);
        if (list == null || list.isEmpty()){
            return null;
        }
        //获得对应的权限信息
        List<Object> deptConfigs = deptConfigMapper.selectObjs(new LambdaQueryWrapper<DeptAuthorisation>()
                .select(DeptAuthorisation::getParamValue)
                .in(DeptAuthorisation::getConfId, list));
        List<String> result = null;
        for (Object object : deptConfigs) {
            result = Arrays.stream(String.valueOf(object).split(",")).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * 获取手机首页的轮播图片
     * 最多返回3张轮播图片
     * 1  代表轮播图设置为显示状态
     * @return
     */
    private List<String> getLunchImages() {
        List<LunchImage> imageList = lunchImageMapper.selectList(new LambdaQueryWrapper<LunchImage>()
                .eq(LunchImage::getIsUse, 1));
        //将图片按照id进行倒排序
        imageList.sort(Comparator.comparing(LunchImage::getId).reversed());
        //如果设置的图片超过3张，则返回最新设置的三张图片
        if (imageList.size() > MAX_IMAGE_COUNT) {
            imageList = imageList.subList(0,3);
        }
        List<String> list = imageList.stream().map((i) -> i.getSourceImage()).collect(Collectors.toList());
        return list;
    }

    /**
     * 获得用户的app首页个人信息
     * @param userId
     * @return
     */
    private Map<String, String> getDeptUserMassage(Integer userId) {
        DeptUser user = deptUserMapper.selectById(userId);
        String realName = user.getRealName();
        String jobNum = user.getJobNum();
        String sourceImage = user.getSourceImage();
        String username = user.getUsername();
        return MapUtils.getMap("username",username,"realName",realName,"jobNum",jobNum,"sourceImage",sourceImage);
    }

    /**
     * 更新用户的密码
     * @param userParam
     * @return
     */
    private Integer updatePassByPhone(UserParam userParam) {
        DeptUser deptUser = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getPhone, userParam.getPhone()));
        //设置更新盐值
        String password = userParam.getNewCredentials();
        String saltPass = Md5Utils.getHashAndSaltAndTime(password, deptUser.getUserSalt(), 1024);
        deptUser.setPassword(saltPass);
        //更新用户密码
        return deptUserMapper.updateUserPasswordByUserId(deptUser.getUserId(),saltPass);
    }

    /**
     * 将汉语字符转成拼音
     * @param chineseName
     * @return
     */
    private String getFirstUpperCase(String chineseName)  {
        char[] charArray = chineseName.toCharArray();
        StringBuilder pinyin = new StringBuilder();
        HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();
        //设置大小写格式
        outputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        //设置声调格式：
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < charArray.length ; i++) {
            //匹配中文,非中文转换会转换成null
            if (Character.toString(charArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
                try {
                    String[] hanYuPinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(charArray[i],outputFormat);
                    String string =hanYuPinyinStringArray[0];
                    pinyin.append(string, 0, 1);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    throw new MyException("中文转拼音出错，请联系管理员");
                }
            } else {
                pinyin.append(charArray[i]);
            }
        }
        return pinyin.toString();
    }

}