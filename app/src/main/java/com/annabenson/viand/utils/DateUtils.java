package com.annabenson.viand.utils;

import java.util.Calendar;

public class DateUtils {

    /** Returns Unix epoch seconds for Monday 00:00:00 of the current week. */
    public static long getCurrentWeekStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }
}
