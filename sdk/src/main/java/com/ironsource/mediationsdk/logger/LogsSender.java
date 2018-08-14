package com.ironsource.mediationsdk.logger;

import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.mediationsdk.server.HttpFunctions;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class LogsSender
        implements Runnable {
    private final String LOG_URL = "https://mobilelogs.supersonic.com";
    private final String AUTHO_USERNAME = "mobilelogs";
    private final String AUTHO_PASSWORD = "k@r@puz";
    private ArrayList<ServerLogEntry> mLogs;

    public LogsSender(ArrayList<ServerLogEntry> logs) {
        this.mLogs = logs;
    }


    private JSONObject getJSONToSend() {
        JSONObject logContent = new JSONObject();

        try {
            logContent.put("general_properties", GeneralProperties.getProperties().toJSON());


            JSONArray logData = new JSONArray();
            for (ServerLogEntry log : this.mLogs) {
                logData.put(log.toJSON());
            }
            logContent.put("log_data", logData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return logContent;
    }


    private void sendLogs(JSONObject logContent) {
        HttpFunctions.getStringFromPostWithAutho("https://mobilelogs.supersonic.com", logContent.toString(), "mobilelogs", "k@r@puz");
    }

    public void run() {
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/LogsSender.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */