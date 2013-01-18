package org.yousense.eventlog;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.yousense.common.Files;

import java.io.File;
import java.io.IOException;

public class EventLog {
    public static final String TAG = "yousense-eventlog";
    public static final int WRITE_ATTEMPTS = 2;  // How many times a write is attempted before giving up.

    private static EventFileWriter writer;

    public static synchronized boolean append(Context context, String tag, Object data) {
        if (writer == null)
            rotateWriter(context);

        for (int i = 0; i < WRITE_ATTEMPTS; ++i) {
            try {
                if (writer == null)
                    throw new IOException("EventFileWriter is null, cannot write");
                writer.appendEvent(context, tag, data);
                return true;  // Success
            } catch (IOException e) {
                Log.e(TAG, String.format("Failed to write event with tag %s, maybe retrying.", tag), e);
                rotateWriter(context);
            }
        }
        Log.e(TAG, String.format("Failed to write event with tag %s %d times. Giving up.", tag, WRITE_ATTEMPTS));
        return false;
    }

    public static synchronized void rotateAndStartGzip(Context context) {
        rotateWriter(context);
        Intent intent = new Intent(context, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP);
        context.startService(intent);
    }

    public static synchronized void rotateAndStartGzipAndUpload(Context context) {
        rotateWriter(context);
        Intent intent = new Intent(context, GzipService.class);
        intent.setAction(GzipService.ACTION_GZIP_AND_UPLOAD);
        context.startService(intent);
    }

    /**
     * Directory with the file currently being written to.
     * May contain also written and closed files.
     * All files are atomically moved to getClosedDirectory() during file rotation.
     */
    static File getOpenDirectory(Context context) throws IOException {
        return Files.getInternalSubdir(context, "yousense-eventlog-open");
    }

    /**
     * Directory with log files that have been written and closed.
     */
    static File getClosedDirectory(Context context) throws IOException {
        return Files.getInternalSubdir(context, "yousense-eventlog");
    }

    private static void rotateWriter(Context context) {
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
            Files.moveAllFilesSorted(getOpenDirectory(context), getClosedDirectory(context));
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
