package org.yousense.common;

import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {
    public static final String TAG = AppId.TAG;
    public static final int GZIP_ATTEMPTS = 3;  // How many times gzipping is attempted for a single call. It sometimes fails.

    private static TestCallback doNothing = new TestCallback();
    /**
     * Gzips the given file and deletes the original.
     * The returned filename has a .gz suffix.
     *
     * NOT thread-safe.
     */
    public static File gzip(File file) throws IOException {
        return testableGzip(file, doNothing);
    }

    /**
     * Returns true if the uncompressed file and the gzipped file have the same contents (apart from compression).
     */
    public static boolean contentEqualsGzip(File uncompressed, File gzipped) throws IOException {
        if (uncompressed.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to compare an uncompressed file that ends with .gz: %s", uncompressed.getAbsolutePath());
        if (!gzipped.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to compare a gzip file that does not end with .gz: %s", gzipped.getAbsolutePath());
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

    /**
     * Reads the entire contents of a gzipped file into memory as a String.
     * UTF-8 encoding is assumed.
     * WARNING: Many Android versions have a low, <16MB memory limit for apps. Do not use on big files.
     */
    public static String readToString(File gzipped) throws IOException {
        if (!gzipped.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to read a gzip file that does not end with .gz: %s", gzipped.getAbsolutePath());
        StringWriter contents = new StringWriter();
        GZIPInputStream stream = new GZIPInputStream(new FileInputStream(gzipped));
        IOUtils.copy(stream, contents, Files.UTF8);
        stream.close();
        return contents.toString();
    }

    /**
     * Actual gzip code. Callback is called right before the gzipping and checks are done, to enable simulating failures.
     */
    static File testableGzip(File file, TestCallback callback) throws IOException {
        if (file.getName().endsWith(".gz"))
            Throw.ioe(TAG, "Refusing to gzip a file that already ends with .gz: %s", file.getAbsolutePath());

        File gzipFile = new File(file.getAbsolutePath() + ".gz");
        for (int i = 0; i < GZIP_ATTEMPTS; ++i) {
            callback.openGzip();
            GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(gzipFile));
            try {
            	try {
                    callback.copy();
            		FileUtils.copyFile(file, gzipStream);
            	} finally {
                    callback.closeGzip();
            		gzipStream.close();
            	}

                callback.compare();
                if (!contentEqualsGzip(file, gzipFile)) {
                    Throw.ioe(TAG, "Gzip output does not match original on attempt %d", i + 1);
                }

                // All ok. Delete original.
                file.delete();  // TODO: failure is ignored. But if the original survives, it is presumed to be re-gzipped and then deleted later.
                return gzipFile;
            } catch (IOException e) {
                Log.e(TAG, String.format("Failed attempt %d of gzipping %s", i + 1, file.getAbsolutePath()), e);
                gzipFile.delete();  // TODO: failure is ignored. But if the gzipped survives, it is presumed to be overwritten by a later gzip call.
            }
        }
        // Failed.
        Throw.ioe(TAG, "Failed all %d attempts to gzip %s", GZIP_ATTEMPTS, file.getAbsolutePath());
        return null; // never reached, but to make compiler happy.
    }

    static class TestCallback {
        int attempts;  // This is not updated unless a child class overrides callback methods.
        void openGzip() throws IOException {}
        void copy() throws IOException {}
        void closeGzip() throws IOException {}
        void compare() throws IOException {}
    }
}
