package org.yousense.common;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class provides a fast in-memory cache for preferences and persists
 * them to disk on write, but no more frequently than specified by the user. 
 */
public class AsyncPrefs {
	private static final String PREFS_FILE = "async";
	private static long lastFlush;
	private static Map<String, Long> longValues = new HashMap<String, Long>();
	
	private static int minFlushIntervalInMs = 10000;
	public static void setMinFlushInterval(int val) {
		minFlushIntervalInMs = val;
	}
	
	/**
	 * Places a value in the in-memory cache. May also (synchronously) write to disk.
	 * @return Whether or not the store was persisted to disk.
	 */
	public static synchronized boolean putLong(Context ctx, String key, long val) {
		longValues.put(key, val);
		if(System.currentTimeMillis() - lastFlush >= minFlushIntervalInMs) {
			flush(ctx);
			return true;
		}
		return false;
	}
	
	public static synchronized long getLong(Context ctx, String key) {
		if(!longValues.containsKey(key)) {
			SharedPreferences prefs = ctx.getSharedPreferences(PREFS_FILE, 0);
            longValues.put(key, prefs.getLong(key, 0));
		}
		return longValues.get(key);
	}
	
	/**
	 * Synchronously writes all stored objects to disk.
	 */
	public static void flush(Context ctx) {
		SharedPreferences.Editor editor = ctx.getSharedPreferences(PREFS_FILE, 0).edit();
		for(String key : longValues.keySet()) {
			editor.putLong(key, longValues.get(key));
		}
        editor.commit();
        lastFlush = System.currentTimeMillis();
	}
	
}
