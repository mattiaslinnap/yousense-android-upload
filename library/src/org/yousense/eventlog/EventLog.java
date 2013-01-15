package org.yousense.eventlog;

import java.io.File;

public class EventLog {

    /**
     * Must be called before append() is valid.
     * Tip: Put this into your Application object constructor or onCreate() callback.
     */
    public static synchronized void init() {

    }

    public static synchronized void append(String tag, Object data) {

    }

}
