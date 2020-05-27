package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.Dept;
import com.caidao.entity.DeptDeptRole;
import com.caidao.mapper.DeptDeptRoleMapper;
import com.caidao.mapper.DeptMapper;
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

        //判断是否还有角色关联着部门，如果有，删除失败
        List<DeptDeptRole> deptDeptRoles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                .eq(DeptDeptRole::getDeptId, id));
        if ((deptDeptRoles != null ) || (!deptDeptRoles.isEmpty())) {
            throw new RuntimeException("部门还绑有角色，不能删除");
        }

        //判断是否还有子部门，如果有 抛异常处理
        List<Dept> depts = deptMapper.selectList(new LambdaQueryWrapper<Dept>()
                                                            .eq(Dept::getParentId, id));
        if (depts.size() != 0){
            throw new RuntimeException("部门还有子部门，不能删除");
        }
        return super.removeById(id);
    }
}
