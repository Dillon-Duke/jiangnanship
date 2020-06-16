package com.caidao;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MysqlTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Test
    void test1 () {
        long start = System.currentTimeMillis();
        String businessKey;
        TaskQuery taskQuery = taskService.createTaskQuery();
        TaskEntity taskEntity = (TaskEntity) taskQuery.taskId("50031").singleResult();
        HistoricProcessInstanceQuery instanceQuery = historyService.createHistoricProcessInstanceQuery();
        HistoricProcessInstance processInstance = instanceQuery.processInstanceId(taskEntity.getProcessInstanceId()).singleResult();
        if (processInstance.getSuperProcessInstanceId() != null && processInstance.getBusinessKey() == null) {
            processInstance = instanceQuery.processInstanceId(processInstance.getSuperProcessInstanceId()).singleResult();
            businessKey = processInstance.getBusinessKey();
        } else {
            businessKey = processInstance.getBusinessKey();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start + ">>>>>>>>>>>");
    }
}
