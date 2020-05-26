package com.caidao.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author tom
 * @since 2020-05-25
 */
public class PropertiesReaderUtils {

    private static Map<String, String> map = new HashMap();

    public static Map<String, String> getMap(){

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        //处理映射配置信息
        Properties mappingProperties = new Properties();
        InputStream mappingPropertiesInStream = loader.getResourceAsStream("activiti.properties");
        try {
            mappingProperties.load(mappingPropertiesInStream);
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
