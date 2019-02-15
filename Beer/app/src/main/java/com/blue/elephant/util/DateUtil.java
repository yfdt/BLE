package com.blue.elephant.util;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.DataFormatException;

public class DateUtil {

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static DateFormat dfday = new SimpleDateFormat("yyyy-MM-dd");
    static DateFormat dfZone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static long getPeriodTime(String start,String end)
    {
        try {
            long startMillins = df.parse(start).getTime();
            long endMilins = df.parse(end).getTime();
            return (endMilins - startMillins);
        }catch(Exception e)
        {
            Log.e("Date","date parser has occur : "+ e.getMessage());
            return 0l;
        }
    }


    public static String getLocalTime(String time,String timeZ)
    {
        String utcTime = time + " "+ timeZ;
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));//时区定义并进行时间获取
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }
        SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }




}
