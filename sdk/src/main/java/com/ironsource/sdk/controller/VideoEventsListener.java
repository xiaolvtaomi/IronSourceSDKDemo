package com.ironsource.sdk.controller;

public abstract interface VideoEventsListener {
    public abstract void onVideoStarted();

    public abstract void onVideoPaused();

    public abstract void onVideoResumed();

    public abstract void onVideoEnded();

    public abstract void onVideoStopped();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/VideoEventsListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */