package org.yousense.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.util.Log;

public class ManifestInfo {
    public static final String TAG = AppId.TAG;

    public static PackageInfo info(Context context) {
        if (context == null)
            throw new IllegalArgumentException("Context must not be null.");
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES);
        } catch (PackageManager.NameNotFoundException e) {
            Log.wtf(AppId.TAG, "Cannot find own package info.", e);
            return null;
        }
    }

    public static boolean hasService(Context context, String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null.");
        PackageInfo info = info(context);
        if (info != null) {
            if (info.services != null) {
                for (ServiceInfo service : info.services) {
                    if (name.equals(service.name))
                        return true;
                }
            }
        }
        return false;
    }
}
