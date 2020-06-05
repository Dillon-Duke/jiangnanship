package com.caidao;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class JiangnanShipApplicationTests {

   @Autowired
   private TaskService taskService;


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

}
