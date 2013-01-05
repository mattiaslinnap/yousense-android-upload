package org.yousense.upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;


/**
 * A class to identify app installs.
 *
 * The ID is ANDROID_ID if it exists and is not known to be invalid.
 * The ID is 0000 + 12 random hex characters (64 bits) otherwise.
 *
 * The install ID is saved on first app launch to SharedPreferences.
 */
public class InstallId {

	private static final String PREFS_FILE = "identity";
	private static final String INSTALL_ID = "installid";
	private static final int INSTALL_ID_RANDOM_LENGTH = 12;
	
	private static String cachedInstallId;	
	
	public static synchronized String installId(Context context) {
		if (cachedInstallId == null) {
			// No id cached in memory. Try reading from disk.
			String storedDeviceId = readInstallId(context);
			if (storedDeviceId == null) {
				// No stored device id exists yet.
				writeRandomInstallId(context);
				storedDeviceId = readInstallId(context);
			}
			cachedInstallId = storedDeviceId;
		}
		return cachedInstallId;
    }
	
	private static void writeRandomInstallId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
		SharedPreferences.Editor editor = prefs.edit();
        editor.putString(INSTALL_ID, generateInstallId(context));
		editor.commit();
	}
	
	private static String readInstallId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
		return prefs.getString(INSTALL_ID, null);		
	}

    private static String generateInstallId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Known duplicate ID: http://code.google.com/p/android/issues/detail?id=10603
        if (androidId == null || "9774d56d682e549c".equals(androidId)) {
            byte[] random = new byte[INSTALL_ID_RANDOM_LENGTH / 2];
            new SecureRandom().nextBytes(random);
            return ("0000" + new String(Hex.encodeHex(random))).toLowerCase();
        } else {
            return androidId.toLowerCase();
        }
    }
}
