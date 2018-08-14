package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.RewardedVideoSmash;
import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface RewardedVideoManagerListener {
    public abstract void onRewardedVideoAdShowFailed(IronSourceError paramIronSourceError, RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdOpened(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdClosed(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAvailabilityChanged(boolean paramBoolean, RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdStarted(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdEnded(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdRewarded(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdClicked(RewardedVideoSmash paramRewardedVideoSmash);

    public abstract void onRewardedVideoAdVisible(RewardedVideoSmash paramRewardedVideoSmash);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/RewardedVideoManagerListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */