package com.ecnu.traceability.Utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Vector
 */
public class DateUtils {

    public static long dataDiff(Date dateStart, Date dateEnd) {
        try {
            //毫秒ms
            long diff = Math.abs(dateStart.getTime() - dateEnd.getTime());

            long diffSeconds = diff / 1000 % 60;
//            long diffMinutes = diff / (60 * 1000) % 60;
//            long diffHours = diff / (60 * 60 * 1000) % 24;
//            long diffDays = diff / (24 * 60 * 60 * 1000);
//
//            System.out.print("两个时间相差：");
//            System.out.print(diffDays + " 天, ");
//            System.out.print(diffHours + " 小时, ");
//            System.out.print(diffMinutes + " 分钟, ");
//            System.out.print(diffSeconds + " 秒.");
            return diffSeconds;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;

        }
    }

    public static int yearDateDiff(String startDate, String endDate) {
        Calendar calBegin = Calendar.getInstance(); //获取日历实例
        Calendar calEnd = Calendar.getInstance();
        calBegin.setTime(stringTodate(startDate, "yyyy")); //字符串按照指定格式转化为日期
        calEnd.setTime(stringTodate(endDate, "yyyy"));
        return calEnd.get(Calendar.YEAR) - calBegin.get(Calendar.YEAR);
    }

    public static long dayDiff(String startTime, String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long day = 0;
        try {
            Date state = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            long stateTimeLong = state.getTime();
            long endTimeLong = end.getTime();
            // 结束时间-开始时间 = 天数
            day = (endTimeLong - stateTimeLong) / (24 * 60 * 60 * 1000);
            System.out.println(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

    public static Date stringTodate(String dateStr, String formatStr) {
        // 如果时间为空则默认当前时间
        Date date = null;
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        if (dateStr != null && !dateStr.equals("")) {
            String time = "";
            try {
                Date dateTwo = format.parse(dateStr);
                time = format.format(dateTwo);
                date = format.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            String timeTwo = format.format(new Date());
            try {
                date = format.parse(timeTwo);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
}