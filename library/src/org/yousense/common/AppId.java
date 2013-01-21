package org.yousense.common;

import android.content.Context;
import android.content.pm.PackageInfo;


public class AppId {
    public static final String TAG = "yousense-common";

    public static final int UPLOAD_LIBRARY_VERSION_CODE = 2;

	public static String appId(Context context) {
		return context.getPackageName();
	}
	
	public static int versionCode(Context context) {
        PackageInfo info = ManifestInfo.info(context);
        if (info != null)
            return info.versionCode;
        else
            return 0;
    }

	public static String versionName(Context context) {
        PackageInfo info = ManifestInfo.info(context);
        if (info != null)
            return info.versionName;
        else
            return "UNKNOWN";
    }
	
	public static String fullVersionString(Context context) {
		return String.format("%s/%d/%s", appId(context), versionCode(context), versionName(context));
	}
}
