package org.yousense.common;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Helpers for Apache Commons Digest functions.
 *
 * Workaround for http://stackoverflow.com/questions/9126567/method-not-found-using-digestutils-in-android
 * Apache Commons Codec library conflicts with the Android-shipped version, and somehow the sha1Hex function breaks.
 */
public class Hash {
    public static String sha1Hex(String data) {
        return new String(Hex.encodeHex(DigestUtils.sha1(data))).toLowerCase();
    }

    public static String sha1Hex(String data, String salt) {
        return new String(Hex.encodeHex(DigestUtils.sha1(salt + data + salt))).toLowerCase();
    }

    public static String sha1Hex(File file) throws IOException {
        FileInputStream fis = FileUtils.openInputStream(file);
        try {
            return new String(Hex.encodeHex(DigestUtils.sha1(fis))).toLowerCase();
        } finally {
            fis.close();
        }
    }
}
