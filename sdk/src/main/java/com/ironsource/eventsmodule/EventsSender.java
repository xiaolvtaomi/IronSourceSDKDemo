package com.ironsource.eventsmodule;

import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class EventsSender
        extends AsyncTask<Object, Void, Boolean> {
    private final int SERVER_REQUEST_TIMEOUT = 15000;
    private final String SERVER_REQUEST_METHOD = "POST";
    private final String SERVER_REQUEST_ENCODING = "UTF-8";
    private final String CONTENT_TYPE_FIELD = "Content-Type";
    private final String APPLICATION_JSON = "application/json";

    private ArrayList extraData;

    private IEventsSenderResultListener mResultListener;

    public EventsSender() {
    }

    public EventsSender(IEventsSenderResultListener resultListener) {
        this.mResultListener = resultListener;
    }


    protected Boolean doInBackground(Object... objects) {
        try {
            URL requestURL = new URL((String) objects[1]);
            this.extraData = ((ArrayList) objects[2]);
            HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            writer.write((String) objects[0]);
            writer.flush();
            writer.close();

            os.close();

            int responseCode = conn.getResponseCode();

            conn.disconnect();

            return Boolean.valueOf(responseCode == 200);
        } catch (Exception e) {
        }
        return Boolean.valueOf(false);
    }


    protected void onPostExecute(Boolean success) {
        if (this.mResultListener != null) {
            this.mResultListener.onEventsSenderResult(this.extraData, success.booleanValue());
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/eventsmodule/EventsSender.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */