package com.annabenson.viand;

import com.annabenson.viand.utils.DateUtils;

import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void getCurrentWeekStart_returnsMonday() {
        long epochSeconds = DateUtils.getCurrentWeekStart();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epochSeconds * 1000);
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void getCurrentWeekStart_returnsMidnight() {
        long epochSeconds = DateUtils.getCurrentWeekStart();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epochSeconds * 1000);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void getCurrentWeekStart_isInSeconds_notMilliseconds() {
        long epochSeconds = DateUtils.getCurrentWeekStart();
        // Seconds since epoch for year 2020+ is ~1.58 billion, well under Long.MAX_VALUE.
        // Milliseconds would be ~1.58 trillion. A value over 10 billion means ms were returned.
        assertTrue("Value looks like milliseconds, not seconds", epochSeconds < 10_000_000_000L);
    }

    @Test
    public void getCurrentWeekStart_isNotInFuture() {
        long epochSeconds = DateUtils.getCurrentWeekStart();
        long nowSeconds = System.currentTimeMillis() / 1000;
        assertTrue("Week start should not be in the future", epochSeconds <= nowSeconds);
    }

    @Test
    public void getCurrentWeekStart_isWithinLastSevenDays() {
        long epochSeconds = DateUtils.getCurrentWeekStart();
        long nowSeconds = System.currentTimeMillis() / 1000;
        long sevenDaysInSeconds = 7 * 24 * 60 * 60;
        assertTrue("Week start should be within the last 7 days",
                nowSeconds - epochSeconds < sevenDaysInSeconds);
    }
}
