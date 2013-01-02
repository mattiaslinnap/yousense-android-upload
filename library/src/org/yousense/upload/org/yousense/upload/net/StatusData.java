package org.yousense.upload.org.yousense.upload.net;

import java.io.File;

public class StatusData {

    public String[] filenames;

    public StatusData(File[] pendingFiles) {
        filenames = new String[pendingFiles.length];
        for (int i = 0; i < pendingFiles.length; ++i)
            filenames[i] = pendingFiles[i].getAbsolutePath();
    }
}
