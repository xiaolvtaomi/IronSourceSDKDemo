package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface InterstitialSmashListener
  extends InterstitialListener
{
  public abstract void onInterstitialInitSuccess();
  
  public abstract void onInterstitialInitFailed(IronSourceError paramIronSourceError);
  
  public abstract void onInterstitialAdVisible();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InterstitialSmashListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */