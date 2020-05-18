package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysCar;
import com.caidao.entity.SysRole;
import com.caidao.mapper.SysCarMapper;
import com.caidao.service.SysCarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Dillon
 * @since 2020-05-18
 */
@Service
public class SysCarServiceImpl extends ServiceImpl<SysCarMapper, SysCar> implements SysCarService {

    @Autowired
    private SysCarMapper sysCarMapper;

    public IPage<SysCar> findSysCarPage(Page<SysCar> page, SysCar sysCar) {
        IPage<SysCar> carIPage = sysCarMapper.selectPage(page, new LambdaQueryWrapper<SysCar>()
                .eq(StringUtils.hasText(sysCar.getCarName()), SysCar::getCarName, sysCar.getCarName()));
        return carIPage;
    }

}
