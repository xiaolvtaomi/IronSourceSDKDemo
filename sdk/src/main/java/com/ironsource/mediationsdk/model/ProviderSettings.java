package com.ironsource.mediationsdk.model;

import org.json.JSONException;
import org.json.JSONObject;


public class ProviderSettings {
    private String mProviderName;
    private String mProviderTypeForReflection;
    private JSONObject mApplicationSettings;
    private JSONObject mRewardedVideoSettings;
    private JSONObject mInterstitialSettings;
    private JSONObject mBannerSettings;
    private String mSubProviderId;
    private boolean mIsMultipleInstances;
    private String mProviderInstanceName;
    private int mProviderRVPriority;
    private int mProviderISPriority;
    private int mProviderBNPriority;

    public ProviderSettings(String providerName) {
        this.mProviderName = providerName;
        this.mProviderInstanceName = providerName;
        this.mProviderTypeForReflection = providerName;
        this.mRewardedVideoSettings = new JSONObject();
        this.mInterstitialSettings = new JSONObject();
        this.mBannerSettings = new JSONObject();
        this.mApplicationSettings = new JSONObject();
        this.mProviderRVPriority = -1;
        this.mProviderISPriority = -1;
        this.mProviderBNPriority = -1;
    }

    public ProviderSettings(String providerName, String providerType, JSONObject applicationSettings, JSONObject rewardedVideoSettings, JSONObject interstitialSettings, JSONObject bannerSettings) {
        this.mProviderName = providerName;
        this.mProviderInstanceName = providerName;
        this.mProviderTypeForReflection = providerType;
        this.mRewardedVideoSettings = rewardedVideoSettings;
        this.mInterstitialSettings = interstitialSettings;
        this.mBannerSettings = bannerSettings;
        this.mApplicationSettings = applicationSettings;
        this.mProviderRVPriority = -1;
        this.mProviderISPriority = -1;
        this.mProviderBNPriority = -1;
    }

    public ProviderSettings(ProviderSettings other) {
        this.mProviderName = other.getProviderName();
        this.mProviderInstanceName = other.getProviderName();
        this.mProviderTypeForReflection = other.getProviderTypeForReflection();
        this.mRewardedVideoSettings = other.getRewardedVideoSettings();
        this.mInterstitialSettings = other.getInterstitialSettings();
        this.mBannerSettings = other.getBannerSettings();
        this.mApplicationSettings = other.getApplicationSettings();
        this.mProviderRVPriority = other.getRewardedVideoPriority();
        this.mProviderISPriority = other.getInterstitialPriority();
        this.mProviderBNPriority = other.getBannerPriority();
    }

    public String getProviderName() {
        return this.mProviderName;
    }

    public JSONObject getRewardedVideoSettings() {
        return this.mRewardedVideoSettings;
    }

    public String getProviderTypeForReflection() {
        return this.mProviderTypeForReflection;
    }

    public void setRewardedVideoSettings(JSONObject rewardedVideoSettings) {
        this.mRewardedVideoSettings = rewardedVideoSettings;
    }

    public void setRewardedVideoSettings(String key, Object value) {
        try {
            this.mRewardedVideoSettings.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getInterstitialSettings() {
        return this.mInterstitialSettings;
    }

    public void setInterstitialSettings(JSONObject interstitialSettings) {
        this.mInterstitialSettings = interstitialSettings;
    }

    public void setInterstitialSettings(String key, Object value) {
        try {
            this.mInterstitialSettings.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getBannerSettings() {
        return this.mBannerSettings;
    }

    public void setBannerSettings(JSONObject bannerSettings) {
        this.mBannerSettings = bannerSettings;
    }

    public void setSubProviderId(String subProviderId) {
        this.mSubProviderId = subProviderId;
    }

    public String getSubProviderId() {
        return this.mSubProviderId;
    }

    public void setIsMultipleInstances(boolean isMultipleInstances) {
        this.mIsMultipleInstances = isMultipleInstances;
    }

    public boolean isMultipleInstances() {
        return this.mIsMultipleInstances;
    }

    public String getProviderInstanceName() {
        return this.mProviderInstanceName;
    }

    public JSONObject getApplicationSettings() {
        return this.mApplicationSettings;
    }

    public void setBannerPriority(int priority) {
        this.mProviderBNPriority = priority;
    }

    public void setInterstitialPriority(int priority) {
        this.mProviderISPriority = priority;
    }

    public void setRewardedVideoPriority(int priority) {
        this.mProviderRVPriority = priority;
    }

    public int getBannerPriority() {
        return this.mProviderBNPriority;
    }

    public int getInterstitialPriority() {
        return this.mProviderISPriority;
    }

    public int getRewardedVideoPriority() {
        return this.mProviderRVPriority;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ProviderSettings.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */