package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.logger.LoggingApi;
import com.ironsource.mediationsdk.sdk.BannerAdapterApi;
import com.ironsource.mediationsdk.sdk.BannerSmashListener;
import com.ironsource.mediationsdk.sdk.BaseApi;
import com.ironsource.mediationsdk.sdk.InterstitialAdapterApi;
import com.ironsource.mediationsdk.sdk.InterstitialSmashListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialApi;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoAdapterApi;
import com.ironsource.mediationsdk.sdk.RewardedVideoSmashListener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONObject;


public abstract class AbstractAdapter
        implements BaseApi, InterstitialAdapterApi, RewardedVideoAdapterApi, BannerAdapterApi, LoggingApi, RewardedInterstitialApi {
    private IronSourceLoggerManager mLoggerManager;
    protected RewardedVideoSmashListener mActiveRewardedVideoSmash;
    protected CopyOnWriteArrayList<RewardedVideoSmashListener> mAllRewardedVideoSmashes;
    protected ConcurrentHashMap<String, RewardedVideoSmashListener> mRewardedVideoPlacementToListenerMap;
    protected InterstitialSmashListener mActiveInterstitialSmash;
    protected CopyOnWriteArrayList<InterstitialSmashListener> mAllInterstitialSmashes;
    protected ConcurrentHashMap<String, InterstitialSmashListener> mInterstitialPlacementToListenerMap;
    protected BannerSmashListener mActiveBannerSmash;
    protected CopyOnWriteArrayList<BannerSmashListener> mAllBannerSmashes;
    protected ConcurrentHashMap<String, BannerSmashListener> mBannerPlacementToListenerMap;
    private String mPluginType;
    private String mPluginFrameworkVersion;
    private String mProviderName;
    protected RewardedInterstitialListener mRewardedInterstitial;

    public AbstractAdapter(String providerName) {
        this.mLoggerManager = IronSourceLoggerManager.getLogger();

        this.mAllRewardedVideoSmashes = new CopyOnWriteArrayList();
        this.mAllInterstitialSmashes = new CopyOnWriteArrayList();

        this.mRewardedVideoPlacementToListenerMap = new ConcurrentHashMap();
        this.mInterstitialPlacementToListenerMap = new ConcurrentHashMap();
        this.mBannerPlacementToListenerMap = new ConcurrentHashMap();

        this.mProviderName = providerName;
    }

    public String getProviderName() {
        return this.mProviderName;
    }

    protected String getDynamicUserId() {
        return IronSourceObject.getInstance().getDynamicUserId();
    }

    void setPluginData(String pluginType, String pluginFrameworkVersion) {
        this.mPluginType = pluginType;
        this.mPluginFrameworkVersion = pluginFrameworkVersion;
    }

    public String getPluginType() {
        return this.mPluginType;
    }

    public String getPluginFrameworkVersion() {
        return this.mPluginFrameworkVersion;
    }

    protected void log(IronSourceLogger.IronSourceTag tag, String message, int logLevel) {
        this.mLoggerManager.onLog(tag, message, logLevel);
    }


    public abstract String getVersion();


    public abstract String getCoreSDKVersion();


    public void setLogListener(LogListener logListener) {
    }


    public void setRewardedInterstitialListener(RewardedInterstitialListener listener) {
        this.mRewardedInterstitial = listener;
    }

    protected boolean isAdaptersDebugEnabled() {
        return this.mLoggerManager.isDebugEnabled();
    }


    public void initBanners(Activity activity, String appKey, String userId, JSONObject config, BannerSmashListener listener) {
    }


    public void loadBanner(IronSourceBannerLayout banner, JSONObject config, BannerSmashListener listener) {
    }


    public void destroyBanner(JSONObject config) {
    }


    public void reloadBanner(JSONObject config) {
    }


    protected void addBannerListener(BannerSmashListener listener) {
    }


    protected void removeBannerListener(BannerSmashListener listener) {
    }


    public void addRewardedVideoListener(RewardedVideoSmashListener listener) {
        this.mAllRewardedVideoSmashes.add(listener);
    }

    public void removeRewardedVideoListener(RewardedVideoSmashListener listener) {
        this.mAllRewardedVideoSmashes.remove(listener);
    }

    public void addInterstitialListener(InterstitialSmashListener listener) {
        this.mAllInterstitialSmashes.add(listener);
    }

    public void removeInterstitialListener(InterstitialSmashListener listener) {
        this.mAllInterstitialSmashes.remove(listener);
    }


    public void onResume(Activity activity) {
    }


    public void onPause(Activity activity) {
    }


    public void setAge(int age) {
    }


    public void setGender(String gender) {
    }


    public void setMediationSegment(String segment) {
    }


    protected boolean isLargeScreen(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        return dpHeight > 720.0F;
    }

    protected int getScreenWidthPixels(Activity activity) {
        return activity.getResources().getDisplayMetrics().widthPixels;
    }

    protected int getScreenWidthDp(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return (int) (displayMetrics.widthPixels / displayMetrics.density);
    }

    protected void setMediationState(AbstractSmash.MEDIATION_STATE state, String adUnit) {
    }

    protected void setConsent(boolean consent) {
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/AbstractAdapter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */