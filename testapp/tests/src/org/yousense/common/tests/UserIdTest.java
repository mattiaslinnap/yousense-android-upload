package org.yousense.common.tests;

import android.content.Context;
import android.provider.Settings;
import android.test.AndroidTestCase;
import android.test.MoreAsserts;
import org.yousense.common.UserId;

public class UserIdTest extends AndroidTestCase {

    Context context;

    public void setUp() {
        context = getContext();
        UserId.resetRandomId(context);
    }

    public void test32HexChars() {
        String uid = UserId.userId(context);
        assertEquals(32, uid.length());
        MoreAsserts.assertMatchesRegex("[a-z0-9]{32}", uid);
    }

    public void testStartsWithAndroidId() {
        assertEquals(
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID),
                UserId.userId(context).substring(0, 16));
    }

    public void testFullIdIsPersistent() {
        String uid = UserId.userId(context);
        for (int i = 0; i < 3; ++i) {
            UserId.clearCache();
            assertEquals(uid, UserId.userId(context));
        }
    }

    public void testRandomPartChangesWhenReset() {
        String uid = UserId.userId(context);
        UserId.resetRandomId(context);
        MoreAsserts.assertNotEqual(uid, UserId.userId(context));
    }
}
