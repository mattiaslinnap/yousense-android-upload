package org.yousense.eventlog.data;

import android.os.SystemClock;

public class EventData {
    String tag;
    long counter_object;
    long time_system;
    long time_uptime;
    long time_realtime;

    Object data;

    public EventData(String tag, Object data, long objectCounter) {
        this.tag = tag;
        this.counter_object = objectCounter;
        this.time_system = System.currentTimeMillis();
        this.time_uptime = SystemClock.uptimeMillis();
        this.time_realtime = SystemClock.elapsedRealtime();
        this.data = data;
    }
}
