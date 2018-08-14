package com.ironsource.mediationsdk.sdk;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.ironsource.mediationsdk.BannerSmash;
import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface BannerManagerListener
{
  public abstract void onBannerAdLoaded(BannerSmash paramBannerSmash, View paramView, FrameLayout.LayoutParams paramLayoutParams, RelativeLayout.LayoutParams paramLayoutParams1);
  
  public abstract void onBannerAdLoadFailed(IronSourceError paramIronSourceError, BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdReloaded(BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdReloadFailed(IronSourceError paramIronSourceError, BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdClicked(BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdScreenDismissed(BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdScreenPresented(BannerSmash paramBannerSmash);
  
  public abstract void onBannerAdLeftApplication(BannerSmash paramBannerSmash);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/BannerManagerListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */