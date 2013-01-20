package org.yousense.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.yousense.common.Files;
import org.yousense.common.Hash;
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

    // Public API

    /**
     * Copies a file to the pending files directory, queueing it for upload.
     * The original is not deleted.
     */
    public static void copyFileForUpload(Context context, File original) throws IOException {
        // TODO: check file exists and is readable for better error messaging.
        String sha1 = Hash.sha1Hex(original);
        File tempFile = new File(getMoveDirectory(context), original.getName());
        try {
            // Copy file to the same filesystem to enable atomic moves.
            FileUtils.copyFile(original, tempFile);
            if (!Hash.sha1Hex(tempFile).equals(sha1))
                throw new IOException("SHA1 mismatch after copy");
            // Atomic move into the upload directory to avoid race conditions on writing files.
            FileUtils.moveFileToDirectory(tempFile, getUploadDirectory(context), false);
        } catch (IOException e) {
            tempFile.delete();
            throw e;
        }
    }

    public static void startUpload(Context context) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_UPLOAD);
        context.startService(intent);
    }

    public static Status getStatus() {
        return status;
    }

    // Implementation

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            status = Status.UPLOADING;

            if (ACTION_UPLOAD.equals(intent.getAction())) {
                File[] pending = sortedPendingFiles(this);
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

    public static File[] sortedPendingFiles(Context context) throws IOException {
        return Files.listFilesSorted(getUploadDirectory(context));
    }

    public UploadService() {
        super("YouSense Upload Service");
        status = Status.IDLE;
    }

    private static File getMoveDirectory(Context context) throws IOException {
        return Files.getExternalSubdir(context, "yousense-upload-move");
    }

    private static File getUploadDirectory(Context context) throws IOException {
        return Files.getExternalSubdir(context, "yousense-upload");
    }
}
