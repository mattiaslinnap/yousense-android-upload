package org.yousense.upload.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import com.google.gson.Gson;
import org.yousense.common.AppId;
import org.yousense.common.ConfigurationException;
import org.yousense.upload.UploadService;
import org.yousense.upload.exceptions.ServerUnhappyException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BaseRequest {
    private Context context;
    private URL url;

    public BaseRequest(Context context, String relativeUrl) throws ConfigurationException {
        this.context = context;
        try {
            this.url = new URL(UploadService.getBaseUrl() + relativeUrl);
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Base + relative URL is invalid.", e);
        }
    }

    abstract void setupConnectionAndWriteBody(HttpURLConnection connection) throws IOException;

    public ResponseData run() throws IOException, ServerUnhappyException {
        Gson gson = new Gson();

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", AppId.fullVersionString(context));
            connection.setConnectTimeout(60*1000);
            connection.setReadTimeout(60*1000);
            connection.setDoInput(true);

            setupConnectionAndWriteBody(connection);

            // Read response
            Reader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
            ResponseData response = gson.fromJson(reader, ResponseData.class);
            response.assertSuccess();
            return response;
        } finally {
            connection.disconnect();
        }
    }
}
