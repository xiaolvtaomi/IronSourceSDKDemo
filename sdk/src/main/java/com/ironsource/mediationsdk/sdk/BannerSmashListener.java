package com.ironsource.mediationsdk.sdk;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface BannerSmashListener
{
  public abstract void onBannerInitSuccess();
  
  public abstract void onBannerInitFailed(IronSourceError paramIronSourceError);
  
  public abstract void onBannerAdLoaded(View paramView, FrameLayout.LayoutParams paramLayoutParams, RelativeLayout.LayoutParams paramLayoutParams1);
  
  public abstract void onBannerAdLoadFailed(IronSourceError paramIronSourceError);
  
  public abstract void onBannerAdClicked();
  
  public abstract void onBannerAdScreenPresented();
  
  public abstract void onBannerAdScreenDismissed();
  
  public abstract void onBannerAdLeftApplication();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/BannerSmashListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */