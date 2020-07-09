package com.caidao.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tom
 */
public class MapUtils {

    private static final double OBJECT_HALF_LENGTH = 0.5;

    public static Map<String,String> getMap(String... strings){
        Map<String, String> map = new HashMap<>(strings.length);
        map.clear();
        for (int i = 0; i < strings.length * OBJECT_HALF_LENGTH; i++) {
            int number = 2 * i;
            map.put(strings[number],strings[number+1]);
        }
        return map;
    }

    public static Map<String,Object> getMap(Object... objects){
        Map<String, Object> map = new HashMap<>(objects.length);
        map.clear();
        for (int i = 0; i < objects.length * OBJECT_HALF_LENGTH; i++) {
            int number = 2 * i;
            map.put(String.valueOf(objects[number]),objects[number+1]);
        }
        return map;
    }

}
