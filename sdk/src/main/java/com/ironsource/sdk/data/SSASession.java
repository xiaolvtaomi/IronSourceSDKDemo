package com.ironsource.sdk.data;

import android.content.Context;

import com.ironsource.environment.ConnectivityService;
import com.ironsource.sdk.utils.SDKUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class SSASession {
    public final String SESSION_START_TIME = "sessionStartTime";
    public final String SESSION_END_TIME = "sessionEndTime";
    public final String SESSION_TYPE = "sessionType";
    public final String CONNECTIVITY = "connectivity";

    private long sessionStartTime;
    private long sessionEndTime;
    private SessionType sessionType;
    private String connectivity;

    public SSASession(Context context, SessionType type) {
        setSessionStartTime(SDKUtils.getCurrentTimeMillis().longValue());
        setSessionType(type);
        setConnectivity(ConnectivityService.getConnectionType(context));
    }

    public SSASession(JSONObject jsonObj) {
        try {
            jsonObj.get(SESSION_START_TIME);
            jsonObj.get(SESSION_END_TIME);
            jsonObj.get(SESSION_TYPE);
            jsonObj.get(CONNECTIVITY);
        } catch (JSONException localJSONException) {
        }
    }

    public void endSession() {
        setSessionEndTime(SDKUtils.getCurrentTimeMillis().longValue());
    }

    public static enum SessionType {
        launched, backFromBG;

        private SessionType() {
        }
    }

    public long getSessionStartTime() {
        return this.sessionStartTime;
    }

    public void setSessionStartTime(long sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    public long getSessionEndTime() {
        return this.sessionEndTime;
    }

    public void setSessionEndTime(long sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }

    public SessionType getSessionType() {
        return this.sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public String getConnectivity() {
        return this.connectivity;
    }

    public void setConnectivity(String connectivity) {
        this.connectivity = connectivity;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/SSASession.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */