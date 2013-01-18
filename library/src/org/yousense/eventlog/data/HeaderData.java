package org.yousense.eventlog.data;

import android.content.Context;
import org.yousense.common.AppId;
import org.yousense.common.UserId;

public class HeaderData {
    // App and user ids.
    String appid;
    String userid;

    // App restart and file counters.
    long counter_restart;
    long counter_file;

    // App version that created this file (may be different from version that uploads!)
    int app_version_code;
    String app_version_name;

    // Device identifiers from Build.
    BuildData build;

    public HeaderData(Context context, long fileCounter, long restartCounter) {
        this.appid = AppId.appId(context);
        this.userid = UserId.userId(context);

        this.counter_file = fileCounter;
        this.counter_restart = restartCounter;

        this.app_version_code = AppId.versionCode(context);
        this.app_version_name = AppId.versionName(context);

        this.build = new BuildData();
    }
}
