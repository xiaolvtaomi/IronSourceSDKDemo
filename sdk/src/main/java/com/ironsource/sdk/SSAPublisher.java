package com.ironsource.sdk;

import android.app.Activity;

import com.ironsource.sdk.listeners.OnInterstitialListener;
import com.ironsource.sdk.listeners.OnOfferWallListener;
import com.ironsource.sdk.listeners.OnRewardedVideoListener;

import java.util.Map;

import org.json.JSONObject;

public abstract interface SSAPublisher {
    public abstract void initOfferWall(String paramString1, String paramString2, Map<String, String> paramMap, OnOfferWallListener paramOnOfferWallListener);

    public abstract void showOfferWall(Map<String, String> paramMap);

    public abstract void getOfferWallCredits(String paramString1, String paramString2, OnOfferWallListener paramOnOfferWallListener);

    public abstract void initRewardedVideo(String paramString1, String paramString2, String paramString3, Map<String, String> paramMap, OnRewardedVideoListener paramOnRewardedVideoListener);

    public abstract void showRewardedVideo(JSONObject paramJSONObject);

    public abstract void initInterstitial(String paramString1, String paramString2, String paramString3, Map<String, String> paramMap, OnInterstitialListener paramOnInterstitialListener);

    public abstract void loadInterstitial(JSONObject paramJSONObject);

    public abstract void showInterstitial(JSONObject paramJSONObject);

    public abstract boolean isInterstitialAdAvailable(String paramString);

    public abstract void release(Activity paramActivity);

    public abstract void onPause(Activity paramActivity);

    public abstract void onResume(Activity paramActivity);

    public abstract void setMediationState(String paramString1, String paramString2, int paramInt);

    public abstract void updateConsentInfo(JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/SSAPublisher.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */