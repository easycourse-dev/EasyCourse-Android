package com.example.markwen.easycourse.utils;

import android.support.annotation.Nullable;

import com.example.markwen.easycourse.models.main.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by noahrinehart on 11/12/16.
 */

public class DateUtils {

    public static boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static int timeDifferenceInMinutes(Date date1, Date date2) {
        return (int)((date1.getTime()/60000) - (date2.getTime()/60000));
    }

    public static Date getLocalDate(Date utcDate) {
        return new Date(utcDate.getTime() + TimeZone.getDefault().getRawOffset());
    }
    

    @Nullable
    public static String getTimeString(Message message, Message prevMessage) {
        Date messageDate = getLocalDate(message.getCreatedAt());
        TimeZone timeZone = TimeZone.getDefault();

        if (prevMessage == null) {
            //Include date in time
            DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
            df.setTimeZone(timeZone);
            return df.format(messageDate);
        }
        Date prevMessageDate = getLocalDate(prevMessage.getCreatedAt());
        int diffInMinutes = DateUtils.timeDifferenceInMinutes(messageDate, prevMessageDate);


        if (diffInMinutes >= 5) {
            //If today
            if (DateUtils.isToday(messageDate)) {
                //Exclude date in time
                DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                df.setTimeZone(timeZone);
                return df.format(messageDate);

            } else {
                //Include date in time
                DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
                df.setTimeZone(timeZone);
                return df.format(messageDate);
            }

        }
        return null;
    }


}
