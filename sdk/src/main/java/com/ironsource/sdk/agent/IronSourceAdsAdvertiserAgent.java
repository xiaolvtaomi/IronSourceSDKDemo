package com.ironsource.sdk.agent;

import android.content.Context;

import com.ironsource.sdk.SSAAdvertiser;


/**
 * @deprecated
 */
public class IronSourceAdsAdvertiserAgent
        implements SSAAdvertiser {
    private static final String TAG = "IronSourceAdsAdvertiserAgent";
    static IronSourceAdsAdvertiserAgent sInstance;

    public static synchronized IronSourceAdsAdvertiserAgent getInstance() {
        if (sInstance == null) {
            sInstance = new IronSourceAdsAdvertiserAgent();
        }
        return sInstance;
    }

    @Deprecated
    public void reportAppStarted(Context context) {
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/agent/IronSourceAdsAdvertiserAgent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */