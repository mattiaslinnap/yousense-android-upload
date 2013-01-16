package org.yousense.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


public class AppId {
    public static final String TAG = "yousense-common";

    public static final int UPLOAD_LIBRARY_VERSION_CODE = 2;

	public static String appId(Context context) {
		return context.getPackageName();
	}
	
	public static int versionCode(Context context) {
        try {
        	PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        	return pinfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        	// Our own package name not found??
        	Log.e(TAG, "Cannot find my own PackageInfo");
        	return 0;
        }
    }

	public static String versionName(Context context) {
        try {
        	PackageInfo pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        	return pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        	// Our own package name not found??
        	Log.e(TAG, "Cannot find my own PackageInfo");
        	return "UNKNOWN";
        }
    }
	
	public static String fullVersionString(Context context) {
		return String.format("%s/%d/%s", appId(context), versionCode(context), versionName(context));
	}
}
