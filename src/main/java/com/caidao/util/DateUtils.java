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
        String yyyyMm = arrivalDate.format(format);
        return yyyyMm;
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
     * 获取两个时间点内的时长,为毫秒
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getTimesLengthBetweenEndTimeAndStartTimeMailSecond(LocalDateTime endTime, LocalDateTime startTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toMillis();
    }

    /**
     * 获取两个时间点内的时长,为分钟
     * @param startTime
     * @param endTime
     * @return
     */
    public static long getTimesLengthBetweenEndTimeAndStartTimeSecond(LocalDateTime endTime, LocalDateTime startTime) {
        Duration duration = Duration.between(startTime, endTime);
        long between = duration.toMinutes();
        return between;
    }

    /**
     * 时间戳转换为localtime
     * @return
     */
    public static LocalDateTime secondTimeStamp2LocalDateTime(Long SecondTimeStamp) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(SecondTimeStamp, 0, ZoneOffset.ofHours(8));
        return localDateTime;
    }

    /**
     * 时间戳转换为localtime
     * @return
     */
    public static LocalDateTime mailSecondTimeStamp2LocalDateTime(Long mailSecondTimeStamp) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(mailSecondTimeStamp/1000, 0, ZoneOffset.ofHours(8));
        return localDateTime;
    }

    public static void main(String[] args) throws InterruptedException {
    }

}

