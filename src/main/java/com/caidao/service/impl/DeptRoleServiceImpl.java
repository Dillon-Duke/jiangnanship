package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.DeptDeptRole;
import com.caidao.entity.DeptRole;
import com.caidao.entity.DeptRoleConfig;
import com.caidao.mapper.DeptDeptRoleMapper;
import com.caidao.mapper.DeptRoleConfigMapper;
import com.caidao.mapper.DeptRoleMapper;
import com.caidao.service.DeptRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Service
public class DeptRoleServiceImpl extends ServiceImpl<DeptRoleMapper, DeptRole> implements DeptRoleService {

    @Autowired
    private DeptRoleMapper deptRoleMapper;

    @Autowired
    private DeptDeptRoleMapper deptDeptRoleMapper;

    @Autowired
    private DeptRoleConfigMapper deptRoleConfigMapper;

    /**
     * 获得部门角色的分页数据
     * @param page
     * @param deptRole
     * @return
     */
    @Override
    public IPage<DeptRole> getDeptRolePage(Page<DeptRole> page , DeptRole deptRole) {
        IPage<DeptRole> roleIPage = deptRoleMapper.selectPage(page, new LambdaQueryWrapper<DeptRole>()
                .eq(StringUtils.hasText(deptRole.getRoleName()), DeptRole::getRoleName, deptRole.getRoleName()));
        return roleIPage;
    }

    /**
     * 新增用户角色列表 新增角色部门中间表
     * @param deptRole
     * @return
     */
    @Override
    public boolean save(DeptRole deptRole) {
        deptRole.setCreateDate(LocalDateTime.now());
        deptRole.setState(1);

        boolean save = super.save(deptRole);

        //新增角色部门的中间表
        List<Integer> deptIdList = deptRole.getDeptIdList();
        if ((deptIdList != null) && (!deptIdList.isEmpty())){
            for (Integer integer : deptIdList) {
                DeptDeptRole role = new DeptDeptRole();
                role.setDeptId(integer);
                role.setRoleId(deptRole.getRoleId());
                deptDeptRoleMapper.insert(role);
            }
        }

        //新增角色权限的中间表
        List<Integer> powerIdList = deptRole.getPowerIdList();
        if ((powerIdList != null) && (!powerIdList.isEmpty())){
            for (Integer integer : powerIdList) {
                DeptRoleConfig config = new DeptRoleConfig();
                config.setDeptId(deptIdList.get(0));
                config.setRoleId(deptRole.getRoleId());
                config.setConfigId(integer);
                deptRoleConfigMapper.insert(config);
            }
        }
        return save;
    }

    /**
     * 编辑前获取被编辑的角色
     * @param id
     * @return
     */
    @Override
    public DeptRole getById(Serializable id) {

        DeptRole deptRole = super.getById(id);

        //从中间表获取部门数据
        List<DeptDeptRole> deptDeptRoles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                                                                .eq(DeptDeptRole::getRoleId, id));

        //判断中间表是否有数据
        List<Integer> arrayList = new ArrayList<>();
        if ((deptDeptRoles != null) && (!deptDeptRoles.isEmpty())){
            //将部门id放在角色里
            for (DeptDeptRole deptDeptRole : deptDeptRoles) {
                arrayList.add(deptDeptRole.getDeptId());
            }
            deptRole.setDeptIdList(arrayList);
        }

        //从中间表获取权限数据
        List<DeptRoleConfig> deptRoleConfigs = deptRoleConfigMapper.selectList(new LambdaQueryWrapper<DeptRoleConfig>()
                                                                    .eq(DeptRoleConfig::getRoleId, id));

        //判断中间表是否有数据
        ArrayList<Integer> arrayList1 = new ArrayList<Integer>();
        if ((deptRoleConfigs != null) && (!deptRoleConfigs.isEmpty())){
            //将权限id放在角色里
            for (DeptRoleConfig config : deptRoleConfigs) {
                arrayList1.add(config.getConfigId());
            }
            deptRole.setPowerIdList(arrayList1);
        }
        return deptRole;
    }

    /**
     * 更新部门角色
     * @param deptRole
     * @return
     */
    @Override
    public boolean updateById(DeptRole deptRole) {

        deptRole.setUpdateDate(LocalDateTime.now());

        //更新角色之前，先删除对应的部门
        deptDeptRoleMapper.delete(new LambdaQueryWrapper<DeptDeptRole>()
                .in(DeptDeptRole::getRoleId, deptRole.getRoleId()));

        //更新角色之前，先删除对应的部门
        deptRoleConfigMapper.delete(new LambdaQueryWrapper<DeptRoleConfig>()
                .in(DeptRoleConfig::getRoleId, deptRole.getRoleId()));

        List<Integer> deptIdList = deptRole.getDeptIdList();
        if ((deptIdList != null) && (!deptIdList.isEmpty())){
            for (Integer integer : deptIdList) {
                DeptDeptRole role = new DeptDeptRole();
                role.setDeptId(integer);
                role.setRoleId(deptRole.getRoleId());
                deptDeptRoleMapper.insert(role);
            }
        }

        //新增角色权限的中间表
        List<Integer> powerIdList = deptRole.getPowerIdList();
        if ((powerIdList != null) && (!powerIdList.isEmpty())){
            for (Integer integer : powerIdList) {
                DeptRoleConfig config = new DeptRoleConfig();
                config.setDeptId(deptIdList.get(0));
                config.setRoleId(deptRole.getRoleId());
                config.setConfigId(integer);
                deptRoleConfigMapper.insert(config);
            }
        }

        return super.updateById(deptRole);
    }

    /**
     * 删除角色之前需要删除对应的角色部门关系
     * @param idList
     * @return
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {

        for (Serializable serializable : idList) {
            //更新角色之前，先删除对应的部门
            deptDeptRoleMapper.delete(new LambdaQueryWrapper<DeptDeptRole>()
                    .in(DeptDeptRole::getRoleId, serializable));
            //更新角色之前，先删除对应的部门
            deptRoleConfigMapper.delete(new LambdaQueryWrapper<DeptRoleConfig>()
                    .in(DeptRoleConfig::getRoleId, serializable));
        }
        return super.removeByIds(idList);
    }
}
