package com.caidao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caidao.entity.SysUser;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jinpeng
 * @since 2020-03-25
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 批量删除用户 使用mapper.xml 里面的语句
     * @param ids
     * @return 返回的是成功删除多少条数据
     */
    Integer batchDelete(List<Integer> ids);

}
