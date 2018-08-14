package com.ironsource.sdk.listeners.internals;

public abstract interface DSInterstitialListener
        extends DSAdProductListener {
    public abstract void onInterstitialLoadSuccess(String paramString);

    public abstract void onInterstitialLoadFailed(String paramString1, String paramString2);

    public abstract void onInterstitialShowSuccess(String paramString);

    public abstract void onInterstitialShowFailed(String paramString1, String paramString2);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/internals/DSInterstitialListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */