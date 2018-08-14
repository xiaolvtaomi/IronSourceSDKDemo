package com.ironsource.adapters.supersonicads;

import android.text.TextUtils;

import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.model.ProviderSettingsHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;


public class SupersonicConfig {
    private final String CUSTOM_PARAM_PREFIX = "custom_";
    private final String CLIENT_SIDE_CALLBACKS = "useClientSideCallbacks";
    private final String MAX_VIDEO_LENGTH = "maxVideoLength";
    private final String DYNAMIC_CONTROLLER_URL = "controllerUrl";
    private final String DYNAMIC_CONTROLLER_DEBUG_MODE = "debugMode";
    private final String CAMPAIGN_ID = "campaignId";
    private final String LANGUAGE = "language";
    private final String APPLICATION_PRIVATE_KEY = "privateKey";
    private final String ITEM_NAME = "itemName";
    private final String ITEM_COUNT = "itemCount";

    private Map<String, String> mRewardedVideoCustomParams;

    private Map<String, String> mOfferwallCustomParams;
    private static SupersonicConfig mInstance;
    ProviderSettings mProviderSettings;

    public static SupersonicConfig getConfigObj() {
        if (mInstance == null) {
            mInstance = new SupersonicConfig();
        }
        return mInstance;
    }

    private SupersonicConfig() {
        this.mProviderSettings = new ProviderSettings(ProviderSettingsHolder.getProviderSettingsHolder().getProviderSettings("Mediation"));
    }

    public void setClientSideCallbacks(boolean status) {
        this.mProviderSettings.setRewardedVideoSettings("useClientSideCallbacks", String.valueOf(status));
    }

    public void setCustomControllerUrl(String url) {
        this.mProviderSettings.setRewardedVideoSettings("controllerUrl", url);
        this.mProviderSettings.setInterstitialSettings("controllerUrl", url);
    }

    public void setDebugMode(int debugMode) {
        this.mProviderSettings.setRewardedVideoSettings("debugMode", Integer.valueOf(debugMode));
        this.mProviderSettings.setInterstitialSettings("debugMode", Integer.valueOf(debugMode));
    }

    public void setCampaignId(String id) {
        this.mProviderSettings.setRewardedVideoSettings("campaignId", id);
    }

    public void setLanguage(String language) {
        this.mProviderSettings.setRewardedVideoSettings("language", language);
        this.mProviderSettings.setInterstitialSettings("language", language);
    }

    public void setRewardedVideoCustomParams(Map<String, String> rvCustomParams) {
        this.mRewardedVideoCustomParams = convertCustomParams(rvCustomParams);
    }

    public void setOfferwallCustomParams(Map<String, String> owCustomParams) {
        this.mOfferwallCustomParams = convertCustomParams(owCustomParams);
    }

    private Map<String, String> convertCustomParams(Map<String, String> customParams) {
        Map<String, String> result = new HashMap();
        try {
            if (customParams != null) {

                Set<String> keys = customParams.keySet();
                if (keys != null) {
                    for (String k : keys) {
                        if (!TextUtils.isEmpty(k)) {
                            String value = (String) customParams.get(k);
                            if (!TextUtils.isEmpty(value)) {
                                result.put("custom_" + k, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, ":convertCustomParams()", e);
        }

        return result;
    }


    public boolean getClientSideCallbacks() {
        boolean csc = false;

        if ((this.mProviderSettings != null) && (this.mProviderSettings.getRewardedVideoSettings() != null) &&
                (this.mProviderSettings.getRewardedVideoSettings().has("useClientSideCallbacks"))) {
            csc = this.mProviderSettings.getRewardedVideoSettings().optBoolean("useClientSideCallbacks", false);
        }

        return csc;
    }

    Map<String, String> getOfferwallCustomParams() {
        return this.mOfferwallCustomParams;
    }

    Map<String, String> getRewardedVideoCustomParams() {
        return this.mRewardedVideoCustomParams;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/adapters/supersonicads/SupersonicConfig.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */