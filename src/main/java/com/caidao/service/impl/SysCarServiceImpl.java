package com.caidao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caidao.entity.SysCar;
import com.caidao.mapper.SysCarMapper;
import com.caidao.service.SysCarService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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

    @Override
    public IPage<SysCar> findSysCarPage(Page<SysCar> page, SysCar sysCar) {
        IPage<SysCar> carIPage = sysCarMapper.selectPage(page, new LambdaQueryWrapper<SysCar>()
                .eq(StringUtils.hasText(sysCar.getCarName()), SysCar::getCarName, sysCar.getCarName()));
        return carIPage;
    }

    /**
     * 复写新增车辆，增加创建日期 ，状态
     * @param sysCar
     * @return
     */
    @Override
    public boolean save(SysCar sysCar) {
        sysCar.setCreateDate(LocalDateTime.now());
        sysCar.setState(1);
        return super.save(sysCar);
    }
}
