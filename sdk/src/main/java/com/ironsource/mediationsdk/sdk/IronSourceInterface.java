package com.ironsource.mediationsdk.sdk;

import android.content.Context;
import com.ironsource.mediationsdk.logger.LoggingApi;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.Placement;
import java.util.Map;

public abstract interface IronSourceInterface
  extends BaseApi, RewardedVideoApi, InterstitialApi, OfferwallApi, LoggingApi, RewardedInterstitialApi
{
  public abstract void removeRewardedVideoListener();
  
  public abstract void removeInterstitialListener();
  
  public abstract void removeOfferwallListener();
  
  public abstract Placement getRewardedVideoPlacementInfo(String paramString);
  
  public abstract InterstitialPlacement getInterstitialPlacementInfo(String paramString);
  
  public abstract String getAdvertiserId(Context paramContext);
  
  public abstract void shouldTrackNetworkState(Context paramContext, boolean paramBoolean);
  
  public abstract boolean setDynamicUserId(String paramString);
  
  public abstract void setAdaptersDebug(boolean paramBoolean);
  
  public abstract void setMediationType(String paramString);
  
  public abstract void setRewardedVideoServerParameters(Map<String, String> paramMap);
  
  public abstract void clearRewardedVideoServerParameters();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/IronSourceInterface.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */