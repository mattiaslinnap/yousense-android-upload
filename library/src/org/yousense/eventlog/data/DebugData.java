package org.yousense.eventlog.data;

import android.util.Log;

public class DebugData {
    public String level;
    public String tag;
    public String message;
    public String stacktrace;

    public DebugData(int levelNumber, String tag, String message, Throwable tr) {
        switch (levelNumber) {
            case Log.ASSERT: this.level = "assert"; break;
            case Log.DEBUG: this.level = "debug"; break;
            case Log.ERROR: this.level = "error"; break;
            case Log.INFO: this.level = "info"; break;
            case Log.VERBOSE: this.level = "verbose"; break;
            case Log.WARN: this.level = "warn"; break;
            default: this.level = "unknown";
        }
        this.tag = tag;
        this.message = message;
        if (tr != null) {
            stacktrace = Log.getStackTraceString(tr);
        }
    }
}
