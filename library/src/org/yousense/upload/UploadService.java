package org.yousense.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.yousense.upload.exceptions.ConfigurationException;
import org.yousense.upload.exceptions.ServerUnhappyException;
import org.yousense.upload.net.FileRequest;
import org.yousense.upload.net.StatusRequest;

import java.io.File;
import java.io.IOException;

public class UploadService extends IntentService {
    public static final String TAG = "yousense-upload";
    public static final String ACTION_UPLOAD = "org.yousense.intent.action.UPLOAD";
    public enum Status { IDLE, UPLOADING }

    private static volatile Status status;

    public UploadService() {
        super("YouSense Upload Service");
        status = Status.IDLE;
    }

    public static Status getStatus() {
        return status;
    }

    public static void startUpload(Context context) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_UPLOAD);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            status = Status.UPLOADING;

            if (ACTION_UPLOAD.equals(intent.getAction())) {
                File[] pending = UploadFolder.sortedPendingFiles(this);
                new StatusRequest(this, pending).run();

                for (File file : pending) {
                    new FileRequest(this, file).run();
                    file.delete();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (ConfigurationException e) {
            Log.e(TAG, "ConfigurationException", e);
        } catch (ServerUnhappyException e) {
            Log.e(TAG, "ServerUnhappyException", e);
        } finally {
            status = Status.IDLE;
        }
    }
}
