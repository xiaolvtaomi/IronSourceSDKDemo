package com.ironsource.mediationsdk;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;

public class IronSourceBannerLayout
        extends FrameLayout {
    private View mBannerView;
    private EBannerSize mSize;
    private String mPlacementName;
    private Activity mActivity;
    private boolean isDestroyed = false;
    private boolean mIsBannerDisplayed = false;
    private BannerListener mBannerListener;

    public IronSourceBannerLayout(Activity activity, EBannerSize size, BannerManagerListener bannerManager) {
        super(activity);
        this.mActivity = activity;
        if (size == null) {
            size = EBannerSize.BANNER;
        }
        this.mSize = size;
    }

    protected void destroyBanner() {
        this.isDestroyed = true;
        this.mBannerListener = null;
        this.mActivity = null;
        this.mSize = null;
        this.mPlacementName = null;
        this.mBannerView = null;
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public View getBannerView() {
        return this.mBannerView;
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    public EBannerSize getSize() {
        return this.mSize;
    }

    public String getPlacementName() {
        return this.mPlacementName;
    }

    public void setPlacementName(String placementName) {
        this.mPlacementName = placementName;
    }


    public void setBannerListener(BannerListener listener) {
        String logMessage = "setBannerListener()";
        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        this.mBannerListener = listener;
    }


    public void removeBannerListener() {
        String logMessage = "removeBannerListener()";
        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        this.mBannerListener = null;
    }

    public BannerListener getBannerListener() {
        return this.mBannerListener;
    }

    void sendBannerAdLoaded(BannerSmash smash) {
        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.INTERNAL, "onBannerAdLoaded() | internal | adapter: " + smash
                .getName(), 0);


        if ((this.mBannerListener != null) && (!this.mIsBannerDisplayed)) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onBannerAdLoaded()", 1);
            this.mBannerListener.onBannerAdLoaded();
        }

        this.mIsBannerDisplayed = true;
    }

    void sendBannerAdLoadFailed(IronSourceError error) {
        if (this.mIsBannerDisplayed) {
            this.mBannerListener.onBannerAdLoadFailed(error);
            return;
        }

        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.INTERNAL, "onBannerAdLoadFailed() | internal | " + error, 0);


        try {
            if (this.mBannerView != null) {
                removeView(this.mBannerView);
                this.mBannerView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.mBannerListener != null) {
            this.mBannerListener.onBannerAdLoadFailed(error);
        }
    }

    void sendBannerAdClicked() {
        if (this.mBannerListener != null) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onBannerAdClicked()", 1);
            this.mBannerListener.onBannerAdClicked();
        }
    }

    void sendBannerAdScreenPresented() {
        if (this.mBannerListener != null) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onBannerAdScreenPresented()", 1);
            this.mBannerListener.onBannerAdScreenPresented();
        }
    }

    void sendBannerAdScreenDismissed() {
        if (this.mBannerListener != null) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onBannerAdScreenDismissed()", 1);
            this.mBannerListener.onBannerAdScreenDismissed();
        }
    }

    void sendBannerAdLeftApplication() {
        if (this.mBannerListener != null) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onBannerAdLeftApplication()", 1);
            this.mBannerListener.onBannerAdLeftApplication();
        }
    }

    void addViewWithFrameLayoutParams(View adView, LayoutParams layoutParams) {
        removeAllViews();
        this.mBannerView = adView;
        addView(adView, 0, layoutParams);
    }

    void addViewWithRelativeLayoutParams(View adView, RelativeLayout.LayoutParams layoutParams) {
        removeAllViews();
        this.mBannerView = adView;
        addView(adView, layoutParams);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/IronSourceBannerLayout.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */