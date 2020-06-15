package com.caidao.util;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author tom
 * @since 2020-06-05
 */
public class DateUtils {

    /** 获取当前的时间戳
     * @return
     */
    public static Long getTimeStamp() {
         //获取13位时间戳
        long epochMilli = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        //获取10位时间戳
        long epochSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        return epochMilli;
    }

    /** 获取当前日期的前六位
     * @return*/
    public static String getYyyyMm() {
        LocalDateTime arrivalDate = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMM");
        String yyyyMM = arrivalDate.format(format);
        return yyyyMM;
    }

    /**
     * string转为date格式
     * @param date
     * @return
     */
    public static Date string2Date(String date){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, dtf);
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date1 = Date.from(zdt.toInstant());
        return date1;
    }

    /**
     * Date转换为LocalDateTime
     * @param date
     */
    public static LocalDateTime date2LocalDateTime(Date date){
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
        return localDateTime;
    }

    /**
     * LocalDateTime转换为Date
     * @param localDateTime
     */
    public static Date localDateTime2Date( LocalDateTime localDateTime){
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        return date;
    }

    /**
     * 日期转为Boolean值
     * @param date
     * @return
     */
    public static Boolean booleanDate(Date date){
        if (date == null){
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(string2Date(null));
    }

}

