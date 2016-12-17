package com.example.markwen.easycourse.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by noahrinehart on 11/12/16.
 */

public class DateUtils {

    public static boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        int i = cal1.get(Calendar.DATE);
        int j = cal2.get(Calendar.DATE);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static long timeDifferenceInMinutes(Date date1, Date date2) {
        return TimeUnit.MINUTES.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
    }
}
