package com.ironsource.mediationsdk;

import android.app.Activity;

import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoSmashApi;
import com.ironsource.mediationsdk.sdk.RewardedVideoSmashListener;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;


public class RewardedVideoSmash
        extends AbstractSmash
        implements RewardedVideoSmashListener, RewardedVideoSmashApi {
    private JSONObject mRewardedVideoAdapterConfigs;
    private RewardedVideoManagerListener mRewardedVideoManagerListener;
    private String mRequestUrl;
    private int mTimeout;
    private final String REQUEST_URL_KEY = "requestUrl";

    RewardedVideoSmash(ProviderSettings adapterConfigs, int timeout) {
        super(adapterConfigs);
        this.mRewardedVideoAdapterConfigs = adapterConfigs.getRewardedVideoSettings();
        this.mMaxAdsPerIteration = this.mRewardedVideoAdapterConfigs.optInt("maxAdsPerIteration", 99);
        this.mMaxAdsPerSession = this.mRewardedVideoAdapterConfigs.optInt("maxAdsPerSession", 99);
        this.mMaxAdsPerDay = this.mRewardedVideoAdapterConfigs.optInt("maxAdsPerDay", 99);
        this.mRequestUrl = this.mRewardedVideoAdapterConfigs.optString("requestUrl");
        this.mTimeout = timeout;
    }


    void completeIteration() {
        this.mIterationShowCounter = 0;
        setMediationState(isRewardedVideoAvailable() ? MEDIATION_STATE.AVAILABLE : MEDIATION_STATE.NOT_AVAILABLE);
    }

    void startInitTimer() {
        try {
            this.mInitTimerTask = new TimerTask() {
                public void run() {
                    if (RewardedVideoSmash.this.mRewardedVideoManagerListener != null) {
                        RewardedVideoSmash.this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, "Timeout for " + RewardedVideoSmash.this.getInstanceName(), 0);
                        RewardedVideoSmash.this.setMediationState(MEDIATION_STATE.NOT_AVAILABLE);
                        RewardedVideoSmash.this.mRewardedVideoManagerListener.onRewardedVideoAvailabilityChanged(false, RewardedVideoSmash.this);
                    }

                }
            };
            Timer timer = new Timer();

            if (this.mInitTimerTask != null)
                timer.schedule(this.mInitTimerTask, this.mTimeout * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void startLoadTimer() {
    }


    protected String getAdUnitString() {
        return "rewardedvideo";
    }


    public void initRewardedVideo(Activity activity, String appKey, String userId) {
        startInitTimer();
        if (this.mAdapter != null) {
            this.mAdapter.addRewardedVideoListener(this);
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getInstanceName() + ":initRewardedVideo()", 1);
            this.mAdapter.initRewardedVideo(activity, appKey, userId, this.mRewardedVideoAdapterConfigs, this);
        }
    }

    public void fetchRewardedVideo() {
        if (this.mAdapter != null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getInstanceName() + ":fetchRewardedVideo()", 1);
            this.mAdapter.fetchRewardedVideo(this.mRewardedVideoAdapterConfigs);
        }
    }

    public void showRewardedVideo() {
        if (this.mAdapter != null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getInstanceName() + ":showRewardedVideo()", 1);
            preShow();
            this.mAdapter.showRewardedVideo(this.mRewardedVideoAdapterConfigs, this);
        }
    }

    public boolean isRewardedVideoAvailable() {
        if (this.mAdapter != null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getInstanceName() + ":isRewardedVideoAvailable()", 1);
            return this.mAdapter.isRewardedVideoAvailable(this.mRewardedVideoAdapterConfigs);
        }

        return false;
    }

    public void setRewardedVideoManagerListener(RewardedVideoManagerListener listener) {
        this.mRewardedVideoManagerListener = listener;
    }


    public void onRewardedVideoAdShowFailed(IronSourceError error) {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdShowFailed(error, this);
        }
    }

    public void onRewardedVideoAdOpened() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdOpened(this);
        }
    }

    public void onRewardedVideoAdClosed() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdClosed(this);
        }
        fetchRewardedVideo();
    }

    public void onRewardedVideoAvailabilityChanged(boolean available) {
        stopInitTimer();

        if ((isMediationAvailable()) && (
                ((available) && (this.mMediationState != MEDIATION_STATE.AVAILABLE)) || ((!available) && (this.mMediationState != MEDIATION_STATE.NOT_AVAILABLE)))) {
            setMediationState(available ? MEDIATION_STATE.AVAILABLE : MEDIATION_STATE.NOT_AVAILABLE);

            if (this.mRewardedVideoManagerListener != null) {
                this.mRewardedVideoManagerListener.onRewardedVideoAvailabilityChanged(available, this);
            }
        }
    }

    public void onRewardedVideoAdStarted() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdStarted(this);
        }
    }

    public void onRewardedVideoAdEnded() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdEnded(this);
        }
    }

    public void onRewardedVideoAdRewarded() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdRewarded(this);
        }
    }

    public void onRewardedVideoAdClicked() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdClicked(this);
        }
    }

    public void onRewardedVideoAdVisible() {
        if (this.mRewardedVideoManagerListener != null) {
            this.mRewardedVideoManagerListener.onRewardedVideoAdVisible(this);
        }
    }

    String getRequestUrl() {
        return this.mRequestUrl;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/RewardedVideoSmash.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */