package com.caidao.controller.front.flatCar;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController("/flatcar/plan")
@Slf4j
public class FlatCarPlanController {

    @Autowired
    private RuntimeService runtimeService;


}
