package org.yousense.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;

public class Time {

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
}
