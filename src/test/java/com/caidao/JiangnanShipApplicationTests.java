package com.caidao;

import com.caidao.mapper.AppMassageMapper;
import com.caidao.mapper.DeptUserCarMapper;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;


@SpringBootTest
class JiangnanShipApplicationTests {

    @Autowired
    private DeptUserCarMapper deptUserCarMapper;

   @Autowired
   private TaskService taskService;

   @Autowired
   private AppMassageMapper appMassageMapper;

   @Autowired
   private StringRedisTemplate stringRedisTemplate;


    @Test
    void contextLoads() {

        System.out.println((int)((Math.random()*9+1)*100000));
    }

    @Test
    void deptTest(){
        Task task = taskService.createTaskQuery()
                .processInstanceBusinessKey("admin:14")
                .singleResult();
        System.out.println(task);
    }

    @Test
    void test1 () {
        System.out.println(stringRedisTemplate.opsForValue().get("appUserPrivateKey:225a37ed-baa7-4c00-a33d-655d43faf1ec"));
    }

}
