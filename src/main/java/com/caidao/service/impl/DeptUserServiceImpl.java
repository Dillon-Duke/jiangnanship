package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.config.AppUserRealmConfig;
import com.caidao.exception.MyException;
import com.caidao.mapper.AppMassageMapper;
import com.caidao.mapper.DeptUserCarMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.DeptUserRoleMapper;
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

/**
 * @author Dillon
 * @since 2020-05-28
 */
@Service
@Slf4j
public class DeptUserServiceImpl extends ServiceImpl<DeptUserMapper, DeptUser> implements DeptUserService {

    @Autowired
    private AppMassageMapper appMassageMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptUserCarMapper deptUserCarMapper;

    @Autowired
    private Jedis jedis;

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
        Assert.notNull(page != null,"分页数据不能为空");
        log.info("查询部门分页数据，当前页{}，页大小{}",page.getCurrent(),page.getSize());
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
        List<DeptUserRole> deptUserRoles = new ArrayList<>(roleIdList.size());
        for (Integer integer : roleIdList) {
            deptUserRoles.add(EntityUtils.getDeptUserRole(userId,integer));
        }
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
            ArrayList<DeptUserRole> deptUserRoles1 = new ArrayList<>(roleIdList.size());
            for (Integer integer : roleIdList) {
                deptUserRoles1.add(EntityUtils.getDeptUserRole(deptUser.getUserId(),integer));
            }
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
            //删除用户对应的session个人信息 删除用户储存的aes密钥
            jedis.del(PropertyUtils.USER_SESSION+token,PropertyUtils.AES_PREFIX + token);
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
        //判断中间表是否删除
        int count = 0;
        for (Serializable serializable : ids) {
            int delete = deptUserRoleMapper.delete(new LambdaQueryWrapper<DeptUserRole>()
                    .eq(DeptUserRole::getUserId, serializable));
            count+=delete;
        }
        if (count == 0){
            throw new MyException("用户角色删除失败");
        }
        //判断部门用户表是否删除
        Integer batchIds = deptUserMapper.deleteBatchIds(ids);
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
     * @param userParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class, propagation= Propagation.REQUIRES_NEW)
    public boolean updatePassByPhone(UserParam userParam) {

        DeptUser deptUser = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getPhone, userParam.getPhone()));
        //设置更新盐值
        String password = userParam.getNewCredentials();
        String saltPass = Md5Utils.getHashAndSaltAndTime(password, deptUser.getUserSalt(), 1024);
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
    public Map<String, Object> getFreeDrivers() {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUser,"用户登录超时，请重新登录");
        //查询用户车辆表当天所有有任务的人
        List<DeptUserCar> deptUserCarList = deptUserCarMapper.selectList(null);
        List<Integer> integers = new ArrayList<>();
        for (DeptUserCar deptUserCar : deptUserCarList) {
            Integer driverId = deptUserCar.getDriverId();
            Integer operatorId = deptUserCar.getOperatorId();
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
    public List<DeptUserCar> getFreeDriverById(Integer id) {
        Assert.notNull(id,"部门用户Id不能为空");
        log.info("查询部门用户id为{}的空闲时间",id);
        //获得该司机的所有任务 包括司机和操作员的
        List<DeptUserCar> deptUserCars = deptUserCarMapper.selectList(new LambdaQueryWrapper<DeptUserCar>()
                .eq(DeptUserCar::getDriverId, id)
                .or(true)
                .eq(DeptUserCar::getOperatorId,id));
        //获得对应的车辆ID 去重
        List<Integer> integers = new ArrayList<>();
        for (DeptUserCar deptUserCar : deptUserCars) {
            Integer carId = deptUserCar.getCarId();
            if (!integers.contains(carId)) {
                integers.add(carId);
            }
        }
        //TODO 获得对应的车辆任务 之后逻辑确定是否是获取对应的申请任务，再写逻辑
        return deptUserCars;
    }

    /**
     * 用户车辆绑定
     * @param deptUserCars
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Boolean userBindCar(List<DeptUserCar> deptUserCars) {
        DeptUser deptUser = (DeptUser) SecurityUtils.getSubject().getPrincipal();
        Assert.notNull(deptUserCars,"绑定数据不能为空");
        log.info("用户绑定车辆",deptUserCars);
        List<DeptUserCar> userCars = new ArrayList<>(deptUserCars.size());
        List<String> username = new ArrayList<>(deptUserCars.size() + 1);
        List<AppMassage> massages = new ArrayList<>(username.size());
        Integer taskId = null;
        for (DeptUserCar deptUserCar : deptUserCars) {
            //自定义工号
            deptUserCar.setWorkNum(PropertyUtils.USER_BIND_CAR_TASK_PREFIX + DateUtils.getYyyyMm() + deptUserCar.getCarPlant());
            userCars.add(deptUserCar);
            //将姓名收集到姓名列表中
            if (!username.contains(deptUserCar.getDriverName())) {
                username.add(deptUserCar.getDriverName());
            }
            username.add(deptUserCar.getOperatorName());
            taskId = deptUserCar.getTaskId();
        }
        deptUserCarMapper.insertBatches(userCars);
        for (String name : username) {
            massages.add(EntityUtils.getAppMassage(name, taskId, "平板车操作任务"));
        }
        return appMassageMapper.insertBatches(massages);
    }

    /**
     * 获得用户的app首页个人信息
     * @param deptUser
     * @return
     */
    @Override
    public Map<String, String> getDeptUserMassage(DeptUser deptUser) {
        String realName = deptUser.getRealName();
        String jobNum = deptUser.getJobNum();
        String sourceImage = deptUser.getSourceImage();
        return MapUtils.getMap("realName",realName,"jobNum",jobNum,"sourceImage",sourceImage);
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
        List<DeptUserCar> deptUserCars = deptUserCarMapper.selectBatchIds(ids);
        //获取对应的业务Id以及司机和操作员
        Integer businessKey = null;
        List<String> username = new LinkedList<>();
        for (DeptUserCar deptUserCar : deptUserCars) {
            businessKey = deptUserCar.getBusinessKey();
            if (!username.contains(deptUserCar.getDriverName())) {
                username.add(deptUserCar.getDriverName());
            }
            username.add(deptUserCar.getOperatorName());
        }
        //通过业务主键获取对应的任务Id
        String taskId = deptUserCarMapper.selectTaskIdByBusinessKey(businessKey);
        List<AppMassage> appMassages = appMassageMapper.selectList(new LambdaQueryWrapper<AppMassage>()
                .eq(AppMassage::getTaskId, taskId)
                .in(AppMassage::getDeptUsername, username));
        //获取对应的id
        List<AppMassage> list = new LinkedList<>();
        for (AppMassage appMassage : appMassages) {
            list.add(EntityUtils.getAppMassage(appMassage.getMassageName(),Integer.parseInt(taskId),appMassage.getDeptUsername()));
        }
        return appMassageMapper.updateBatches(list);
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