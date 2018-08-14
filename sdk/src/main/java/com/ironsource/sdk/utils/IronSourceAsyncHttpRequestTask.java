package com.ironsource.sdk.utils;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;


public class IronSourceAsyncHttpRequestTask
        extends AsyncTask<String, Integer, Integer> {
    protected Integer doInBackground(String... urls) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return Integer.valueOf(1);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/IronSourceAsyncHttpRequestTask.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */