package com.ironsource.sdk.data;

import com.ironsource.sdk.listeners.OnAdProductListener;

import java.util.Map;


public class DemandSource {
    public static final int INIT_NOT_STARTED = 0;
    public static final int INIT_PENDING = 1;
    public static final int INIT_SUCCEEDED = 2;
    public static final int INIT_FAILED = 3;
    public static final int MEDIATION_STATE_NOT_SET = -1;
    private String mName;
    private int mMediationState = -1;

    private Map<String, String> mExtraParams;

    private int mInitState;

    private boolean mAvailabilityState;
    private OnAdProductListener mListener;

    public DemandSource(String demandSourceName, Map<String, String> extraParams, OnAdProductListener listener) {
        this.mName = demandSourceName;
        this.mExtraParams = extraParams;
        this.mListener = listener;
        this.mInitState = 0;
        this.mAvailabilityState = false;
    }

    public String getDemandSourceName() {
        return this.mName;
    }

    public int getDemandSourceInitState() {
        return this.mInitState;
    }

    public boolean getAvailabilityState() {
        return this.mAvailabilityState;
    }

    public void setAvailabilityState(boolean isAvailable) {
        this.mAvailabilityState = isAvailable;
    }

    public Map<String, String> getExtraParams() {
        return this.mExtraParams;
    }

    public synchronized void setDemandSourceInitState(int initState) {
        this.mInitState = initState;
    }

    public OnAdProductListener getListener() {
        return this.mListener;
    }

    public void setMediationState(int state) {
        this.mMediationState = state;
    }

    public int getMediationState() {
        return this.mMediationState;
    }

    public boolean isMediationState(int state) {
        return this.mMediationState == state;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/DemandSource.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */