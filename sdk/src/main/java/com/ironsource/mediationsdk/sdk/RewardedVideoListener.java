package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;

public abstract interface RewardedVideoListener {
    public abstract void onRewardedVideoAdOpened();

    public abstract void onRewardedVideoAdClosed();

    public abstract void onRewardedVideoAvailabilityChanged(boolean paramBoolean);

    public abstract void onRewardedVideoAdStarted();

    public abstract void onRewardedVideoAdEnded();

    public abstract void onRewardedVideoAdRewarded(Placement paramPlacement);

    public abstract void onRewardedVideoAdShowFailed(IronSourceError paramIronSourceError);

    public abstract void onRewardedVideoAdClicked(Placement paramPlacement);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/RewardedVideoListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */