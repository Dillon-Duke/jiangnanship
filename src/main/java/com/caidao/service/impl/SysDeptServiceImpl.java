package com.caidao.service.impl;

import com.caidao.entity.SysDept;
import com.caidao.mapper.SysDeptMapper;
import com.caidao.service.SysDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 获取部门所有人员
     * @return
     */
    @Override
    public List<SysDept> findSysDept() {
        List<SysDept> sysDepts = sysDeptMapper.selectList(null);
        return sysDepts;
    }

    /**
     * 复写新增部门信息
     * @param sysDept
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysDept sysDept) {
        sysDept.setCreateDate(LocalDateTime.now());
        sysDept.setState(1);
        return super.save(sysDept);
    }

    /**
     * 复写更新，添加更新信息
     * @param sysDept
     * @return
     */
    @Override
    public boolean updateById(SysDept sysDept) {
        sysDept.setUpdateDate(LocalDateTime.now());
        return super.updateById(sysDept);
    }
}
