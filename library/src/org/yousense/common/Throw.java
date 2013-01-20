package org.yousense.common;

import java.io.IOException;
import android.util.Log;

public class Throw {
    public static void ioe(String tag, String format, Object... args) throws IOException {
        String message = String.format(format, args);
        Log.e(tag, message);
        throw new IOException(message);
    }

    public static void ise(String tag, String format, Object... args) throws IllegalStateException {
        String message = String.format(format, args);
        Log.e(tag, message);
        throw new IllegalStateException(message);
    }

}
