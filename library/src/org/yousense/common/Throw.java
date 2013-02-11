package org.yousense.common;

import java.io.IOException;
import android.util.Log;
import org.yousense.eventlog.DebugLog;

public class Throw {
    public static void ioe(String tag, String format, Object... args) throws IOException {
        String message = String.format(format, args);
        IOException tr = new IOException(message);
        DebugLog.e(tag, message, tr);
        throw tr;
    }

    public static void rte(String tag, String format, Object... args) {
        String message = String.format(format, args);
        RuntimeException tr = new RuntimeException(message);
        DebugLog.e(tag, message, tr);
        throw tr;
    }

    public static void ise(String tag, String format, Object... args) throws IllegalStateException {
        String message = String.format(format, args);
        IllegalStateException tr = new IllegalStateException(message);
        DebugLog.e(tag, message, tr);
        throw tr;
    }

}
