package com.ironsource.mediationsdk.model;


public class ApplicationEvents {
    private boolean mSendUltraEvents;

    private boolean mSendEventsToggle;

    private String mServerEventsURL;

    private String mEventsType;

    private int mBackupThreshold;

    private int mMaxNumberOfEvents;
    private int mMaxEventsPerBatch;
    private int[] mOptOutEvents;

    public ApplicationEvents() {
    }

    public ApplicationEvents(boolean sendUltraEvents, boolean sendEventsToggle, String serverEventsURL, String serverEventsType, int backupThreshold, int maxNumberOfEvents, int maxEventsPerBatch, int[] optOutEvents) {
        this.mSendUltraEvents = sendUltraEvents;
        this.mSendEventsToggle = sendEventsToggle;
        this.mServerEventsURL = serverEventsURL;
        this.mEventsType = serverEventsType;
        this.mBackupThreshold = backupThreshold;
        this.mMaxNumberOfEvents = maxNumberOfEvents;
        this.mMaxEventsPerBatch = maxEventsPerBatch;
        this.mOptOutEvents = optOutEvents;
    }

    public boolean isUltraEventsEnabled() {
        return this.mSendUltraEvents;
    }

    public boolean isEventsEnabled() {
        return this.mSendEventsToggle;
    }

    public String getEventsURL() {
        return this.mServerEventsURL;
    }

    public String getEventsType() {
        return this.mEventsType;
    }

    public int getEventsBackupThreshold() {
        return this.mBackupThreshold;
    }

    public int getMaxNumberOfEvents() {
        return this.mMaxNumberOfEvents;
    }

    public int getMaxEventsPerBatch() {
        return this.mMaxEventsPerBatch;
    }

    public int[] getOptOutEvents() {
        return this.mOptOutEvents;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ApplicationEvents.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */