package org.yousense.eventlog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.yousense.common.Files;
import org.yousense.common.Throw;
import org.yousense.eventlog.data.Event;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Main interface to the eventlog.
 * It is recommended to call a rotate*() function regularly, for example once per hour.
 *
 * By default, EventLog is disabled. To enable EventLog in your application:
 * * add <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> to AndroidManifest.xml,
 * * add <service android:name="org.yousense.eventlog.GzipService" /> to AndroidManifest.xml,
 * * add EventLog.init(getApplicationContext()) to Application.onCreate().
 */
public class EventLog {
    public static final String TAG = "yousense-eventlog";
    public static final int WRITE_ATTEMPTS = 2;  // How many times a write is attempted before giving up.
    final static String OPEN_SUFFIX = ".open";
    final static String CLOSED_SUFFIX = ".log";
    final static String GZIPPED_SUFFIX = ".gz";
    final static FileFilter CLOSED_FILTER = new Files.SuffixFilter(CLOSED_SUFFIX, true);
    final static FileFilter GZIPPED_FILTER = new Files.SuffixFilter(GZIPPED_SUFFIX, true);

    private static Context appContext;
    private static EventFileWriter writer;
    private static LatestCache latestCache;

    /**
     * Call this from Application.onCreate() to enable EventLog.
     */
    public static synchronized void init(Context appContext, Map<String, Type> latestCachePersistTypes) {
        if (appContext != null) {
            GzipService.checkManifest(appContext);
            EventLog.appContext = appContext;
            EventLog.latestCache = new LatestCache(appContext, getLatestCacheFile(appContext), latestCachePersistTypes);
        } else {
            EventLog.appContext = null;
            EventLog.latestCache = null;
        }
    }

    /**
     * Main logging call.
     */
    public static synchronized <T> boolean append(String tag, T data) {
        // NOTE: Make sure this method never tries to eventually write to EventLog again via Throw or DebugLog.
        if (appContext == null) {
            // EventLog is disabled.
            return false;
        }

        if (writer == null)
            rotateWriter();

        for (int i = 0; i < WRITE_ATTEMPTS; ++i) {
            try {
                if (writer == null)
                    throw new IOException("EventFileWriter is null, cannot write");
                writer.appendEvent(appContext, tag, data);
                return true;  // Success
            } catch (IOException e) {
                Log.e(TAG, String.format("Failed to write event with tag %s, maybe retrying.", tag), e);
                rotateWriter();
            }
        }
        Log.e(TAG, String.format("Failed to write event with tag %s %d times. Giving up.", tag, WRITE_ATTEMPTS));
        return false;
    }

    public static synchronized Event getLatest(String tag) {
        if (latestCache != null)
            return latestCache.get(tag);
        else
            return null;
    }

    public static synchronized long timeSince(String tag) {
        if (latestCache != null)
            return latestCache.timeSince(tag);
        else
            return LatestCache.INFINITY;
    }

    public static synchronized void rotateAndStartGzip() {
        if (appContext == null)
            Throw.ise(TAG, "Cannot rotate EventLog writer because it is disabled.");
        Log.i(TAG, "Rotating eventlog file, will start gzip later.");
        rotateWriter();
        Intent intent = new Intent(appContext, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP);
        appContext.startService(intent);
    }

    public static synchronized void rotateAndStartGzipAndUpload() {
        if (appContext == null)
            Throw.ise(TAG, "Cannot rotate writer EventLog because it is disabled.");
        Log.i(TAG, "Rotating eventlog file, will start gzip and upload later.");
        rotateWriter();
        Intent intent = new Intent(appContext, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP_AND_UPLOAD);
        appContext.startService(intent);
    }

    /**
     * Directory with log files. Open files have suffix .open, Closed files .log.
     * Gzipped files are named .log.gz, and there may be temporary files .log.gz.temp.
     */
    public static File getLogDirectory(Context context) throws IOException {
        return Files.getInternalSubdir(context, "yousense-eventlog");
    }

    // Implementation

    public static File getLatestCacheFile(Context context) {
        // Outside EventLog.getLogDirectory(), don't want file handling and gzip mess.
        return new File(context.getFilesDir(), "yousense-latestcache.json");
    }

    private static void rotateWriter() {
        // NOTE: rotateWriter() is called from append().
        // Make sure this method never tries to eventually write to EventLog again via Throw or DebugLog.
        if (appContext == null) {
            Throw.ise(TAG, "Cannot rotate EventLog writer because it is disabled.");
        }

        // Close existing writer.
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close EventFileWriter", e);
            }
            writer = null;
        }

        try {
            Files.moveAllFilesSortedSuffix(getLogDirectory(appContext), OPEN_SUFFIX, CLOSED_SUFFIX);
        } catch (IOException e) {
            Log.e(TAG, "Failed to move eventlog files from open to closed directory.", e);
        }

        // Open new writer.
        try {
            writer = new EventFileWriter(appContext, latestCache);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open new EventFileWriter", e);
        }
    }

    // Dangerous public API - if your are using it outside tests, you are probably doing something wrong.

    /**
     * Do not call this outside of tests.
     */
    public static synchronized void resetWriterAndDeleteLogDirectory(Context context) throws IOException {
        if (writer != null)
            writer.close();
        writer = null;
        if (getLogDirectory(context).exists())
            FileUtils.deleteDirectory(getLogDirectory(context));
        if (getLatestCacheFile(context).exists())
            FileUtils.deleteQuietly(getLatestCacheFile(context));
    }
}
