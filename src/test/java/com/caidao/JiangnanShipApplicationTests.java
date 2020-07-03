package com.caidao;

import com.caidao.mapper.CarMapper;
import com.caidao.mapper.CarPlatformApplyMapper;
import com.caidao.pojo.Car;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
class JiangnanShipApplicationTests {

    @Autowired
    private CarMapper carMapper;

   @Autowired
   private CarPlatformApplyMapper carPlatformApplyMapper;


    @Test
    void deptTest(){
        //获得所有的车辆信息
        List<Car> carLists = carMapper.selectList(null);
        //获取所有当前时间点有任务的车辆id
        List<Integer> carIds = carPlatformApplyMapper.selectTaskCarList(LocalDateTime.now());

        //去除当前时间有任务的车辆
        List<Car> carList = new ArrayList<>(carLists.size());
        for (Car car : carLists) {
            if (!carIds.contains(car.getCarId())) {
                carList.add(car);
            }
        }
        System.out.println(carList);
    }

}
