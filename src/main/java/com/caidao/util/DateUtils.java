package com.caidao.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author tom
 * @since 2020-05-25
 * 日期转换的格式类
 */
public class DateUtils {

    private DateUtils(){}

    public static String yyyyMMdd(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(date);
        return format;
    }

    public static String transTimeCode(String timeTemp){
        Date date = new Date(Long.valueOf(timeTemp));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        return format;
    }

}
