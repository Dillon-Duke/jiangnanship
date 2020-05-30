package com.caidao;

import com.caidao.util.PropertiesReaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ActivitiTableTest {

    @Test
    void mapTest(){
        System.out.println(PropertiesReaderUtils.getMap());
    }


}
