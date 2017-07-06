package org.yousense.common;


import android.content.Context;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class Files {
    public static final String TAG = AppId.TAG;
    public static final String UTF8 = "UTF-8";

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
    public static synchronized File getSubdir(File outer, String name) throws IOException {
        File dir = new File(outer, name);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Throw.ioe(TAG, "Failed to create " + dir.getAbsolutePath());
            }
        }
        checkWritableDirectory(dir);
        return dir;
    }

    public static File[] listFilesSorted(File dir, FileFilter filter) throws IOException {
        checkReadableDirectory(dir);
        File[] files = dir.listFiles(filter);
        if (files == null)
            files = new File[0];
        Arrays.sort(files, new SortedByName());
        return files;
    }

    public static synchronized void moveAllFilesSortedSuffix(File dir, String fromSuffix, String toSuffix) throws IOException {
        checkWritableDirectory(dir);
        checkValidSuffix(fromSuffix);
        checkValidSuffix(toSuffix);
        for (File file : listFilesSorted(dir, new SuffixFilter(fromSuffix, true))) {
            file.renameTo(replaceSuffix(file, toSuffix));
        }
    }

    private static class SortedByName implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    public static String getSuffix(File file) throws IOException {
        if (file == null) {
            Throw.ioe(TAG, "File is null.");
            return null;  // To make static analysis happy
        }
        String path = file.getAbsolutePath();
        String suffix = "." + StringUtils.substringAfterLast(path, ".");
        checkValidSuffix(suffix);
        return suffix;
    }

    public static File appendSuffix(File file, String suffix) throws IOException {
        if (file == null) {
            Throw.ioe(TAG, "File is null.");
            return null; // To make static analysis happy
        }
        checkValidSuffix(suffix);
        if (file.getAbsolutePath().endsWith(suffix))
            Throw.ioe(TAG, "File already has suffix %s: %s", suffix, file.getAbsolutePath());
        return new File(file.getAbsolutePath() + suffix);
    }

    public static File replaceSuffix(File file, String newSuffix) throws IOException {
        if (file == null) {
            Throw.ioe(TAG, "File is null.");
            return null;  // To make static analysis happy
        }
        checkValidSuffix(newSuffix);
        String currentSuffix = getSuffix(file);
        if (newSuffix.equals(currentSuffix))
            Throw.ioe(TAG, "File already has suffix %s: %s", newSuffix, file.getAbsolutePath());
        String path = file.getAbsolutePath();
        String withoutSuffix = StringUtils.removeEnd(path, currentSuffix);
        return appendSuffix(new File(withoutSuffix), newSuffix);
    }

    public static class SuffixFilter implements FileFilter {
        private final String suffix;
        private final boolean match;
        public SuffixFilter(String suffix, boolean match) {
            this.suffix = suffix;
            this.match = match;
        }
        public boolean accept(File file) {
            return file.getName().endsWith(suffix) == match;
        }
    }

    public static void checkValidSuffix(String suffix) throws IOException {
        if (suffix == null) {
            Throw.ioe(TAG, "Suffix is null.");
        } else {
            if ("".equals(suffix))
                Throw.ioe(TAG, "Suffix is empty string.");
            if (!suffix.startsWith("."))
                Throw.ioe(TAG, "Suffix must start with a dot: \"%s\".", suffix);
            if (suffix.length() < 2)
                Throw.ioe(TAG, "Suffix \"%s\" is too short. Must be (dot)[a-z]{1,10}.", suffix);
            if (suffix.length() > 11)
                Throw.ioe(TAG, "Suffix \"%s\" is too long. Must be (dot)[a-z]{1,10}.", suffix);
            if (!StringUtils.containsOnly(suffix.substring(1, suffix.length()), "abcdefghijklmnopqrstuvwxyz"))
                Throw.ioe(TAG, "Suffix \"%s\" contains weird characters. Must be (dot)[a-z]{1,10}.", suffix);
        }
    }

    public static void checkReadableDirectory(File dir) throws IOException {
        if (dir == null) {
            Throw.ioe(TAG, "Directory object File is null.");
        } else {
            if (!dir.exists())
                Throw.ioe(TAG, "Directory does not exist: " + dir.getAbsolutePath());
            if (!dir.isDirectory())
                Throw.ioe(TAG, "Not a directory: " + dir.getAbsolutePath());
            if (!dir.canRead())
                Throw.ioe(TAG, "No read permission: " + dir.getAbsolutePath());
            if (!dir.canExecute())
                Throw.ioe(TAG, "No execute (access files) permission: " + dir.getAbsolutePath());
        }
    }

    public static void checkWritableDirectory(File dir) throws IOException {
        checkReadableDirectory(dir);
        if (!dir.canWrite())
            Throw.ioe(TAG, "No write permission: " + dir.getAbsolutePath());
    }
}
