package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.InterstitialSmash;
import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface InterstitialManagerListener
{
  public abstract void onInterstitialInitSuccess(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialInitFailed(IronSourceError paramIronSourceError, InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdReady(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdLoadFailed(IronSourceError paramIronSourceError, InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdOpened(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdClosed(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdShowSucceeded(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdShowFailed(IronSourceError paramIronSourceError, InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdClicked(InterstitialSmash paramInterstitialSmash);
  
  public abstract void onInterstitialAdVisible(InterstitialSmash paramInterstitialSmash);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InterstitialManagerListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */