package org.yousense.common.tests;

import android.test.AndroidTestCase;
import org.yousense.common.AppId;

public class AppIdTest extends AndroidTestCase {

    public void testAppId() {
        assertEquals("org.yousense.upload.testapp", AppId.appId(getContext()));
    }

    public void testVersionCode() {
        assertEquals(2, AppId.versionCode(getContext()));
    }

    public void testVersionName() {
        assertEquals("2.0 beta", AppId.versionName(getContext()));
    }

    public void testFullVersionString() {
        assertEquals("org.yousense.upload.testapp/2/2.0 beta", AppId.fullVersionString(getContext()));
    }
}
