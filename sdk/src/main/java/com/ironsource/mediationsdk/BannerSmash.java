package com.ironsource.mediationsdk;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;
import com.ironsource.mediationsdk.sdk.BannerSmashListener;

import java.util.Timer;
import java.util.TimerTask;


public class BannerSmash
        implements BannerSmashListener {
    private AbstractAdapter mAdapter;
    private Timer mTimer;
    private long mLoadTimeoutMilisecs;
    private ProviderSettings mAdapterConfigs;
    private BANNER_SMASH_STATE mState;
    private BannerManagerListener mListener;
    private boolean mIsReadyToLoad;
    private IronSourceBannerLayout mBannerLayout;
    private int mProviderPriority;
    private String mName;

    protected static enum BANNER_SMASH_STATE {
        NO_INIT,
        INIT_IN_PROGRESS,
        LOAD_IN_PROGRESS,
        LOADED,
        LOAD_FAILED,
        DESTROYED;

        private BANNER_SMASH_STATE() {
        }
    }

    BannerSmash(BannerManagerListener listener, ProviderSettings adapterConfigs, AbstractAdapter adapter, long timeout, int providerPriority) {
        this.mState = BANNER_SMASH_STATE.NO_INIT;
        this.mProviderPriority = providerPriority;
        this.mListener = listener;
        this.mAdapter = adapter;
        this.mAdapterConfigs = adapterConfigs;
        this.mLoadTimeoutMilisecs = timeout;
        this.mAdapter.addBannerListener(this);

        if (adapterConfigs.isMultipleInstances()) {
            this.mName = adapterConfigs.getProviderTypeForReflection();
        } else {
            this.mName = adapterConfigs.getProviderName();
        }
    }

    public boolean isReadyToLoad() {
        return this.mIsReadyToLoad;
    }

    public void setReadyToLoad(boolean isReadyToLoad) {
        this.mIsReadyToLoad = isReadyToLoad;
    }

    public int getProviderPriority() {
        return this.mProviderPriority;
    }

    public String getName() {
        return this.mName;
    }

    public String getSubProviderId() {
        return this.mAdapterConfigs.getSubProviderId();
    }

    public AbstractAdapter getAdapter() {
        return this.mAdapter;
    }


    public void loadBanner(IronSourceBannerLayout bannerLayout, Activity activity, String appKey, String userId) {
        log("loadBanner()");
        this.mIsReadyToLoad = false;

        if (bannerLayout == null) {
            this.mListener.onBannerAdLoadFailed(new IronSourceError(610, "banner==null"), this);
            return;
        }

        if (this.mAdapter == null) {
            this.mListener.onBannerAdLoadFailed(new IronSourceError(611, "adapter==null"), this);
            return;
        }

        this.mBannerLayout = bannerLayout;

        startLoadTimer();
        if (this.mState == BANNER_SMASH_STATE.NO_INIT) {
            setState(BANNER_SMASH_STATE.INIT_IN_PROGRESS);
            setCustomParams();
            this.mAdapter.initBanners(activity, appKey, userId, this.mAdapterConfigs.getBannerSettings(), this);
        } else {
            setState(BANNER_SMASH_STATE.LOAD_IN_PROGRESS);
            this.mAdapter.loadBanner(bannerLayout, this.mAdapterConfigs.getBannerSettings(), this);
        }
    }

    public void reloadBanner() {
        log("reloadBanner()");
        startLoadTimer();
        this.mAdapter.reloadBanner(this.mAdapterConfigs.getBannerSettings());
    }

    public void destroyBanner() {
        log("destroyBanner()");
        if (this.mAdapter == null) {
            log("destroyBanner() mAdapter == null");
            return;
        }

        this.mAdapter.destroyBanner(this.mAdapterConfigs.getBannerSettings());
        setState(BANNER_SMASH_STATE.DESTROYED);
    }

    private void setCustomParams() {
        if (this.mAdapter == null) {
            return;
        }
        try {
            Integer age = IronSourceObject.getInstance().getAge();
            if (age != null) {
                this.mAdapter.setAge(age.intValue());
            }
            String gender = IronSourceObject.getInstance().getGender();
            if (!TextUtils.isEmpty(gender)) {
                this.mAdapter.setGender(gender);
            }
            String segment = IronSourceObject.getInstance().getMediationSegment();
            if (!TextUtils.isEmpty(segment)) {
                this.mAdapter.setMediationSegment(segment);
            }
            String pluginType = ConfigFile.getConfigFile().getPluginType();
            if (!TextUtils.isEmpty(pluginType)) {
                this.mAdapter.setPluginData(pluginType, ConfigFile.getConfigFile().getPluginFrameworkVersion());
            }
            Boolean consent = IronSourceObject.getInstance().getConsent();
            if (consent != null) {
                log("setConsent(" + consent + ")");
                this.mAdapter.setConsent(consent.booleanValue());
            }
        } catch (Exception e) {
            log(":setCustomParams():" + e.toString());
        }
    }

    private void setState(BANNER_SMASH_STATE state) {
        this.mState = state;
        log("state=" + state.name());
    }

    private void stopLoadTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
            this.mTimer = null;
        }
    }

    private void startLoadTimer() {
        try {
            stopLoadTimer();

            this.mTimer = new Timer();
            this.mTimer.schedule(new TimerTask() {
                public void run() {
                    if (BannerSmash.this.mState == BANNER_SMASH_STATE.INIT_IN_PROGRESS) {
                        BannerSmash.this.log("init timed out");
                        BannerSmash.this.mListener.onBannerAdLoadFailed(new IronSourceError(607, "Timed out"), BannerSmash.this);
                    } else if (BannerSmash.this.mState == BANNER_SMASH_STATE.LOAD_IN_PROGRESS) {
                        BannerSmash.this.log("load timed out");
                        BannerSmash.this.mListener.onBannerAdLoadFailed(new IronSourceError(608, "Timed out"), BannerSmash.this);
                    } else if (BannerSmash.this.mState == BANNER_SMASH_STATE.LOADED) {
                        BannerSmash.this.log("reload timed out");
                        BannerSmash.this.mListener.onBannerAdReloadFailed(new IronSourceError(609, "Timed out"), BannerSmash.this);
                    }

                    BannerSmash.this.setState(BANNER_SMASH_STATE.LOAD_FAILED);
                }
            }, this.mLoadTimeoutMilisecs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onBannerInitSuccess() {
        stopLoadTimer();
        if (this.mState == BANNER_SMASH_STATE.INIT_IN_PROGRESS) {
            startLoadTimer();
            this.mAdapter.loadBanner(this.mBannerLayout, this.mAdapterConfigs.getBannerSettings(), this);
            setState(BANNER_SMASH_STATE.LOAD_IN_PROGRESS);
        }
    }

    public void onBannerInitFailed(IronSourceError error) {
        stopLoadTimer();
        if (this.mState == BANNER_SMASH_STATE.INIT_IN_PROGRESS) {
            this.mListener.onBannerAdLoadFailed(new IronSourceError(612, "Banner init failed"), this);
            setState(BANNER_SMASH_STATE.NO_INIT);
        }
    }

    public void onBannerAdLoaded(View adView, FrameLayout.LayoutParams frameLayoutParams, RelativeLayout.LayoutParams relativeLayoutParams) {
        log("onBannerAdLoaded()");
        stopLoadTimer();

        if (this.mState == BANNER_SMASH_STATE.LOAD_IN_PROGRESS) {
            setState(BANNER_SMASH_STATE.LOADED);
            this.mListener.onBannerAdLoaded(this, adView, frameLayoutParams, relativeLayoutParams);
        } else if (this.mState == BANNER_SMASH_STATE.LOADED) {
            this.mListener.onBannerAdReloaded(this);
        }
    }

    public void onBannerAdLoadFailed(IronSourceError error) {
        log("onBannerAdLoadFailed()");
        stopLoadTimer();

        if (this.mState == BANNER_SMASH_STATE.LOAD_IN_PROGRESS) {
            setState(BANNER_SMASH_STATE.LOAD_FAILED);
            this.mListener.onBannerAdLoadFailed(error, this);
        } else if (this.mState == BANNER_SMASH_STATE.LOADED) {
            this.mListener.onBannerAdReloadFailed(error, this);
        }
    }


    public void onBannerAdClicked() {
        if (this.mListener != null) {
            this.mListener.onBannerAdClicked(this);
        }
    }

    public void onBannerAdScreenPresented() {
        if (this.mListener != null) {
            this.mListener.onBannerAdScreenPresented(this);
        }
    }

    public void onBannerAdScreenDismissed() {
        if (this.mListener != null) {
            this.mListener.onBannerAdScreenDismissed(this);
        }
    }

    public void onBannerAdLeftApplication() {
        if (this.mListener != null) {
            this.mListener.onBannerAdLeftApplication(this);
        }
    }

    public void setConsent(boolean consent) {
        if (this.mAdapter != null) {
            log("setConsent(" + consent + ")");
            this.mAdapter.setConsent(consent);
        }
    }

    public void onPause(Activity activity) {
        if (this.mAdapter != null) {
            this.mAdapter.onPause(activity);
        }
    }

    public void onResume(Activity activity) {
        if (this.mAdapter != null) {
            this.mAdapter.onResume(activity);
        }
    }

    private void log(String text) {
        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.ADAPTER_API, "BannerSmash " + getName() + " " + text, 1);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/BannerSmash.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */