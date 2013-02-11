package org.yousense.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.yousense.common.Files;
import org.yousense.common.Hash;
import org.yousense.common.ManifestInfo;
import org.yousense.upload.exceptions.ConfigurationException;
import org.yousense.upload.exceptions.ServerUnhappyException;
import org.yousense.upload.net.FileRequest;
import org.yousense.upload.net.StatusRequest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class UploadService extends IntentService {
    public static final String TAG = "yousense-upload";
    public static final String ACTION_UPLOAD = "org.yousense.intent.action.UPLOAD";
    public enum Status { IDLE, UPLOADING }
    final static String TEMP_SUFFIX = ".temp";
    final static FileFilter VALID_FILTER = new Files.SuffixFilter(TEMP_SUFFIX, false);

    private static volatile Status status;

    // Public API

    /**
     * Copies a file to the pending files directory, queueing it for upload.
     * The original is not deleted.
     */
    public static void copyFileForUpload(Context context, File original) throws IOException {
        // TODO: check file exists and is readable for better error messaging.
        String sha1 = Hash.sha1Hex(original);
        File finalFile = new File(getUploadDirectory(context), original.getName());
        File tempFile = Files.appendSuffix(finalFile, TEMP_SUFFIX);
        try {
            // Copy file to the same filesystem to enable atomic moves.
            FileUtils.copyFile(original, tempFile);
            if (!Hash.sha1Hex(tempFile).equals(sha1))
                throw new IOException("SHA1 mismatch after copy");
            // Atomic move to remove suffix.
            tempFile.renameTo(finalFile);
        } catch (IOException e) {
            tempFile.delete();
            throw e;
        }
    }

    public static void startUpload(Context context) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_UPLOAD);
        checkManifest(context);
        context.startService(intent);
    }

    public static Status getStatus() {
        return status;
    }

    public static void checkManifest(Context context) {
        if (!ManifestInfo.hasService(context, "org.yousense.upload.UploadService"))
            Log.e(TAG, "You forgot to add <service android:name=\"org.yousense.upload.UploadService\"> to AndroidManifest.xml.");
    }

    // Implementation

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            status = Status.UPLOADING;

            if (ACTION_UPLOAD.equals(intent.getAction())) {
                new StatusRequest(this).run();
                for (File file : sortedValidFiles(this)) {
                    new FileRequest(this, file).run();
                    file.delete();
                }
                new StatusRequest(this).run();
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

    public static File[] sortedValidFiles(Context context) throws IOException {
        return Files.listFilesSorted(getUploadDirectory(context), VALID_FILTER);
    }

    public static File getUploadDirectory(Context context) throws IOException {
        return Files.getExternalSubdir(context, "yousense-upload");
    }

    public UploadService() {
        super("YouSense Upload Service");
        status = Status.IDLE;
    }

    // Dangerous public API - if your are using it outside tests, you are probably doing something wrong.

    /**
     * Do not call this outside of tests.
     */
    public static void deleteUploadDirectory(Context context) throws IOException {
        if (getUploadDirectory(context).exists())
            FileUtils.deleteDirectory(getUploadDirectory(context));
    }
}
