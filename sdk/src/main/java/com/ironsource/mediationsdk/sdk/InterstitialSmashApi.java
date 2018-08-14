package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

public abstract interface InterstitialSmashApi
{
  public abstract void setInterstitialManagerListener(InterstitialManagerListener paramInterstitialManagerListener);
  
  public abstract void initInterstitial(Activity paramActivity, String paramString1, String paramString2);
  
  public abstract void loadInterstitial();
  
  public abstract void showInterstitial();
  
  public abstract boolean isInterstitialReady();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InterstitialSmashApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */