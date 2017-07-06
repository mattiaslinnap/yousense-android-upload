package org.yousense.eventlog.data;

import android.content.Context;
import android.os.SystemClock;
import org.yousense.common.Counter;

public class Event<T> {
    public static final long NEVER = 3155692597470L;  // 100 years in milliseconds
    public static final long MAX_CLOCK_JUMP = 500;  // When calculating timeSince(), allow events this far in the future to be considered "now".

    public String tag;
    public long counter_event;
    public long time_system;
    public long time_uptime;
    public long time_realtime;

    public T data;

    public Event(Context context, String tag, T data) {
        this.tag = tag;
        this.counter_event = Counter.getNext(context, "eventlog_event");
        this.time_system = System.currentTimeMillis();
        this.time_uptime = SystemClock.uptimeMillis();
        this.time_realtime = SystemClock.elapsedRealtime();
        this.data = data;
    }

    public long timeSince() {
        long now = System.currentTimeMillis();
        if (time_system <= now) {
            // Event was recorded in the past, all OK.
            return now - time_system;
        } else {
            // Event was recorded in the future? Clock is messed up.
            long timeTo = time_system - now;
            if (timeTo <= MAX_CLOCK_JUMP)  // Allow for small clock jumps
                return 0;
            else
                return NEVER;  // Error code.
        }
    }
}
