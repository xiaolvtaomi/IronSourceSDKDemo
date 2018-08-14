package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

import org.json.JSONObject;

public abstract interface RewardedVideoAdapterApi {
    public abstract void addRewardedVideoListener(RewardedVideoSmashListener paramRewardedVideoSmashListener);

    public abstract void removeRewardedVideoListener(RewardedVideoSmashListener paramRewardedVideoSmashListener);

    public abstract void initRewardedVideo(Activity paramActivity, String paramString1, String paramString2, JSONObject paramJSONObject, RewardedVideoSmashListener paramRewardedVideoSmashListener);

    public abstract void fetchRewardedVideo(JSONObject paramJSONObject);

    public abstract void showRewardedVideo(JSONObject paramJSONObject, RewardedVideoSmashListener paramRewardedVideoSmashListener);

    public abstract boolean isRewardedVideoAvailable(JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/RewardedVideoAdapterApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */