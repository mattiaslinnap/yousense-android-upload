package org.yousense.upload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import org.apache.commons.io.FileUtils;
import org.yousense.common.*;
import org.yousense.eventlog.DebugLog;
import org.yousense.upload.exceptions.ServerUnhappyException;
import org.yousense.upload.net.FileRequest;
import org.yousense.upload.net.StatusRequest;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadService extends IntentService {
    public static final String TAG = "yousense-upload";
    public static final String ACTION_UPLOAD = "org.yousense.intent.action.UPLOAD";
    public enum Status { IDLE, UPLOADING }
    final static String TEMP_SUFFIX = ".temp";
    final static FileFilter VALID_FILTER = new Files.SuffixFilter(TEMP_SUFFIX, false);

    private static volatile Status status;
    private static String baseUrl;

    // Public API

    public static void init(String baseUrl) {
        checkBaseUrl(baseUrl);
        UploadService.baseUrl = baseUrl;
    }

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
                Throw.ioeLog(TAG, "SHA1 mismatch after copy");
            // Atomic move to remove suffix.
            tempFile.renameTo(finalFile);
        } catch (IOException e) {
            tempFile.delete();
            throw e;
        }
    }

    public static void startUpload(Context context) {
        checkManifest(context);
        checkBaseUrl(UploadService.baseUrl);
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_UPLOAD);
        context.startService(intent);
    }

    public static Status getStatus() {
        return status;
    }

    public static String getBaseUrl() {
        return baseUrl;
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
            DebugLog.eLog(TAG, "Upload failed with IOException", e);
        } catch (ConfigurationException e) {
            DebugLog.eLog(TAG, "Upload failed with ConfigurationException", e);
        } catch (ServerUnhappyException e) {
            DebugLog.eLog(TAG, "Upload failed with ServerUnhappyException", e);
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

    private static void checkManifest(Context context) {
        if (!ManifestInfo.hasService(context, "org.yousense.upload.UploadService"))
            Throw.ceLog(TAG, "You forgot to add <service android:name=\"org.yousense.upload.UploadService\"> to AndroidManifest.xml.");
    }

    public static void checkBaseUrl(String baseUrl) throws ConfigurationException {
        if (baseUrl == null)
            Throw.ceLog(TAG, "Upload base URL is null.");
        else {
            if (!baseUrl.endsWith("/"))
                Throw.ceLog(TAG, "Upload base URL must end with a /.");
            try {
                // Check that URL is valid before the first request is made.
                new URL(baseUrl);
            } catch (MalformedURLException e) {
                Throw.ceLog(TAG, "Upload base URL is malformed.");
            }
        }
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
