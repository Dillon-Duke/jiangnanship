package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.caidao.pojo.DeptAuthorition;
import com.caidao.pojo.DeptRoleAuthorition;
import com.caidao.exception.MyException;
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
public class DeptConfigServiceImpl extends ServiceImpl<DeptConfigMapper, DeptAuthorition> implements DeptConfigService {

    @Autowired
    private DeptConfigMapper deptConfigMapper;

    @Autowired
    private DeptRoleConfigMapper deptRoleConfigMapper;

    /**
     * 获取页面的分页数据
     * @param page
     * @param deptAuthorition
     * @return
     */
    @Override
    public IPage<DeptAuthorition> findPage(Page<DeptAuthorition> page, DeptAuthorition deptAuthorition) {
        IPage<DeptAuthorition> selectPage = deptConfigMapper.selectPage(page, new LambdaQueryWrapper<DeptAuthorition>()
                .eq(StringUtils.hasText(deptAuthorition.getParamKey()), DeptAuthorition::getParamKey, deptAuthorition.getParamKey()));
        return selectPage;
    }

    /**
     * 查询所有的部门列表
     * @return
     */
    @Override
    public List<DeptAuthorition> getListDept() {
        List<DeptAuthorition> deptAuthoritions = deptConfigMapper.selectList(new LambdaQueryWrapper<DeptAuthorition>(null));
        return deptAuthoritions;
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
        List<Object> deptConfigs = deptConfigMapper.selectObjs(new LambdaQueryWrapper<DeptAuthorition>()
                .select(DeptAuthorition::getParamValue)
                .in(DeptAuthorition::getConfId, list));

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
     * @param deptAuthorition
     * @return
     */
    @Override
    public boolean save(DeptAuthorition deptAuthorition) {

        deptAuthorition.setCreateDate(LocalDateTime.now());
        deptAuthorition.setState(1);

        return super.save(deptAuthorition);
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
        List<DeptRoleAuthorition> deptRoleAuthoritions = deptRoleConfigMapper.selectList(new LambdaQueryWrapper<DeptRoleAuthorition>()
                .in(DeptRoleAuthorition::getConfigId, idList));
        if (deptRoleAuthoritions.size() != 0 || (!deptRoleAuthoritions.isEmpty())){
            throw new MyException("还有角色在使用该权限，删除失败");
        }
        return super.removeByIds(idList);
    }
}
