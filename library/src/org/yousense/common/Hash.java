package org.yousense.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

/**
 * Helpers for Apache Commons Digest functions.
 *
 * Workaround for http://stackoverflow.com/questions/9126567/method-not-found-using-digestutils-in-android
 * Apache Commons Codec library conflicts with the Android-shipped version, and somehow the sha1Hex function breaks.
 */
public class Hash {
	
	private static String encodeHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	        sb.append(String.format("%02X", b));
	    }
	    return sb.toString();
	}
	
    public static String sha1Hex(String data) {
        return new String(encodeHex(DigestUtils.sha1(data))).toLowerCase();
    }

    public static String sha1Hex(String data, String salt) {
        return new String(encodeHex(DigestUtils.sha1(salt + data + salt))).toLowerCase();
    }

    public static String sha1Hex(File file) throws IOException {
        FileInputStream fis = FileUtils.openInputStream(file);
        try {
            return new String(encodeHex(DigestUtils.sha1(fis))).toLowerCase();
        } finally {
            fis.close();
        }
    }
}
