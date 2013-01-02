package org.yousense.upload;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Utils {
    /**
     * Returns a hex string of the SHA1 hash of the file contents.
     */
    public static String fileSha1Hex(File file) throws IOException {
        FileInputStream fis = FileUtils.openInputStream(file);
        try {
            // Workaround for a strange bug, where the Android-shipped Apache Commons Codec library breaks the .jar.
            return new String(Hex.encodeHex(DigestUtils.sha1(fis))).toLowerCase();
        } finally {
            fis.close();
        }
    }
}
