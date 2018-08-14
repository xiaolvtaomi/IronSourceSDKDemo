package com.ironsource.sdk.listeners.internals;

public abstract interface DSRewardedVideoListener
        extends DSAdProductListener {
    public abstract void onRVNoMoreOffers(String paramString);

    public abstract void onRVAdCredited(String paramString, int paramInt);

    public abstract void onRVShowFail(String paramString1, String paramString2);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/internals/DSRewardedVideoListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */