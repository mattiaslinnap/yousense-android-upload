package org.yousense.eventlog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class GzipService extends IntentService {

    // Public API


    public static void startGzipFilesAndUpload(Context context) {

    }


    public GzipService() {
        super("YouSense Log Compressing Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
