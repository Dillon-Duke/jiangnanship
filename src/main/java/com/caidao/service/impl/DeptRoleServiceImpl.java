package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.exception.MyException;
import com.caidao.mapper.DeptDeptRoleMapper;
import com.caidao.mapper.DeptRoleConfigMapper;
import com.caidao.mapper.DeptRoleMapper;
import com.caidao.mapper.DeptUserRoleMapper;
import com.caidao.pojo.*;
import com.caidao.service.DeptRoleService;
import com.caidao.util.EntityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Service
@Slf4j
public class DeptRoleServiceImpl extends ServiceImpl<DeptRoleMapper, DeptRole> implements DeptRoleService {

    @Autowired
    private DeptRoleMapper deptRoleMapper;

    @Autowired
    private DeptDeptRoleMapper deptDeptRoleMapper;

    @Autowired
    private DeptUserRoleMapper deptUserRoleMapper;

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
        log.info("获取部门角色当前页{}，页大小{}",page.getCurrent(),page.getSize());
        IPage<DeptRole> selectPage = deptRoleMapper.selectPage(page, new LambdaQueryWrapper<DeptRole>()
                .eq(DeptRole::getState, 1)
                .like(StringUtils.hasText(deptRole.getRoleName()), DeptRole::getRoleName, deptRole.getRoleName()));
        return selectPage;
    }

    /**
     * 获取部门所有的角色
     * @return
     */
    @Override
    public List<DeptRole> getDeptRoleList() {
        log.info("获取部门角色列表");
        List<DeptRole> deptRoles = deptRoleMapper.selectList(new LambdaQueryWrapper<DeptRole>()
        .eq(DeptRole::getState,1));
        return deptRoles;
    }

    /**
     * 新增用户角色列表 新增角色部门中间表
     * @param deptRole
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean save(DeptRole deptRole) {
        Assert.notNull(deptRole,"新增部门角色不能为空");
        log.info("新增角色名为{}的部门角色",deptRole.getRoleName());
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptRole.setCreateId(sysUser.getUserId());
        deptRole.setCreateDate(LocalDateTime.now());
        deptRole.setState(1);
        boolean save = super.save(deptRole);
        //批量新增角色部门的中间表
        List<Integer> deptIdList = deptRole.getDeptIdList();
        List<DeptDeptRole> deptDeptRoles = new ArrayList<>(deptIdList.size());
        if ((deptIdList != null) && (!deptIdList.isEmpty())){
            deptDeptRoles = deptIdList.stream().map((x) -> EntityUtils.getDeptDeptRole(x, deptRole.getRoleId())).collect(Collectors.toList());
        }
        Boolean result = deptDeptRoleMapper.insertBatches(deptDeptRoles);
        //新增角色权限的中间表
        List<Integer> powerIdList = deptRole.getPowerIdList();
        List<DeptRoleAuthorisation> authorisations = new ArrayList<>(powerIdList.size());
        if ((powerIdList != null) && (!powerIdList.isEmpty())){
            authorisations = powerIdList.stream().map((x) -> EntityUtils.getDeptRoleAuthorisation(deptIdList.get(0), deptRole.getRoleId(), x)).collect(Collectors.toList());
        }
        Boolean result1 = deptRoleConfigMapper.insertBatches(authorisations);
        if (result && result1) {
            return save;
        }
        return false;
    }

    /**
     * 编辑前获取被编辑的角色
     * @param id
     * @return
     */
    @Override
    public DeptRole getById(Serializable id) {
        Assert.notNull(id,"角色id{}不能为空");
        log.info("查询角色id为{}的部门角色",id);
        DeptRole deptRole = super.getById(id);
        //从中间表获取部门数据
        List<DeptDeptRole> deptDeptRoles = deptDeptRoleMapper.selectList(new LambdaQueryWrapper<DeptDeptRole>()
                                                                .eq(DeptDeptRole::getRoleId, id));
        //判断中间表是否有数据
        List<Integer> arrayList;
        if ((deptDeptRoles != null) && (!deptDeptRoles.isEmpty())){
            //将部门id放在角色里
            arrayList = deptDeptRoles.stream().map((x) -> x.getDeptId()).collect(Collectors.toList());
            deptRole.setDeptIdList(arrayList);
        }
        //从中间表获取权限数据
        List<DeptRoleAuthorisation> deptRoleAuthorisations = deptRoleConfigMapper.selectList(new LambdaQueryWrapper<DeptRoleAuthorisation>()
                                                                    .eq(DeptRoleAuthorisation::getRoleId, id));
        //判断中间表是否有数据
        List<Integer> arrayList1;
        if ((deptRoleAuthorisations != null) && (!deptRoleAuthorisations.isEmpty())){
            //将权限id放在角色里
            arrayList1 = deptRoleAuthorisations.stream().map((x) -> x.getConfigId()).collect(Collectors.toList());
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
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean updateById(DeptRole deptRole) {
        Assert.notNull(deptRole,"更新角色{}不能为空");
        log.info("更新角色名称为{}的部门角色",deptRole.getRoleName());
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        deptRole.setUpdateId(sysUser.getUserId());
        deptRole.setUpdateDate(LocalDateTime.now());
        //更新角色之前，先删除对应的部门
        deptDeptRoleMapper.delete(new LambdaQueryWrapper<DeptDeptRole>()
                .in(DeptDeptRole::getRoleId, deptRole.getRoleId()));
        //更新角色之前，先删除对应的部门
        deptRoleConfigMapper.delete(new LambdaQueryWrapper<DeptRoleAuthorisation>()
                .in(DeptRoleAuthorisation::getRoleId, deptRole.getRoleId()));
        List<Integer> deptIdList = deptRole.getDeptIdList();
        List<DeptDeptRole> deptDeptRoles = null;
        if ((deptIdList != null) && (!deptIdList.isEmpty())){
            deptDeptRoles = deptIdList.stream().map((x) -> EntityUtils.getDeptDeptRole(x, deptRole.getRoleId())).collect(Collectors.toList());
        }
        Boolean batches = deptDeptRoleMapper.insertBatches(deptDeptRoles);
        //新增角色权限的中间表
        List<Integer> powerIdList = null;
        List<DeptRoleAuthorisation> authorisations = new ArrayList<>(powerIdList.size());
        if ((powerIdList != null) && (!powerIdList.isEmpty())){
            authorisations = powerIdList.stream().map((x) -> EntityUtils.getDeptRoleAuthorisation(deptIdList.get(0), deptRole.getRoleId(), x)).collect(Collectors.toList());
        }
        Boolean insertBatches = deptRoleConfigMapper.insertBatches(authorisations);
        if (batches && insertBatches) {
            return super.updateById(deptRole);
        }
        return false;
    }

    /**
     * 删除角色之前需要删除对应的角色部门关系
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        Assert.notNull(idList,"删除角色{}不能为空");
        log.info("删除角色Id为{}的部门角色",idList);
        //判断如果有用户存在，则不能删除角色
        List<DeptUserRole> userRoles = deptUserRoleMapper.selectList(new LambdaQueryWrapper<DeptUserRole>()
                                        .in(DeptUserRole::getRoleId, idList));
        if ((!userRoles.isEmpty()) || (userRoles != null)){
            throw new MyException("该角色上面有绑定的用户，不能删除");
        }
        //删除角色之前，先删除对应的角色部门中间表
        deptDeptRoleMapper.deleteBatchRoleIds(idList);
        //删除角色之前，先删除对应的权限中间表
        deptRoleConfigMapper.deleteBatchRoleIds(idList);
        boolean result = deptRoleMapper.updateBatchesState(idList);
        return result;
    }

}
