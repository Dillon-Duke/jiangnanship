package com.caidao.mapper;

import com.caidao.entity.DeptUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author Dillon
 * @since 2020-05-28
 */
public interface DeptUserMapper extends BaseMapper<DeptUser> {

    Map<String, Object> selectDeptRole(@PathVariable("integer") Integer integer);
}
