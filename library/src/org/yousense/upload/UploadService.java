package org.yousense.upload;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import org.yousense.upload.org.yousense.upload.net.FileRequest;
import org.yousense.upload.org.yousense.upload.net.StatusRequest;
import org.yousense.upload.org.yousense.upload.net.UploadException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
                File[] pending = PendingFiles.sortedPendingFiles(this);
                new StatusRequest(this, pending).run();

                for (File file : pending) {
                    new FileRequest(this, file).run();
                    file.delete();
                }
            }
        } catch (IOException e) {
            // TODO: handle errors
            // TODO: distinguish bad URLs and other IO errors
        } catch (UploadException e) {
            // TODO: handle errors
        } catch (ManifestException e) {
            // TODO: handle errors
        } finally {
            status = Status.IDLE;
        }
    }

    public static URL baseUrlFromAndroidManifest(Context context) throws ManifestException {
        try {
            ComponentName myself = new ComponentName(context, UploadService.class);
            ServiceInfo info = context.getPackageManager().getServiceInfo(myself, PackageManager.GET_META_DATA);
            if (info == null)
                throw new ManifestException("Could not read Service info from AndroidManifest.xml.");
            Bundle metadata = info.metaData;
            if (metadata == null)
                throw new ManifestException("Could not find Service meta-data in AndroidManifest.xml.");
            String baseUrl = metadata.getString("base_url");
            if (baseUrl == null)
                throw new ManifestException("Could not find Service meta-data with name base_url in AndroidManifest.xml.");
            // Test that URL is valid before the first request is made.
            if (baseUrl.endsWith("/"))
                throw new ManifestException("Service base_url in AndroidManifest.xml must not end with a \"/\".");
            URL url = new URL(baseUrl);
            return url;
        } catch (PackageManager.NameNotFoundException e) {
            throw new ManifestException("Could not find Service in AndroidManifest.xml.", e);
        } catch (MalformedURLException e) {
            throw new ManifestException("Service base_url in AndroidManifest.xml is not a valid URL.", e);
        }
    }
}
