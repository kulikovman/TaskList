package ru.kulikovman.tasklist;


import android.content.Context;
import android.util.TypedValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateHelper {
    private static DateFormat mLongDateFormat;
    private static DateFormat mShortDateFormat;

    static {
        mLongDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        mShortDateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
    }

    public static Calendar convertLongTextDateToCalendar(String textDate) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(mLongDateFormat.parse(textDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }

    public static Calendar convertLongToCalendar(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

        return calendar;
    }

    public static long convertLongTextDateToLong(String longTextDate) {
        long date = 0;
        try {
            date = mLongDateFormat.parse(longTextDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static Date convertLongTextDateToDate(String longTextDate) {
        Date date = new Date();
        try {
            date = mLongDateFormat.parse(longTextDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String convertCalendarToLongTextDate(Calendar calendar) {
        return mLongDateFormat.format(calendar.getTime());
    }

    public static String convertCalendarToShortTextDate(Calendar calendar) {
        return mShortDateFormat.format(calendar.getTime());
    }

    public static String convertDateToLongTextDate(Date date) {
        return mLongDateFormat.format(date);
    }

    public static String convertDateToShortTextDate(Date date) {
        return mShortDateFormat.format(date);
    }

    public static String convertLongToLongTextDate(long longDate) {
        return mLongDateFormat.format(new Date(longDate));
    }

    public static String convertLongToShortTextDate(long longDate) {
        return mShortDateFormat.format(new Date(longDate));
    }

    /*public static Calendar getTodayCalendarWithoutTime() {
        String date = convertLongToLongTextDate(Calendar.getInstance().getTimeInMillis());
        return convertLongTextDateToCalendar(date);
    }*/

    public static Calendar getTodayCalendarWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static Calendar getAfterWeekCalendarWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.WEEK_OF_YEAR, 1);

        return calendar;
    }

    public static Calendar getAfterMonthCalendarWithoutTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MONTH, 1);

        return calendar;
    }

    public static int convertDpToPx(Context context, int valueInDp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp,
                context.getResources().getDisplayMetrics());
    }
}
