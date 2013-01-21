package org.yousense.common;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Locale;
import java.util.TimeZone;

public class Time {

    public static TimeZone UTC = TimeZone.getTimeZone("UTC");
    public static Locale US = Locale.US;
    public static FastDateFormat NOSPACES_MILLISECONDS = FastDateFormat.getInstance("yyyyMMdd_HHmmss_SSS", UTC, US);
    public static FastDateFormat TIMEONLY_MILLISECONDS = FastDateFormat.getInstance("HH:mm:ss.SSS", UTC, US);

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
}
