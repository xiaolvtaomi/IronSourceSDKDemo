package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

public abstract interface RewardedVideoSmashApi {
    public abstract void setRewardedVideoManagerListener(RewardedVideoManagerListener paramRewardedVideoManagerListener);

    public abstract void initRewardedVideo(Activity paramActivity, String paramString1, String paramString2);

    public abstract void fetchRewardedVideo();

    public abstract void showRewardedVideo();

    public abstract boolean isRewardedVideoAvailable();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/RewardedVideoSmashApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */