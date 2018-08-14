package com.ironsource.mediationsdk.logger;

import org.json.JSONException;
import org.json.JSONObject;


class ServerLogEntry {
    private IronSourceLogger.IronSourceTag mTag;
    private String mTimetamp;
    private String mMessage;
    private int mLogLevel;

    public ServerLogEntry(IronSourceLogger.IronSourceTag tag, String timestamp, String message, int level) {
        this.mTag = tag;
        this.mTimetamp = timestamp;
        this.mMessage = message;
        this.mLogLevel = level;
    }


    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("timestamp", this.mTimetamp);
            result.put("tag", this.mTag);
            result.put("level", this.mLogLevel);
            result.put("message", this.mMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }


    public int getLogLevel() {
        return this.mLogLevel;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/ServerLogEntry.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */