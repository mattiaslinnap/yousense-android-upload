package org.yousense.eventlog;

import android.content.Context;
import android.os.Build;
import org.yousense.upload.AppId;
import org.yousense.upload.UserId;

public class HeaderData {
    // App and user ids.
    String appid;
    String androidid;

    // Install, app restart and file counters.
    String installid;
    long counter_restart;
    long counter_file;

    // App version that created this file (may be different from version that uploads!)
    int app_version_code;
    String app_version_name;

    // Device identifiers from Build.
    String device_manufacturer;
    String device_model;
    String device_device;
    String device_display;
    String device_serial;
    String os_release;
    int os_sdk;

    public HeaderData(Context context, long fileCounter, long restartCounter) {
        this.appid = AppId.appId(context);
        this.androidid = UserId.androidId(context);

        this.installid = InstallId.installId(context);
        this.counter_file = fileCounter;
        this.counter_restart = restartCounter;

        this.app_version_code = AppId.versionCode(context);
        this.app_version_name = AppId.versionName(context);

        this.device_manufacturer = Build.MANUFACTURER;
        this.device_model = Build.MODEL;
        this.device_device = Build.DEVICE;
        this.device_display = Build.DISPLAY;
        this.device_serial = Build.SERIAL;
        this.os_release = Build.VERSION.RELEASE;
        this.os_sdk = Build.VERSION.SDK_INT;
    }
}
