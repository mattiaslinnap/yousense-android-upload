package org.yousense.upload.net;

import android.content.Context;
import org.apache.commons.io.FileUtils;
import org.yousense.upload.AppId;
import org.yousense.upload.InstallId;
import org.yousense.upload.UploadFolder;
import org.yousense.upload.exceptions.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class FileRequest extends BaseRequest {

    File upload;

    public FileRequest(Context context, File upload) throws ConfigurationException {
        super(context, String.format("%d/file/%s/%s/", AppId.UPLOAD_LIBRARY_VERSION_CODE, AppId.appId(context), InstallId.installId(context)));
        this.upload = upload;
    }

    @Override
    void setupConnectionAndWriteBody(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode((int) upload.length());
        connection.setRequestProperty("Name", upload.getName());
        connection.setRequestProperty("Size", "" + upload.length());
        connection.setRequestProperty("Sha1", UploadFolder.sha1Hex(upload));

        // Write file into POST request
        FileUtils.copyFile(upload, connection.getOutputStream());
    }


}