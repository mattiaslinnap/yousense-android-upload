package org.yousense.upload.net;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yousense.common.AppId;
import org.yousense.common.ConfigurationException;
import org.yousense.common.Files;
import org.yousense.upload.UploadService;
import org.yousense.upload.exceptions.ClientVersionException;
import org.yousense.upload.exceptions.ServerException;

import java.io.*;
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

    public ResponseData run() throws IOException, ClientVersionException {
        Gson gson = new Gson();

        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", AppId.fullVersionString(context));
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(60*1000);
            connection.setReadTimeout(60*1000);
            connection.setDoInput(true);

            setupConnectionAndWriteBody(connection);

            // Read response
            int status = connection.getResponseCode();
            boolean error = status >= 400;
            InputStream stream;
            try {
                if (!error)
                    stream = connection.getInputStream();
                else
                    stream = connection.getErrorStream();
            } catch (IOException e) {
                throw new ServerException(status, "Error opening " + (error ? "error" : "input") + " stream from server.", e);
            }

            if (stream == null) {
                throw new ServerException(status, "Server " + (error ? "error" : "input") + " stream is null.");
            }

            String responseText = IOUtils.toString(stream, Files.UTF8);
            if (responseText == null) {
                throw new ServerException(status, "Read null from server " + (error ? "error" : "input") + " stream.");
            }
            if (!responseText.startsWith("{") || !responseText.endsWith("}")) {
                throw new ServerException(status, "Server response cannot be JSON object: " + StringUtils.abbreviate(responseText, 50));
            }

            ResponseData response;
            try {
                response = gson.fromJson(responseText, ResponseData.class);
            } catch (JsonParseException e) {
                throw new ServerException(status, "Error parsing JSON from server: " + StringUtils.abbreviate(responseText, 50), e);
            }
            if (response == null) {
                throw new ServerException(status, "Parsed JSON into null response: " + StringUtils.abbreviate(responseText, 50));
            }

            assertClientUpToDate(response);
            assertNoResponseError(response, status);

            if (error)
                throw new ServerException(status, "Parsed JSON fine, but got Server Error code.");

            return response;
        } finally {
            connection.disconnect();
        }
    }

    private void assertClientUpToDate(ResponseData response) throws ClientVersionException {
        if (response.update_required != null)
            throw new ClientVersionException(response.update_required.url, response.update_required.whats_new);
    }

    private void assertNoResponseError(ResponseData response, int statusCode) throws ServerException {
        if (response.error != null)
            throw new ServerException(statusCode, response.error);
    }
}
