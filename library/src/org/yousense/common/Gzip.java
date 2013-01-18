package org.yousense.common;

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

    public static final int GZIP_ATTEMPTS = 2;  // How many times gzipping is attempted for a single call. It sometimes fails.

    /**
     * Gzips the given file and deletes the original.
     * The returned filename has a .gz suffix.
     */
    public static File gzip(File file) throws IOException {
        if (file.getName().endsWith(".gz"))
            throw new IOException("Refusing to gzip a file that already ends with .gz: " + file.getAbsolutePath());

        File gzipFile = new File(file.getAbsolutePath() + ".gz");
        for (int i = 0; i < GZIP_ATTEMPTS; ++i) {
            GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(gzipFile));
            try {
                FileUtils.copyFile(file, gzipStream);
                gzipStream.close();

                if (!contentEqualsGzip(file, gzipFile))
                    throw new IOException("Gzip failed, contents mismatch.");

                // All ok. Delete original.
                file.delete();
                return gzipFile;
            } catch (IOException e) {
                // TODO: Log error.
                gzipFile.delete();
            }
        }
        // Failed.
        throw new IOException(String.format("Gzipping failed after %d attempts: %s", GZIP_ATTEMPTS, file.getAbsolutePath()));
    }

    public static boolean contentEqualsGzip(File uncompressed, File gzipped) throws IOException {
        if (uncompressed.getName().endsWith(".gz"))
            throw new IOException("Refusing to compare an uncompressed file that ends with .gz: " + uncompressed.getAbsolutePath());
        if (!gzipped.getName().endsWith(".gz"))
            throw new IOException("Refusing to compare an gzip file that does not end with .gz: " + gzipped.getAbsolutePath());
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
