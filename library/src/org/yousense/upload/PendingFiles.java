package org.yousense.upload;

import android.content.Context;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class PendingFiles {
    /**
     * Returns a hex string of the SHA1 hash of the file contents.
     */
    public static String fileSha1Hex(File file) throws IOException {
        FileInputStream fis = FileUtils.openInputStream(file);
        try {
            return DigestUtils.sha1Hex(fis);
        } finally {
            fis.close();
        }
    }

    /**
     * Copies a file to the pending files directory, queueing it for upload.
     * The original is not deleted.
     */
    public static void copyFileForUpload(Context context, File original) throws IOException {
        // TODO: check file exists and is readable for better error messaging.
        String sha1 = fileSha1Hex(original);
        File tempFile = new File(getDirectory(context, true), original.getName());
        try {
            FileUtils.copyFile(original, tempFile);
            if (!fileSha1Hex(tempFile).equals(sha1))
                throw new IOException("SHA1 mismatch after copy");
            FileUtils.moveFileToDirectory(tempFile, getDirectory(context, false), false);
        } catch (IOException e) {
            tempFile.delete();
            throw e;
        }
    }

    static File[] sortedPendingFiles(Context context) throws IOException {
        File dir = getDirectory(context, false);
        File[] files = dir.listFiles();
        if (files == null)
            throw new IOException("File.listFiles returned null, maybe not directory: " + dir.getAbsolutePath());
        Arrays.sort(files, new SortedByAbsolutePath());
        return files;
    }

    /**
     * Get a File to a writable directory containing upload files.
     * Temporary directory is used for atomically copying files to the SDCard before a rename.
     */
    private static File getDirectory(Context context, boolean temporary) throws IOException {
        String subDir = temporary ? "yousense-upload-temporary" : "yousense-upload";
        File dir = new File(context.getExternalFilesDir(null), subDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Failed to create " + dir.getAbsolutePath());
            }
        } else {
            if (!dir.isDirectory())
                throw new IOException("Not a directory: " + dir.getAbsolutePath());
            if (!dir.canRead())
                throw new IOException("No read permission: " + dir.getAbsolutePath());
            if (!dir.canWrite())
                throw new IOException("No write permission: " + dir.getAbsolutePath());
            if (!dir.canExecute())
                throw new IOException("No execute (access files) permission: " + dir.getAbsolutePath());
        }
        return dir;
    }

    private static class SortedByAbsolutePath implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getAbsolutePath().compareTo(rhs.getAbsolutePath());
        }
    }
}
