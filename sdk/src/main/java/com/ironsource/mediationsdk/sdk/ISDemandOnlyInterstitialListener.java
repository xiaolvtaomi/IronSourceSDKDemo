package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface ISDemandOnlyInterstitialListener
{
  public abstract void onInterstitialAdReady(String paramString);
  
  public abstract void onInterstitialAdLoadFailed(String paramString, IronSourceError paramIronSourceError);
  
  public abstract void onInterstitialAdOpened(String paramString);
  
  public abstract void onInterstitialAdClosed(String paramString);
  
  public abstract void onInterstitialAdShowSucceeded(String paramString);
  
  public abstract void onInterstitialAdShowFailed(String paramString, IronSourceError paramIronSourceError);
  
  public abstract void onInterstitialAdClicked(String paramString);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/ISDemandOnlyInterstitialListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */