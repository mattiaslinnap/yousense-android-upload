package org.yousense.upload;

/**
 * Pass an instance to UploadService.init() to notify users in your app.
 * NOT thread-safe, may be called from any thread.
 */
public interface ClientUpdateNotifier {
    public void versionUpdateRequired(String url, String whatsNew);
}
