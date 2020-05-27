package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.entity.DeptConfig;
import com.caidao.mapper.DeptConfigMapper;
import com.caidao.service.DeptConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-27
 */
@Service
public class DeptConfigServiceImpl extends ServiceImpl<DeptConfigMapper, DeptConfig> implements DeptConfigService {

    @Autowired
    private DeptConfigMapper deptConfigMapper;

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
