package com.ironsource.mediationsdk.sdk;

import android.app.Activity;
import org.json.JSONObject;

public abstract interface InterstitialAdapterApi
{
  public abstract void addInterstitialListener(InterstitialSmashListener paramInterstitialSmashListener);
  
  public abstract void removeInterstitialListener(InterstitialSmashListener paramInterstitialSmashListener);
  
  public abstract void initInterstitial(Activity paramActivity, String paramString1, String paramString2, JSONObject paramJSONObject, InterstitialSmashListener paramInterstitialSmashListener);
  
  public abstract void loadInterstitial(JSONObject paramJSONObject, InterstitialSmashListener paramInterstitialSmashListener);
  
  public abstract void showInterstitial(JSONObject paramJSONObject, InterstitialSmashListener paramInterstitialSmashListener);
  
  public abstract boolean isInterstitialReady(JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InterstitialAdapterApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */