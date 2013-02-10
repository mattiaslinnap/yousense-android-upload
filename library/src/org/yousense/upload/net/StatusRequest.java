package org.yousense.upload.net;

import android.content.Context;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.yousense.common.AppId;
import org.yousense.common.UserId;
import org.yousense.upload.exceptions.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class StatusRequest extends BaseRequest {

    StatusData status;

    public StatusRequest(Context context) throws ConfigurationException, IOException {
        super(context, String.format("%d/status/%s/%s/", AppId.UPLOAD_LIBRARY_VERSION_CODE, AppId.appId(context), UserId.userId(context)));
        status = new StatusData(context);
    }

    @Override
    void setupConnectionAndWriteBody(HttpURLConnection connection) throws IOException {
        Gson gson = new Gson();
        byte[] body = gson.toJson(status).getBytes("UTF-8");

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(body.length);

        IOUtils.write(body, connection.getOutputStream());
    }
}
