package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface BannerListener {
    public abstract void onBannerAdLoaded();

    public abstract void onBannerAdLoadFailed(IronSourceError paramIronSourceError);

    public abstract void onBannerAdClicked();

    public abstract void onBannerAdScreenPresented();

    public abstract void onBannerAdScreenDismissed();

    public abstract void onBannerAdLeftApplication();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/BannerListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */