package org.yousense.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

/**
 * Persistent counter, that complains if it is not incremented before a read.
 * Useful for managing an app restart counter.
 * 
 * The value is not reset at app reinstalls or updates - but is when the app is uninstalled or "Clear Data" is used.
 */
public class RestartCounter {

    public static final String TAG = AppId.TAG;
	public static final String PREFS_FILE = "restartcounter";
	public static final String PREFS_KEY = "counter_restart";

    static long count = 0;
    static boolean valid = false;

	public static synchronized long getValue(Context context) throws IOException {
        if (!valid)
            incrementAndCache(context);
        if (!valid)
            Throw.ioe(TAG, "Could not read and increment RestartCounter.");
        return count;
    }

    private static void incrementAndCache(Context context) {
        // Find last counter value, or 1 if first launch.
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        long lastCount = prefs.getLong(PREFS_KEY, 0);
        count = lastCount + 1;

        // Save new value to disk.
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREFS_KEY, count);
        editor.commit();
        valid = true;
    }
}
