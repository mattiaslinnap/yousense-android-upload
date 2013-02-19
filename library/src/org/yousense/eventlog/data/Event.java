package org.yousense.eventlog.data;

import android.content.Context;
import android.os.SystemClock;
import org.yousense.common.Counter;

public class Event<T> {
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
}
