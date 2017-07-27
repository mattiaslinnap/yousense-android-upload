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

    public static void ioeLog(String tag, String format, Object... args) throws IOException {
        String message = String.format(format, args);
        IOException tr = new IOException(message);
        DebugLog.eLog(tag, message, tr);
        throw tr;
    }

    public static void ce(String tag, String format, Object... args) throws ConfigurationException {
        String message = String.format(format, args);
        ConfigurationException tr = new ConfigurationException(message);
        DebugLog.e(tag, message, tr);
        throw tr;
    }

    public static void ceLog(String tag, String format, Object... args) throws ConfigurationException {
        String message = String.format(format, args);
        ConfigurationException tr = new ConfigurationException(message);
        DebugLog.eLog(tag, message, tr);
        throw tr;
    }

    public static void ise(String tag, String format, Object... args) throws IllegalStateException {
        String message = String.format(format, args);
        IllegalStateException tr = new IllegalStateException(message);
        DebugLog.e(tag, message, tr);
        throw tr;
    }

}
