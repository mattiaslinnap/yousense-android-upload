package org.yousense.eventlog.data;

import android.os.Build;

public class BuildData {
    String manufacturer;
    String model;
    String device;
    String display;
    String serial;
    String os_release;
    int os_sdk;

    public BuildData() {
        this.manufacturer = Build.MANUFACTURER;
        this.model = Build.MODEL;
        this.device = Build.DEVICE;
        this.display = Build.DISPLAY;
        this.serial = Build.SERIAL;
        this.os_release = Build.VERSION.RELEASE;
        this.os_sdk = Build.VERSION.SDK_INT;
    }
}
