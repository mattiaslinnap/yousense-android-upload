package org.yousense.common;

import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class Gzip {
    public static final String TAG = AppId.TAG;
    public static final int GZIP_ATTEMPTS = 2;  // How many times gzipping is attempted for a single call. It sometimes fails.

    /**
     * Gzips the given file and deletes the original.
     * The returned filename has a .gz suffix.
     */
    public static File gzip(File file) throws IOException {
        if (file.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to gzip a file that already ends with .gz: %s", file.getAbsolutePath());

        File gzipFile = new File(file.getAbsolutePath() + ".gz");
        for (int i = 0; i < GZIP_ATTEMPTS; ++i) {
            GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(gzipFile));
            try {
            	try {
            		FileUtils.copyFile(file, gzipStream);
            	} finally {
            		gzipStream.close();
            	}

                if (!contentEqualsGzip(file, gzipFile)) {
                    Throw.ioe(TAG, "Gzip output does not match original on attempt %d", i + 1);
                }

                // All ok. Delete original.
                file.delete();
                return gzipFile;
            } catch (IOException e) {
                Log.e(TAG, String.format("Failed attempt %d of gzipping %s", i + 1, file.getAbsolutePath()), e);
                gzipFile.delete();
            }
        }
        // Failed.
        Throw.ioe(TAG, "Failed all %d attempts to gzip %s", GZIP_ATTEMPTS, file.getAbsolutePath());
        return null; // never reached, but to make compiler happy.
    }

    public static boolean contentEqualsGzip(File uncompressed, File gzipped) throws IOException {
        if (uncompressed.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to compare an uncompressed file that ends with .gz: %s", uncompressed.getAbsolutePath());
        if (!gzipped.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to compare an gzip file that does not end with .gz: %s", gzipped.getAbsolutePath());
        FileInputStream uncompressedStream = new FileInputStream(uncompressed);
        GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(gzipped));
        try {
            try {
                return IOUtils.contentEquals(uncompressedStream, gzipStream);
            } finally {
                gzipStream.close();
            }
        } finally {
            uncompressedStream.close();
        }

    }
}
