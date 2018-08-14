package com.ironsource.mediationsdk.model;

import android.text.TextUtils;

import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONObject;


public class ProviderSettingsHolder {
    private ArrayList<ProviderSettings> mProviderSettingsArrayList;
    private static ProviderSettingsHolder mInstance;

    public static synchronized ProviderSettingsHolder getProviderSettingsHolder() {
        if (mInstance == null) {
            mInstance = new ProviderSettingsHolder();
        }
        return mInstance;
    }

    private ProviderSettingsHolder() {
        this.mProviderSettingsArrayList = new ArrayList();
    }

    public void addProviderSettings(ProviderSettings providerSettings) {
        if (providerSettings != null)
            this.mProviderSettingsArrayList.add(providerSettings);
    }

    public ProviderSettings getProviderSettings(String providerName) {
        for (ProviderSettings providerSettings : this.mProviderSettingsArrayList) {
            if (providerSettings.getProviderName().equals(providerName)) {
                return providerSettings;
            }
        }
        ProviderSettings ps = new ProviderSettings(providerName);
        addProviderSettings(ps);

        return ps;
    }

    public HashSet<String> getProviderSettingsByReflectionName(String providerNameForReflection, String fieldName) {
        HashSet<String> result = new HashSet();


        try {
            for (ProviderSettings providerSettings : this.mProviderSettingsArrayList) {
                if (providerSettings.getProviderTypeForReflection().equals(providerNameForReflection)) {
                    if ((providerSettings.getRewardedVideoSettings() != null) && (providerSettings.getRewardedVideoSettings().length() > 0) &&
                            (!TextUtils.isEmpty(providerSettings.getRewardedVideoSettings().optString(fieldName))))
                        result.add(providerSettings.getRewardedVideoSettings().optString(fieldName));
                    if ((providerSettings.getInterstitialSettings() != null) && (providerSettings.getInterstitialSettings().length() > 0) &&
                            (!TextUtils.isEmpty(providerSettings.getInterstitialSettings().optString(fieldName))))
                        result.add(providerSettings.getInterstitialSettings().optString(fieldName));
                    if ((providerSettings.getBannerSettings() != null) && (providerSettings.getBannerSettings().length() > 0) &&
                            (!TextUtils.isEmpty(providerSettings.getBannerSettings().optString(fieldName)))) {
                        result.add(providerSettings.getBannerSettings().optString(fieldName));
                    }
                }
            }
        } catch (Exception localException1) {
        }

        return result;
    }

    public boolean containsProviderSettings(String providerName) {
        for (ProviderSettings providerSettings : this.mProviderSettingsArrayList) {
            if (providerSettings.getProviderName().equals(providerName)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ProviderSettings> getProviderSettingsArrayList() {
        return this.mProviderSettingsArrayList;
    }


    public void fillSubProvidersDetails() {
        for (ProviderSettings providerSettings : this.mProviderSettingsArrayList) {
            boolean isSubProvider = (providerSettings.isMultipleInstances()) && (!TextUtils.isEmpty(providerSettings.getProviderTypeForReflection()));

            if (isSubProvider) {
                ProviderSettings commonProviderSettings = getProviderSettings(providerSettings.getProviderTypeForReflection());


                providerSettings.setInterstitialSettings(IronSourceUtils.mergeJsons(providerSettings.getInterstitialSettings(), commonProviderSettings.getInterstitialSettings()));
                providerSettings.setRewardedVideoSettings(IronSourceUtils.mergeJsons(providerSettings.getRewardedVideoSettings(), commonProviderSettings.getRewardedVideoSettings()));
                providerSettings.setBannerSettings(IronSourceUtils.mergeJsons(providerSettings.getBannerSettings(), commonProviderSettings.getBannerSettings()));
            }
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ProviderSettingsHolder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */