package com.ironsource.sdk.listeners;

import com.ironsource.sdk.data.AdUnitsReady;

import org.json.JSONObject;

public abstract interface OnRewardedVideoListener
        extends OnAdProductListener {
    public abstract void onRVInitSuccess(AdUnitsReady paramAdUnitsReady);

    public abstract void onRVInitFail(String paramString);

    public abstract void onRVNoMoreOffers();

    public abstract void onRVAdCredited(int paramInt);

    public abstract void onRVAdClosed();

    public abstract void onRVAdOpened();

    public abstract void onRVShowFail(String paramString);

    public abstract void onRVAdClicked();

    public abstract void onRVEventNotificationReceived(String paramString, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/OnRewardedVideoListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */