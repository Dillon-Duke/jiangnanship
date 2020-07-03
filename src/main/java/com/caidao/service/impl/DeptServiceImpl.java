package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptDeptRoleMapper;
import com.caidao.mapper.DeptMapper;
import com.caidao.mapper.DeptRoleMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.pojo.*;
import com.caidao.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-21
 */
@Service
@Slf4j
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptRoleMapper deptRoleMapper;

    @Autowired
    private DeptDeptRoleMapper deptDeptRoleMapper;

    /**
     * 获取所有的部门信息
     * @return
     */
    @Override
    public List<Dept> selectList() {
        List<Dept> deptList = deptMapper.selectList(null);
        return deptList;
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @Override
    public List<Dept> getListDept() {
        log.info("获取所有的部门列表");
        List<Dept> deptList = deptMapper.selectList(null);
        return deptList;
    }

    /**
     * 根据部门ID获取部门角色列表
     * @param deptId
     * @return
     */
    @Override
    public List<DeptRole> getDeptRoles(Integer deptId) {
        Assert.notNull(deptId,"部门角色id不能为空");
        log.info("通过id为{}的部门查询对应的角色列表",deptId);
        //获取所的角色ID
        List<DeptDeptRole> roles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                .select(DeptDeptRole::getRoleId)
                .eq(DeptDeptRole::getDeptId, deptId));
        //将所有的角色Id放到list中
        List<Integer> list = new ArrayList<>(roles.size());
        for (DeptDeptRole role : roles) {
            list.add(role.getRoleId());
        }
        if (list.size() == 0) {
            return null;
        }
        //查询对应的角色
        List<DeptRole> deptRoles = deptRoleMapper.selectList(new LambdaQueryWrapper<DeptRole>()
                .in(DeptRole::getRoleId, list));
        return deptRoles;
    }

    /**
     * 复写新增部门信息
     * @param dept
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Dept dept) {
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        //如果部门为空，则抛异常
        Assert.notNull(dept,"新增部门信息为空");
        log.info("新增名为{}的部门", dept.getDeptName());
        dept.setCreateId(sysUser.getUserId());
        dept.setCreateDate(LocalDateTime.now());
        dept.setState(1);
        return super.save(dept);
    }

    /**
     * 复写更新，添加更新信息
     * @param dept
     * @return
     */
    @Override
    public boolean updateById(Dept dept) {
        Assert.notNull(dept,"部门不能为空");
        log.info("获取id为{}的不们信息", dept.getDeptId());
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        dept.setUpdateId(sysUser.getUserId());
        dept.setUpdateDate(LocalDateTime.now());
        return super.updateById(dept);
    }

    /**
     * 判断部门是否有子部门，如果有，则删除失败
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean removeById(Serializable id) {
        Assert.notNull(id,"删除部门id为空");
        log.info("删除id为{}的部门",id);
        //判断部门下面是否还有用户，如果有，删除失败
        List<DeptUser> deptUsers = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUserDeptId, id));
        if (deptUsers.size() != 0) {
            throw new MyException("部门还绑有用户，不能删除");
        }
        //查询部门下面所有的角色
        List<DeptDeptRole> deptDeptRoles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                .select(DeptDeptRole::getRoleId)
                .eq(DeptDeptRole::getDeptId, id));
        //删除对应的角色表
        if (deptDeptRoles.size() != 0){
            deptRoleMapper.delete(new LambdaQueryWrapper<DeptRole>().in(DeptRole::getRoleId, deptDeptRoles));
        }
        //删除对应的角色中间表
        int delete1 = deptDeptRoleMapper.delete(new LambdaQueryWrapper<DeptDeptRole>()
                .eq(DeptDeptRole::getDeptId, id));
        if (delete1 == 0){
            throw new MyException("删除部门失败，请重试");
        }
        //判断是否还有子部门，如果有 抛异常处理
        List<Dept> depts = deptMapper.selectList(new LambdaQueryWrapper<Dept>()
                                                            .eq(Dept::getParentId, id));
        if (depts.size() != 0){
            throw new MyException("部门还有子部门，不能删除");
        }
        return super.removeById(id);
    }

    /**
     * 更新前通过id查询对应的部门信息
     * @param id
     * @return
     */
    @Override
    public Dept getById(Serializable id) {
        Assert.notNull(id,"部门id不能为空");
        log.info("获取id为{}的不们信息",id);
        return super.getById(id);
    }
}
