package org.yousense.eventlog.data;

import android.content.Context;
import org.yousense.common.AppId;
import org.yousense.common.Counter;
import org.yousense.common.RestartCounter;
import org.yousense.common.UserId;

import java.io.IOException;

public class HeaderData {
    // App and user ids.
    public String appid;
    public String userid;

    // App restart and file counters.
    public long counter_restart;
    public long counter_file;

    // App version that created this file (may be different from version that uploads!)
    public int app_version_code;
    public String app_version_name;

    // Device identifiers from Build.
    public BuildData build;

    public HeaderData(Context context) throws IOException {
        this.appid = AppId.appId(context);
        this.userid = UserId.userId(context);

        this.counter_restart = RestartCounter.getValue(context);
        this.counter_file = Counter.getNext(context, "eventlog_file");

        this.app_version_code = AppId.versionCode(context);
        this.app_version_name = AppId.versionName(context);

        this.build = new BuildData();
    }
}
