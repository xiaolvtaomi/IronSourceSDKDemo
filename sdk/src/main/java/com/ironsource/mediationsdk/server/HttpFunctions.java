package com.ironsource.mediationsdk.server;

import android.text.TextUtils;

import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.IronSourceObject.IResponseListener;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpFunctions {
    private static final int SERVER_REQUEST_TIMEOUT = 15000;
    private static final String SERVER_REQUEST_GET_METHOD = "GET";
    private static final String SERVER_REQUEST_POST_METHOD = "POST";
    private static final String SERVER_REQUEST_ENCODING = "UTF-8";
    public static final String ERROR_PREFIX = "ERROR:";
    private static final String SERVER_BAD_REQUEST_ERROR = "Bad Request - 400";

    public static String getStringFromURL(String link)
            throws Exception {
        return getStringFromURL(link, null);
    }

    public static String getStringFromURL(String link, IronSourceObject.IResponseListener listener) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL requestURL = new URL(link);

            conn = (HttpURLConnection) requestURL.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 400) {
                if (listener != null) {
                    listener.onUnrecoverableError("Bad Request - 400");
                }
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String result = stringBuilder.toString();
            String str2;
            if (TextUtils.isEmpty(result)) {
                return null;
            }
            return result;
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null)
                conn.disconnect();
            if (reader != null)
                reader.close();
        }
    }

    public static boolean getStringFromPostWithAutho(String url, String json, String userName, String password) {
        OutputStream os = null;
        HttpURLConnection conn = null;
        try {
            URL requestURL = new URL(url);

            String authorizationString = IronSourceUtils.getBase64Auth(userName, password);

            conn = (HttpURLConnection) requestURL.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authorizationString);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.write(json);
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();

            return responseCode == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/server/HttpFunctions.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */