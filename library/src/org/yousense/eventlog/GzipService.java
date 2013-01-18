package org.yousense.eventlog;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.yousense.common.Files;
import org.yousense.common.Gzip;
import org.yousense.upload.UploadService;

import java.io.File;
import java.io.IOException;

public class GzipService extends IntentService {
    public static final String ACTION_GZIP = "org.yousense.intent.action.GZIP";
    public static final String ACTION_GZIP_AND_UPLOAD = "org.yousense.intent.action.GZIP_AND_UPLOAD";

    // Public API

    public GzipService() {
        super("YouSense EventLog Gzip Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ACTION_GZIP.equals(intent.getAction()) && !ACTION_GZIP_AND_UPLOAD.equals(intent.getAction()))
            return;

        // Gzip all files that are not open, in order.
        try {
            for (File file : Files.listFilesSorted(EventLog.getClosedDirectory(this))) {
                if (!isGzip(file)) {
                    // Todo: check that duplicates are removed
                    Gzip.gzip(file);
                }
            }
        } catch (IOException e) {
            // First error terminates gzipping. Copying of the successful files can proceed.
        }

        // Copy all .gz files for upload, in order.
        try {
            for (File file : Files.listFilesSorted(EventLog.getClosedDirectory(this))) {
                if (isGzip(file)) {
                    UploadService.copyFileForUpload(this, file);
                    file.delete();
                }
            }
        } catch (IOException e) {
            Log.e(EventLog.TAG, "Error copying .gz file to upload directory.", e);
        }

        // Start upload now if requested
        if (ACTION_GZIP_AND_UPLOAD.equals(intent.getAction())) {
            UploadService.startUpload(this);
        }
    }

    private static boolean isGzip(File file) {
        return file.getName().endsWith(".gz");
    }
}
