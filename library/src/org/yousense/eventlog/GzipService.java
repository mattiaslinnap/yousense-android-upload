package org.yousense.eventlog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.yousense.common.Files;
import org.yousense.common.Gzip;
import org.yousense.common.ManifestInfo;
import org.yousense.upload.UploadService;

import java.io.File;
import java.io.IOException;

public class GzipService extends IntentService {
    public static final String TAG = EventLog.TAG;
    public static final String ACTION_GZIP = "org.yousense.intent.action.GZIP";
    public static final String ACTION_GZIP_AND_UPLOAD = "org.yousense.intent.action.GZIP_AND_UPLOAD";

    public GzipService() {
        super("YouSense EventLog Gzip Service");
    }

    public static void checkManifest(Context context) {
        if (!ManifestInfo.hasService(context, "org.yousense.eventlog.GzipService"))
            Log.e(TAG, "You forgot to add <service android:name=\"org.yousense.eventlog.GzipService\"> to AndroidManifest.xml.");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ACTION_GZIP.equals(intent.getAction()) && !ACTION_GZIP_AND_UPLOAD.equals(intent.getAction()))
            return;

        // It's safe to call DebugLog from here, as gzipping is not in the same thread as eventlog file rotation.
        DebugLog.i(this, TAG, "GzipService starting.");

        // Gzip all files that are not open, in order.
        try {
            for (File file : Files.listFilesSorted(EventLog.getClosedDirectory(this))) {
                if (!isGzip(file)) {
                    // Todo: check that duplicates are removed
                    Gzip.gzip(file);
                }
            }
        } catch (IOException e) {
            DebugLog.e(this, TAG, "Error gzipping files.", e);
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
            DebugLog.e(this, TAG, "Error moving files to upload directory.", e);
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
