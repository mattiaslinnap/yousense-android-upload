package org.yousense.upload.org.yousense.upload.net;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.yousense.upload.AppId;
import org.yousense.upload.ManifestException;
import org.yousense.upload.UploadService;

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

    public BaseRequest(Context context, String relativeUrl) throws ManifestException, MalformedURLException {
        this.context = context;
        this.url = new URL(UploadService.baseUrlFromAndroidManifest(context), relativeUrl);
    }

    abstract void setupConnectionAndWriteBody(HttpURLConnection connection) throws IOException;

    public ResponseData run() throws UploadException {
        Gson gson = new Gson();

        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            try {
                connection.setUseCaches(false);
                connection.setRequestProperty("User-Agent", AppId.appFullVersionString(context));
                connection.setConnectTimeout(60*1000);
                connection.setReadTimeout(60*1000);

                setupConnectionAndWriteBody(connection);

                // Read response
                Reader reader = new InputStreamReader(new BufferedInputStream(connection.getInputStream()));
                ResponseData response = gson.fromJson(reader, ResponseData.class);

                // TODO: Check response is valid, throw exceptions otherwise
                return response;
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            throw new UploadException("IO problem", e);
        } catch (JsonParseException e) {
            throw new UploadException("Malformed JSON", e);
        }
    }
}
