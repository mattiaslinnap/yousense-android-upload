package org.yousense.upload.net;

import android.content.Context;
import org.yousense.common.Files;
import org.yousense.eventlog.EventLog;
import org.yousense.upload.UploadService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StatusData {
    ArrayList<DirData> dirs;

    public StatusData(Context context) throws IOException {
        this.dirs = new ArrayList<DirData>();
        this.dirs.add(new DirData(EventLog.getLogDirectory(context)));
        this.dirs.add(new DirData(UploadService.getUploadDirectory(context)));
    }

    public static class DirData {
        String name;
        ArrayList<FileData> files;
        public DirData(File dir) throws IOException {
            this.name = dir.getName();
            this.files = new ArrayList<FileData>();
            for (File file : Files.listFilesSorted(dir, null)) {
                this.files.add(new FileData(file));
            }
        }
    }

    public static class FileData {
        String name;
        long size;
        public FileData(File file) {
            this.name = file.getName();
            this.size = file.length();
        }
    }
}
