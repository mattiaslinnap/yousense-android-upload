package org.yousense.upload.net;

import java.io.File;
import java.util.ArrayList;

public class StatusData {

    public ArrayList<FileData> pending_files;

    public StatusData(File[] pendingFiles) {
        this.pending_files = new ArrayList<FileData>();
        for (File file : pendingFiles)
            this.pending_files.add(new FileData(file));
    }

    public static class FileData {
        public String name;
        public long size;
        public FileData(File file) {
            this.name = file.getName();
            this.size = file.length();
        }
    }
}
