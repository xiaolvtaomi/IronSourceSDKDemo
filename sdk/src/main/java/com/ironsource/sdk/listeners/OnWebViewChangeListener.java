package com.ironsource.sdk.listeners;

public abstract interface OnWebViewChangeListener {
    public abstract void onCloseRequested();

    public abstract void onOrientationChanged(String paramString, int paramInt);

    public abstract boolean onBackButtonPressed();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/OnWebViewChangeListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */