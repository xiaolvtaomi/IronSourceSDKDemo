package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface InterstitialListener
{
  public abstract void onInterstitialAdReady();
  
  public abstract void onInterstitialAdLoadFailed(IronSourceError paramIronSourceError);
  
  public abstract void onInterstitialAdOpened();
  
  public abstract void onInterstitialAdClosed();
  
  public abstract void onInterstitialAdShowSucceeded();
  
  public abstract void onInterstitialAdShowFailed(IronSourceError paramIronSourceError);
  
  public abstract void onInterstitialAdClicked();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InterstitialListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */