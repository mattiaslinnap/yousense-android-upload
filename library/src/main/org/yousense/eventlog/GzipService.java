package org.yousense.eventlog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.yousense.common.Files;
import org.yousense.common.Gzip;
import org.yousense.common.ManifestInfo;
import org.yousense.common.Throw;
import org.yousense.upload.UploadService;

import java.io.File;
import java.io.IOException;

/**
 * Background Service for GZipping closed EventLog files.
 * Do not start this service directly, use static methods on EventLog.
 */
public class GzipService extends IntentService {
    public static final String TAG = EventLog.TAG;
    public static final String ACTION_GZIP = "org.yousense.intent.action.GZIP";
    public static final String ACTION_GZIP_AND_UPLOAD = "org.yousense.intent.action.GZIP_AND_UPLOAD";

    public GzipService() {
        super("YouSense EventLog Gzip Service");
    }

    public static void checkManifest(Context context) {
        if (!ManifestInfo.hasService(context, "org.yousense.eventlog.GzipService"))
            Throw.ce(TAG, "You forgot to add <service android:name=\"org.yousense.eventlog.GzipService\"> to AndroidManifest.xml.");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ACTION_GZIP.equals(intent.getAction()) && !ACTION_GZIP_AND_UPLOAD.equals(intent.getAction()))
            return;

        // It's safe to call DebugLog from here, as gzipping is not in the same thread as eventlog file rotation.
        EventLog.append("app.gzip.start", null);
        DebugLog.dLog(TAG, "GzipService starting.");

        int deleted = 0;
        int gzipped = 0;
        int moved = 0;
        // For all closed log files, in filename order:
        // * delete empty files,
        // * gzip files with data.
        try {
            for (File file : Files.listFilesSorted(EventLog.getLogDirectory(this), EventLog.CLOSED_FILTER)) {
                if (file.length() == 0) {
                    file.delete();
                    ++deleted;
                } else {
                    Gzip.gzip(file);
                    ++gzipped;
                }
            }
        } catch (IOException e) {
            DebugLog.eLog(TAG, "Error gzipping files.", e);
            // First error terminates gzipping. Copying of the successful files can proceed.
        }

        // Copy all .gz files for upload, in order.
        try {
            for (File file : Files.listFilesSorted(EventLog.getLogDirectory(this), EventLog.GZIPPED_FILTER)) {
                UploadService.copyFileForUpload(this, file);
                file.delete();
                ++moved;
            }
        } catch (IOException e) {
            DebugLog.eLog(TAG, "Error moving files to upload directory.", e);
        }
        DebugLog.dLog(TAG, String.format("GzipService done. Deleted %d, gzipped %d, moved %d files for upload.", deleted, gzipped, moved));

        // Start upload now if requested
        if (ACTION_GZIP_AND_UPLOAD.equals(intent.getAction())) {
            UploadService.startUpload(this);
        }
    }
}
