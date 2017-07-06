package org.yousense.eventlog;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.yousense.common.Files;
import org.yousense.common.TypedJson;
import org.yousense.eventlog.data.Event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a persistent cache of the last event of each type seen.
 * All EventLog.append() calls write to LastCache, but persistence has to be enabled for each type in LastCache.init().
 */
public class LatestCache {
    public static final String TAG = EventLog.TAG;

    private Gson gson;
    private File file;
    private Map<String, Type> persistTypes;
    private Map<String, Event> cache;

    /**
     * Types must be Gson TypeTokens for Event<T>.
     */
    public LatestCache(File file, Map<String, Type> persistTypes) {
        this.gson = new Gson();
        this.file = file;
        if (persistTypes != null)
            this.persistTypes = persistTypes;
        else
            this.persistTypes = new HashMap<String, Type>();  // Empty map
        this.cache = new HashMap<String, Event>();
        loadCacheFromDisk();
    }

    public synchronized <T> void put(Event<T> event) {
        cache.put(event.tag, event);
        if (persistTypes.containsKey(event.tag))
            writeCacheToDisk();
    }

    public synchronized Event get(String tag) {
        return cache.get(tag);
    }

    public synchronized long timeSince(String tag) {
        Event event = get(tag);
        if (event == null)
            return Event.NEVER;
        else
            return event.timeSince();
    }

    // Implementation

    private Map<String, Event> persistMap() {
        Map<String, Event> persist = new HashMap<String, Event>();
        for (String tag : persistTypes.keySet()) {
            Event event = cache.get(tag);
            if (event != null) {
                persist.put(tag, event);
            }
        }
        return persist;
    }

    private void loadCacheFromDisk() {
        try {
            Map<String, Event> loaded = TypedJson.fromJsonObjectTypedByKeys(FileUtils.readFileToString(file, Files.UTF8), persistTypes);
            for (Map.Entry<String, Event> entry : loaded.entrySet()) {
                if (persistTypes.containsKey(entry.getKey())) {
                    cache.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (IOException e) {
            DebugLog.e(TAG, "Error reading LatestCache", e);
        }
    }

    private void writeCacheToDisk() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(file, Files.UTF8));
            gson.toJson(persistMap(), writer);
            writer.close();
        } catch (IOException e) {
            DebugLog.e(TAG, "Error writing LatestCache", e);
        }
    }
}
