package org.yousense.common;


import android.content.Context;
import org.apache.commons.io.FileUtils;
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
    public static synchronized File getSubdir(File outer, String name) throws IOException {
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

    public static File[] listFilesSorted(File directory, FileFilter filter) throws IOException {
        File[] files = directory.listFiles(filter);
        if (files == null)
            Throw.ioe(TAG, "File.listFiles returned null, maybe not directory: " + directory.getAbsolutePath());
        Arrays.sort(files, new SortedByName());
        return files;
    }

    public static synchronized void moveAllFilesSortedSuffix(File directory, String fromSuffix, String toSuffix) throws IOException {
        if (!directory.isDirectory())
            Throw.ioe(TAG, "Cannot move files in a non-directory: " + directory.getAbsolutePath());
        checkValidSuffix(fromSuffix);
        checkValidSuffix(toSuffix);

        for (File file : listFilesSorted(directory, new SuffixFilter(fromSuffix, true))) {
            FileUtils.moveFile(file, replaceSuffix(file, toSuffix));
        }
    }

    private static class SortedByName implements Comparator<File> {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    public static String getSuffix(File file) throws IOException {
        if (file == null)
            Throw.ioe(TAG, "File is null: %s", file.getAbsolutePath());
        String path = file.getAbsolutePath();
        String suffix = "." + StringUtils.substringAfterLast(path, ".");
        checkValidSuffix(suffix);
        return suffix;
    }

    public static File appendSuffix(File file, String suffix) throws IOException {
        if (file == null)
            Throw.ioe(TAG, "File is null: %s", file.getAbsolutePath());
        checkValidSuffix(suffix);
        if (file.getAbsolutePath().endsWith(suffix))
            Throw.ioe(TAG, "File already has suffix %s: %s", suffix, file.getAbsolutePath());
        return new File(file.getAbsolutePath() + suffix);
    }

    public static File replaceSuffix(File file, String newSuffix) throws IOException {
        if (file == null)
            Throw.ioe(TAG, "File is null: %s", file.getAbsolutePath());
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
        if (suffix == null)
            Throw.ioe(TAG, "Suffix is null.");
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
