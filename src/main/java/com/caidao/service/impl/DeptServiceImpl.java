package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.pojo.Dept;
import com.caidao.pojo.DeptDeptRole;
import com.caidao.pojo.DeptRole;
import com.caidao.pojo.DeptUser;
import com.caidao.exception.MyException;
import com.caidao.mapper.*;
import com.caidao.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
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
     * 获取部门所有人员
     * @return
     */
    @Override
    public List<Dept> findSysDept() {
        List<Dept> depts = deptMapper.selectList(null);
        return depts;
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @Override
    public List<Dept> getListDept() {
        List<Dept> deptList = deptMapper.selectList(new LambdaQueryWrapper<Dept>(null));
        return deptList;
    }

    /**
     * 复写新增部门信息
     * @param dept
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Dept dept) {
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
        dept.setUpdateDate(LocalDateTime.now());
        return super.updateById(dept);
    }

    /**
     * 判断部门是否有子部门，如果有，则删除失败
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {

        //判断部门下面是否还有用户，如果有，删除失败
        List<DeptUser> deptUsers = deptUserMapper.selectList(new LambdaQueryWrapper<DeptUser>()
                .eq(DeptUser::getUserDeptId, id));
        if ((deptUsers != null ) || (!deptUsers.isEmpty())) {
            throw new MyException("部门还绑有用户，不能删除");
        }

        //查询部门下面所有的角色
        List<DeptDeptRole> deptDeptRoles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                .select(DeptDeptRole::getRoleId)
                .eq(DeptDeptRole::getDeptId, id));

        //删除对应的角色表
        int delete = deptRoleMapper.delete(new LambdaQueryWrapper<DeptRole>().in(DeptRole::getRoleId, deptDeptRoles));
        if (delete == 0){
            throw new MyException("删除部门失败，请重试");
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
}
