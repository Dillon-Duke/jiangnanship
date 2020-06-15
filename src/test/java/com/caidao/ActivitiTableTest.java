package com.caidao;

import com.caidao.mapper.DeptUserMapper;
import com.caidao.util.PropertiesReaderUtils;
import org.activiti.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ActivitiTableTest {

    @Autowired
    private DeptUserMapper deptUserMapper;

    @Autowired
    private TaskService taskService;

    @Test
    void test0(){
        System.out.println(PropertiesReaderUtils.getMap());
    }

    @Test
    void test1 () {
        String taskId = taskService.createTaskQuery().processInstanceBusinessKey(String.valueOf(1)).singleResult().getId();
        System.out.println(taskId);
    }

    @Test
    void test2 () {
        taskService.complete("2535");
    }

}
