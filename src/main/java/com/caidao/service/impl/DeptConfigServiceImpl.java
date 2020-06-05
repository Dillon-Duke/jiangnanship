package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.DeptConfig;
import com.caidao.entity.DeptRoleConfig;
import com.caidao.mapper.DeptConfigMapper;
import com.caidao.mapper.DeptRoleConfigMapper;
import com.caidao.service.DeptConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class DeptConfigServiceImpl extends ServiceImpl<DeptConfigMapper, DeptConfig> implements DeptConfigService {

    @Autowired
    private DeptConfigMapper deptConfigMapper;

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
        IPage<DeptConfig> selectPage = deptConfigMapper.selectPage(page, new LambdaQueryWrapper<DeptConfig>()
                .eq(StringUtils.hasText(deptConfig.getParamKey()), DeptConfig::getParamKey, deptConfig.getParamKey()));
        return selectPage;
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

        //获取权限ID
        List<Integer> list = deptConfigMapper.getpowerids(userId);
        if (list == null || list.isEmpty()){
            return null;
        }

        //获得对应的权限信息
        List<Object> deptConfigs = deptConfigMapper.selectObjs(new LambdaQueryWrapper<DeptConfig>()
                .select(DeptConfig::getParamValue)
                .in(DeptConfig::getConfId, list));

        List<String> result = new ArrayList<String>();
        for (Object object : deptConfigs) {
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

    /**
     * 删除i权限的时候需要一起删除角色权限中间表
     * @param idList
     * @return
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {

        //删除权限之前需要删除查询是否有角色在使用该权限，如果有，则删除失败
        List<DeptRoleConfig> deptRoleConfigs = deptRoleConfigMapper.selectList(new LambdaQueryWrapper<DeptRoleConfig>()
                .in(DeptRoleConfig::getConfigId, idList));
        if (deptRoleConfigs.size() != 0 || (!deptRoleConfigs.isEmpty())){
            throw new RuntimeException("还有角色在使用该权限，删除失败");
        }
        return super.removeByIds(idList);
    }
}
