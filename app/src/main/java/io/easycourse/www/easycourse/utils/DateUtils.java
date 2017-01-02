package io.easycourse.www.easycourse.utils;

import android.support.annotation.Nullable;

import io.easycourse.www.easycourse.models.main.Message;

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
        int i = cal1.get(Calendar.DATE);
        int j = cal2.get(Calendar.DATE);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static long timeDifferenceInMinutes(Date date1, Date date2) {
        return TimeUnit.MINUTES.convert(date1.getTime() - date2.getTime(), TimeUnit.MILLISECONDS);
    }

    @Nullable
    public static String getTimeString(Message message, Message prevMessage) {
        if (prevMessage == null) return null;
        Date messageDate = message.getCreatedAt();
        if (messageDate == null) return null;
        Date prevMessageDate = prevMessage.getCreatedAt();
        if (prevMessageDate == null) return null;
        long diffInMinutes = DateUtils.timeDifferenceInMinutes(messageDate, prevMessageDate);
        if (diffInMinutes >= 5) {
            //If today
            if (DateUtils.isToday(messageDate)) {
                //Exclude date in time
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("h:mm a", Locale.US);
                df.setTimeZone(UTC);
                return df.format(messageDate);

            } else {
                //Include date in time
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.US);
                df.setTimeZone(UTC);
                return df.format(messageDate);
            }

        }
        return null;
    }


}
