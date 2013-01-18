package org.yousense.eventlog;

import org.yousense.eventlog.data.EventData;
import org.yousense.eventlog.format.Formatter;

public class EventLog {
    public static final String TAG = "yousense-eventlog";
    public static final String UTF8 = "UTF-8";

    private static boolean valid = false;
    private static Formatter formatter;
    private static AppendWriter appendWriter;

    /**
     * Must be called before append() is valid.
     * Tip: Put this into your Application object constructor or onCreate() callback.
     */
    public static synchronized void init(Formatter _formatter) {
        if (valid) {
            throw new IllegalStateException("EventLog is already initialised. Do it once in your Application onCreate().");
        }

        formatter = _formatter;
        rotateWriter();
        valid = true;
    }

    public static synchronized void append(String tag, Object data) {
        if (!valid)
            throw new IllegalStateException("EventLog is not initialised. Do it before other calls in your Application onCreate().");
        if (!appendWriter)
            appendWriter.append(new EventData(tag, data));
    }

    public static synchronized void rotateAndStartGzip() {
        if (!valid)
            throw new IllegalStateException("EventLog is not initialised. Do it before other calls in your Application onCreate().");
        rotateWriter();
    }

    private static void rotateWriter() {
        if (appendWriter != null) {
            appendWriter.close();
        }
        appendWriter = new AppendWriter();
    }
}
