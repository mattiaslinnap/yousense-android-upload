package org.yousense.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

public class UserId {
    public static final String PREFS_FILE = "identity";
    public static final String INSTALL_ID = "install_id";
    private static final int INSTALL_ID_RANDOM_HEX_LENGTH = 12;
    private static final String INSTALL_ID_PREFIX = "cafe";

    private static String cachedAndroidId;
    private static String cachedInstallId;

    public static synchronized String androidId(Context context) {
        if (cachedAndroidId == null) {
            cachedAndroidId = getAndroidId(context);
        }
        return cachedAndroidId;
    }

    public static synchronized String installId(Context context) {
        if (cachedInstallId == null) {
            // Not cached in memory.
            String storedInstallId = readInstallId(context);
            if (storedInstallId == null) {
                writeRandomInstallId(context);
                storedInstallId = readInstallId(context);
            }
            cachedInstallId = storedInstallId;
        }
        return cachedInstallId;
    }

    private static synchronized String getAndroidId(Context context) {
        String aid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Known duplicate ID: http://code.google.com/p/android/issues/detail?id=10603
        if (aid == null || "9774d56d682e549c".equals(aid)) {
            return "0000000000000000";
        } else {
            return aid.toLowerCase();
        }
    }

    private static void writeRandomInstallId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        byte[] random = new byte[INSTALL_ID_RANDOM_HEX_LENGTH / 2];
        new SecureRandom().nextBytes(random);
        String instid = (INSTALL_ID_PREFIX + Hex.encodeHex(random)).toLowerCase();
        editor.putString(INSTALL_ID, instid);
        editor.commit();
    }

    static String readInstallId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
        return prefs.getString(INSTALL_ID, null);
    }

}
