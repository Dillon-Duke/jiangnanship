package com.caidao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caidao.entity.DeptConfig;
import com.caidao.entity.DeptRoleConfig;
import com.caidao.entity.DeptUser;
import com.caidao.entity.DeptUserRole;
import com.caidao.mapper.DeptConfigMapper;
import com.caidao.mapper.DeptRoleConfigMapper;
import com.caidao.mapper.DeptUserMapper;
import com.caidao.mapper.DeptUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class JiangnanShipApplicationTests {

    @Autowired
    private DeptUserRoleMapper deptUserRoleMapper;

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private DeptRoleConfigMapper deptRoleConfigMapper;

    @Autowired
    private DeptConfigMapper deptConfigMapper;

    @Test
    void contextLoads() {

        System.out.println((int)((Math.random()*9+1)*100000));
    }

    @Test
    void deptTest(){
        //通过用户id查询对应的角色id
        List<Object> deptUserRoles = deptUserRoleMapper.selectObjs(new LambdaQueryWrapper<DeptUserRole>()
                .select(DeptUserRole::getRoleId)
                .eq(DeptUserRole::getUserId, 5));


        //查询用户属于哪个部门
        DeptUser user = deptUserMapper.selectOne(new LambdaQueryWrapper<DeptUser>()
                .select(DeptUser::getUserDeptId)
                .eq(DeptUser::getUserId, 5));
        System.out.println(user.getClass().getTypeName()+ "111111111111111111111");
        //获取对应的权限列表
        List<Object> roleConfigs = deptRoleConfigMapper.selectObjs(new LambdaQueryWrapper<DeptRoleConfig>()
                .select(DeptRoleConfig::getConfigId)
                .eq(DeptRoleConfig::getDeptId, user.getUserDeptId())
                .or(false)
                .in(DeptRoleConfig::getRoleId, deptUserRoles));

        List<Object> deptConfigs = deptConfigMapper.selectObjs(new LambdaQueryWrapper<DeptConfig>()
                .select(DeptConfig::getParamValue)
                .in(DeptConfig::getConfId, roleConfigs));

        List<String> result = new ArrayList<String>();
        for (Object object : deptConfigs) {
            String authorities = String.valueOf(object);
            String[] split = authorities.split(",");
            for (String string : split) {
                result.add(string);
            }
        }

        System.out.println(result);

    }

}
