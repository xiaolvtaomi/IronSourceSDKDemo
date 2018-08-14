package com.ironsource.mediationsdk.model;

import org.json.JSONObject;


public class ServerSegmetData {
    private String mSegmentName;
    private String mSegmentId;
    private JSONObject mCustomSegments;

    public ServerSegmetData(String name, String id, JSONObject customs) {
        this.mSegmentName = name;
        this.mSegmentId = id;
        this.mCustomSegments = customs;
    }

    public String getSegmentName() {
        return this.mSegmentName;
    }

    public String getSegmentId() {
        return this.mSegmentId;
    }

    public JSONObject getCustomSegments() {
        return this.mCustomSegments;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ServerSegmetData.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */