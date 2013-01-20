package org.yousense.common;


import android.content.Context;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Files {
    public static final String TAG = AppId.TAG;
    public static final String UTF8 = "UTF-8";

    /**
     * Returns a subdirectory of the app's data location on EXTERNAL storage.
     * Creates it if necesssary and checks for write permissions.
     */
    public static File getExternalSubdir(Context context, String name) throws IOException {
        return getSubdir(context.getExternalFilesDir(null), name);
    }

    /**
     * Returns a subdirectory of the app's data location on INTERNAL storage.
     * Creates it if necesssary and checks for write permissions.
     */
    public static File getInternalSubdir(Context context, String name) throws IOException {
        return getSubdir(context.getFilesDir(), name);
    }

    /**
     * Returns a subdirectory of outer.
     * Creates it if necesssary and checks for write permissions.
     */
    public static File getSubdir(File outer, String name) throws IOException {
        File dir = new File(outer, name);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Throw.ioe(TAG, "Failed to create " + dir.getAbsolutePath());
            }
        } else {
            if (!dir.isDirectory())
                Throw.ioe(TAG, "Not a directory: " + dir.getAbsolutePath());
            if (!dir.canRead())
                Throw.ioe(TAG, "No read permission: " + dir.getAbsolutePath());
            if (!dir.canWrite())
                Throw.ioe(TAG, "No write permission: " + dir.getAbsolutePath());
            if (!dir.canExecute())
                Throw.ioe(TAG, "No execute (access files) permission: " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static File[] listFilesSorted(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files == null)
            Throw.ioe(TAG, "File.listFiles returned null, maybe not directory: " + directory.getAbsolutePath());
        Arrays.sort(files, new SortedByName());
        return files;
    }

    public static void moveAllFilesSorted(File fromDirectory, File toDirectory) throws IOException {
        if (!fromDirectory.isDirectory())
            Throw.ioe(TAG, "Cannot move files from a non-directory: " + fromDirectory.getAbsolutePath());
        if (!toDirectory.isDirectory())
            Throw.ioe(TAG, "Cannot move files to a non-directory: " + toDirectory.getAbsolutePath());

        for (File file : listFilesSorted(fromDirectory)) {
            FileUtils.moveFileToDirectory(file, toDirectory, false);
        }
    }

    private static class SortedByName implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}