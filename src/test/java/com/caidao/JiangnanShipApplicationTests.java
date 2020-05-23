package com.caidao;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class JiangnanShipApplicationTests {

    @Test
    void contextLoads() {

        System.out.println((int)((Math.random()*9+1)*100000));
    }

    @Test
    void listTest(){
        List<Object> objects = new ArrayList<Object>();

        System.out.println(objects);
    }

    @Test
    void logTest(){

    }

}
