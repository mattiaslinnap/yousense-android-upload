package org.yousense.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;

public class Time {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static TimeZone UTC = TimeZone.getTimeZone("UTC");
    public static Locale US = Locale.US;
    public static FastDateFormat NOSPACES_MILLISECONDS = FastDateFormat.getInstance("yyyyMMdd_HHmmss_SSS", UTC, US);
    public static FastDateFormat TIMEONLY_MILLISECONDS = FastDateFormat.getInstance("HH:mm:ss.SSS", UTC, US);
    public static FastDateFormat ISO_8604_WITH_RFC_822_TIME_ZONE = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static String timestampNoSpacesWithMilliseconds(long millis) {
        return NOSPACES_MILLISECONDS.format(millis);
    }

    public static String timestampNoSpacesWithMilliseconds() {
        return timestampNoSpacesWithMilliseconds(System.currentTimeMillis());
    }

    public static String timeOnlyWithMilliseconds(long millis) {
        return TIMEONLY_MILLISECONDS.format(millis);
    }

    public static String timeOnlyWithMilliseconds() {
        return timeOnlyWithMilliseconds(System.currentTimeMillis());
    }
    
    public static String isoFormat(Calendar cal) {
    	return ISO_8604_WITH_RFC_822_TIME_ZONE.format(cal);
    }
    
    public static String isoFormat(Date date) {
    	return ISO_8604_WITH_RFC_822_TIME_ZONE.format(date);
    }
    
    public static String isoFormat() {
    	return isoFormat(new Date());
    }

    /**
     * Returns strings like "13h 54min".
     */
    public static String hoursMinutesDeltaFormat(long millis) {
        StringBuilder sb = new StringBuilder();
        if (millis < 0) {
            sb.append("-");
            millis = -millis;
        }
        long hours = millis / HOUR;
        long minutes = (millis / MINUTE) % 60;
        sb.append(hours);
        sb.append("h ");
        sb.append(minutes);
        sb.append("min");
        return sb.toString();
    }

    /**
     * Returns localised strings like "1 day ago" or "5 min ago".
     */
    public static String approximateTimeSinceFormat(long millis, String localisedNow, String localisedMin, String localisedHours, String localisedDays, String localisedAgo) {
        if (millis < MINUTE)
            return localisedNow;
        else if (millis < HOUR)
            return String.format("%d %s %s", millis / MINUTE, localisedMin, localisedAgo);
        else if (millis < DAY)
            return String.format("%d %s %s", millis / HOUR, localisedHours, localisedAgo);
        else
            return String.format("%d %s %s", millis / DAY, localisedDays, localisedAgo);
    }

    public static void sleepIgnoreInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
