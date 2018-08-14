package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.IronSourceBannerLayout;
import org.json.JSONObject;

public abstract interface BannerAdapterApi
{
  public abstract void loadBanner(IronSourceBannerLayout paramIronSourceBannerLayout, JSONObject paramJSONObject, BannerSmashListener paramBannerSmashListener);
  
  public abstract void destroyBanner(JSONObject paramJSONObject);
  
  public abstract void reloadBanner(JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/BannerAdapterApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */