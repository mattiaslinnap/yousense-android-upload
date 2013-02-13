package org.yousense.upload.net;

import android.content.Context;
import org.apache.commons.io.FileUtils;
import org.yousense.common.AppId;
import org.yousense.common.Hash;
import org.yousense.common.UserId;
import org.yousense.common.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class FileRequest extends BaseRequest {

    File upload;

    public FileRequest(Context context, File upload) throws ConfigurationException {
        super(context, String.format("%d/file/%s/%s/", AppId.UPLOAD_LIBRARY_VERSION_CODE, AppId.appId(context), UserId.userId(context)));
        this.upload = upload;
    }

    @Override
    void setupConnectionAndWriteBody(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode((int) upload.length());
        connection.setRequestProperty("Name", upload.getName());
        connection.setRequestProperty("Size", "" + upload.length());
        connection.setRequestProperty("Sha1", Hash.sha1Hex(upload));

        // Write file into POST request
        FileUtils.copyFile(upload, connection.getOutputStream());
    }


}
