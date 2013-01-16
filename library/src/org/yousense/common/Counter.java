package org.yousense.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Persistent counter with memory caching.
 * Values returned may not be contiguous, but will be increasing. (Max jumps are < 2 * persistInterval)
 *
 * Do not ever change the PERSIST_INTERVAL to be shorter, even between app versions - it may lead to duplicate values,
 * as jumps after a crash may be too short.
 * Changing PERSIST_INTERVAL to be larger is okay.
 */
public class Counter {
	public static final String PREFS_FILE = "counter";
    public static final String PREFS_KEY_PREFIX = "counter_";
    public static final int PERSIST_INTERVAL = 100;

    private static HashMap<String, CachedCounter> cachedCounters = new HashMap<String, CachedCounter>();

    // Public API

    public static synchronized long getNext(Context context, String name) {
        CachedCounter counter = cachedCounters.get(name);
        if (counter == null || !counter.isValid()) {
            counter = new CachedCounter(name);
            counter.loadFromStorageAndJump(context);
            cachedCounters.put(name, counter);
        }
        // By this time, counter must be valid. Otherwise things are too messed up.
        if (!counter.isValid())
            throw new IllegalStateException("Loaded counter was invalid.");
        ++counter.value;
        if (counter.value >= counter.lastSaved + PERSIST_INTERVAL)
            counter.saveToStorage(context);
        return counter.value;
    }

    // Private persistence logic

    private static class CachedCounter {
        String name;
        long value;
        long lastSaved;

        CachedCounter(String name) {
            this.name = name;
        }

        boolean isValid() {
            return (value > PERSIST_INTERVAL &&  //
                    value >= lastSaved);
        }

        synchronized void loadFromStorageAndJump(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
            long value = prefs.getLong(PREFS_KEY_PREFIX + name, 0);
            value += 2 * PERSIST_INTERVAL;  // Increment counter by more than a whole interval, so that any non-saved values are skipped, and the jump itself is obvious.
            saveToStorage(context);  // Make sure the jump is recorded
        }

        synchronized void saveToStorage(Context context) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_FILE, 0).edit();
            editor.putLong(PREFS_KEY_PREFIX + name, value);
            editor.commit();
            lastSaved = value;
        }
    }
}
