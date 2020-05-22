package com.caidao.service.impl;

import com.caidao.entity.SysDept;
import com.caidao.mapper.SysDeptMapper;
import com.caidao.service.SysDeptService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
