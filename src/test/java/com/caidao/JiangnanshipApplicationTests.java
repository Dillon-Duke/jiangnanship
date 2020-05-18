package com.caidao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class JiangnanshipApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void stringTest(){

        List<String> strings1 = new ArrayList<>();
        strings1.add("12");
        strings1.add("122");
        System.out.println(String.valueOf(strings1));

    }

}
