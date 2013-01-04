package org.yousense.upload.net;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import com.google.gson.Gson;
import org.yousense.upload.AppId;
import org.yousense.upload.exceptions.ConfigurationException;
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
            this.url = new URL(baseUrlFromAndroidManifest(context) + relativeUrl);
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

    public static String baseUrlFromAndroidManifest(Context context) throws ConfigurationException {
        try {
            ComponentName myself = new ComponentName(context, UploadService.class);
            ServiceInfo info = context.getPackageManager().getServiceInfo(myself, PackageManager.GET_META_DATA);
            if (info == null)
                throw new ConfigurationException("Could not read Service info from AndroidManifest.xml.");
            Bundle metadata = info.metaData;
            if (metadata == null)
                throw new ConfigurationException("Could not find Service meta-data in AndroidManifest.xml.");
            String baseUrl = metadata.getString("base_url");
            if (baseUrl == null)
                throw new ConfigurationException("Could not find Service meta-data with name base_url in AndroidManifest.xml.");
            // Test that partial base_url URL is valid before the first request is made.
            new URL(baseUrl);
            if (!baseUrl.endsWith("/"))
                throw new ConfigurationException("Service base_url in AndroidManifest.xml must end with a \"/\".");
            return baseUrl;
        } catch (PackageManager.NameNotFoundException e) {
            throw new ConfigurationException("Could not find Service in AndroidManifest.xml.", e);
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Service base_url in AndroidManifest.xml is not a valid URL.", e);
        }
    }
}
