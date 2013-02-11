package org.yousense.eventlog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.yousense.common.Files;
import org.yousense.common.ManifestInfo;
import org.yousense.common.Throw;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Main interface to the eventlog.
 * It is recommended to call a rotate*() function regularly, for example once per hour.
 *
 * By default, EventLog is disabled. Call EventLog.init(getApplicationContext()) in Application.onCreate() to start it.
 */
public class EventLog {
    public static final String TAG = "yousense-eventlog";
    public static final int WRITE_ATTEMPTS = 2;  // How many times a write is attempted before giving up.
    final static String OPEN_SUFFIX = ".open";
    final static String CLOSED_SUFFIX = ".log";
    final static String GZIPPED_SUFFIX = ".gz";
    final static FileFilter CLOSED_FILTER = new Files.SuffixFilter(CLOSED_SUFFIX, true);
    final static FileFilter GZIPPED_FILTER = new Files.SuffixFilter(GZIPPED_SUFFIX, true);

    private static EventFileWriter writer;
    private static Context context;

    /**
     * Call this from Application.onCreate() to enable EventLog.
     */
    public static synchronized void init(Context appContext) {
        context = appContext;
    }

    public static synchronized boolean append(String tag, Object data) {
        if (context == null) {
            // EventLog is disabled.
            return false;
        }

        if (writer == null)
            rotateWriter();

        for (int i = 0; i < WRITE_ATTEMPTS; ++i) {
            try {
                if (writer == null)
                    throw new IOException("EventFileWriter is null, cannot write");
                writer.appendEvent(context, tag, data);
                return true;  // Success
            } catch (IOException e) {
                Log.e(TAG, String.format("Failed to write event with tag %s, maybe retrying.", tag), e);
                rotateWriter();
            }
        }
        Log.e(TAG, String.format("Failed to write event with tag %s %d times. Giving up.", tag, WRITE_ATTEMPTS));
        return false;
    }

    public static synchronized void rotateAndStartGzip() {
        if (context == null) {
            Throw.rte(TAG, "Cannot rotate EventLog writer because it is disabled.");
        }

        Log.i(TAG, "Rotating eventlog file, will start gzip later.");
        rotateWriter();
        Intent intent = new Intent(context, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP);
        GzipService.checkManifest(context);
        context.startService(intent);
    }

    public static synchronized void rotateAndStartGzipAndUpload() {
        if (context == null) {
            Throw.rte(TAG, "Cannot rotate writer EventLog because it is disabled.");
        }

        Log.i(TAG, "Rotating eventlog file, will start gzip and upload later.");
        rotateWriter();
        Intent intent = new Intent(context, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP_AND_UPLOAD);
        GzipService.checkManifest(context);
        context.startService(intent);
    }

    /**
     * Directory with log files. Open files have suffix .open, Closed files .log.
     * Gzipped files are named .log.gz, and there may be temporary files .log.gz.temp.
     */
    public static File getLogDirectory() throws IOException {
        if (context == null) {
            Throw.rte(TAG, "Cannot get EventLog directory because it is disabled.");
        }

        return Files.getInternalSubdir(context, "yousense-eventlog");
    }

    private static void rotateWriter() {
        if (context == null) {
            Throw.rte(TAG, "Cannot rotate EventLog writer because it is disabled.");
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
            Files.moveAllFilesSortedSuffix(getLogDirectory(), OPEN_SUFFIX, CLOSED_SUFFIX);
        } catch (IOException e) {
            Log.e(TAG, "Failed to move eventlog files from open to closed directory.", e);
        }

        // Open new writer.
        try {
            writer = new EventFileWriter(context);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open new EventFileWriter", e);
        }
    }
}
