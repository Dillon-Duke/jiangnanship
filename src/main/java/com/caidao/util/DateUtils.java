package com.caidao.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author tom
 * @since 2020-06-05
 */
public class DateUtils {

    /** 字符转转日期 */
    public static Date string2Date(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return date;
    }

}

