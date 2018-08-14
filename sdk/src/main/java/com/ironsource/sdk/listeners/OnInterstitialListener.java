package com.ironsource.sdk.listeners;

import org.json.JSONObject;

public abstract interface OnInterstitialListener
        extends OnAdProductListener {
    public abstract void onInterstitialInitSuccess();

    public abstract void onInterstitialInitFailed(String paramString);

    public abstract void onInterstitialLoadSuccess();

    public abstract void onInterstitialLoadFailed(String paramString);

    public abstract void onInterstitialOpen();

    public abstract void onInterstitialClose();

    public abstract void onInterstitialShowSuccess();

    public abstract void onInterstitialShowFailed(String paramString);

    public abstract void onInterstitialClick();

    public abstract void onInterstitialEventNotificationReceived(String paramString, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/OnInterstitialListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */