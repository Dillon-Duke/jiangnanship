package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.DeptConfig;
import com.caidao.entity.DeptRoleConfig;
import com.caidao.entity.DeptUser;
import com.caidao.entity.DeptUserRole;
import com.caidao.mapper.DeptConfigMapper;
import com.caidao.mapper.DeptRoleConfigMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.DeptUserRoleMapper;
import com.caidao.service.DeptConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dillon
 * @since 2020-05-27
 */
@Service
public class DeptConfigServiceImpl extends ServiceImpl<DeptConfigMapper, DeptConfig> implements DeptConfigService {

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptConfigMapper deptConfigMapper;

    @Autowired
    private DeptUserRoleMapper deptUserRoleMapper;

    @Autowired
    private DeptRoleConfigMapper deptRoleConfigMapper;

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptConfig
     * @return
     */
    @Override
    public IPage<DeptConfig> findPage(Page<DeptConfig> page, DeptConfig deptConfig) {
        IPage<DeptConfig> configIPage = deptConfigMapper.selectPage(page, new LambdaQueryWrapper<DeptConfig>()
                .eq(StringUtils.hasText(deptConfig.getParamKey()), DeptConfig::getParamKey, deptConfig.getParamKey()));
        return configIPage;
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @Override
    public List<DeptConfig> getListDept() {
        List<DeptConfig> deptConfigs = deptConfigMapper.selectList(new LambdaQueryWrapper<DeptConfig>(null));
        return deptConfigs;
    }

    /**
     * 获取用户的所有权限
     * @param userId
     * @return
     */
    @Override
    public List<String> getPowerByUserId(Integer userId) {

        //通过用户id查询对应的角色id
        List<Object> roleList = deptUserRoleMapper.selectObjs(new LambdaQueryWrapper<DeptUserRole>()
                                                            .select(DeptUserRole::getRoleId)
                                                            .eq(DeptUserRole::getUserId, userId));
        if (roleList == null || roleList.isEmpty()){
            return null;
        }

        //查询用户属于哪个部门
        DeptUser deptUser = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                                                .select(DeptUser::getUserDeptId)
                                                .eq(DeptUser::getUserId, userId));
        //获取对应的权限列表
        List<Object> roleConfigs = deptRoleConfigMapper.selectObjs(new LambdaQueryWrapper<DeptRoleConfig>()
                                                .select(DeptRoleConfig::getConfigId)
                                                .eq(DeptRoleConfig::getDeptId, deptUser)
                                                .or(false)
                                                .in(DeptRoleConfig::getRoleId, roleList));

        List<String> result = new ArrayList<String>();
        for (Object object : roleConfigs) {
            String authorities = String.valueOf(object);
            String[] split = authorities.split(",");
            for (String string : split) {
                result.add(string);
            }
        }
        return result;
    }

    /**
     * 新增字典值
     * @param deptConfig
     * @return
     */
    @Override
    public boolean save(DeptConfig deptConfig) {

        deptConfig.setCreateDate(LocalDateTime.now());
        deptConfig.setState(1);

        return super.save(deptConfig);
    }
}
