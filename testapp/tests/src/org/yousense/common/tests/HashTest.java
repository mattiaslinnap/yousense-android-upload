package org.yousense.common.tests;

import android.test.AndroidTestCase;
import org.yousense.common.Hash;

public class HashTest extends AndroidTestCase {

    public void testSha1Hex() {
        assertEquals("8843d7f92416211de9ebb963ff4ce28125932878", Hash.sha1Hex("foobar"));
    }

}
