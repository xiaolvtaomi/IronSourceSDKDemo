package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;

public abstract interface ISDemandOnlyRewardedVideoListener
{
  public abstract void onRewardedVideoAdOpened(String paramString);
  
  public abstract void onRewardedVideoAdClosed(String paramString);
  
  public abstract void onRewardedVideoAvailabilityChanged(String paramString, boolean paramBoolean);
  
  public abstract void onRewardedVideoAdRewarded(String paramString, Placement paramPlacement);
  
  public abstract void onRewardedVideoAdShowFailed(String paramString, IronSourceError paramIronSourceError);
  
  public abstract void onRewardedVideoAdClicked(String paramString, Placement paramPlacement);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/ISDemandOnlyRewardedVideoListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */