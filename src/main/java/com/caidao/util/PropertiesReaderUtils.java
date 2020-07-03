package com.caidao.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author tom
 * @since 2020-05-25
 * 作用是加载资源文件里面 activiti.properties 这个文件里面的内容，将内容转换为map形式返回出去
 */
public class PropertiesReaderUtils {

    private static Map<String, String> map = new HashMap();
    public static Map<String, String> getMap(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //处理映射配置信息
        Properties mappingProperties = new Properties();
        InputStream mappingPropertiesInStream = loader.getResourceAsStream("activiti.properties");
        //使用inputstreamreder 解决属性类从数据流中加载中文乱码的问题
        InputStreamReader inputStreamReader = new InputStreamReader(mappingPropertiesInStream);
        try {
            mappingProperties.load(inputStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mappingPropertiesInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Enumeration mappingPropertiesEenumeration = mappingProperties.propertyNames();
        while (mappingPropertiesEenumeration.hasMoreElements()) {
            String key = (String) mappingPropertiesEenumeration.nextElement();
            String value = mappingProperties.getProperty(key);
            map.put(key, value);
        }
        return map;
    }

}
