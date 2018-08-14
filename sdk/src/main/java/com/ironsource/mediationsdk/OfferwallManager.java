package com.ironsource.mediationsdk;

import android.app.Activity;
import android.text.TextUtils;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.Configurations;
import com.ironsource.mediationsdk.model.OfferwallConfigurations;
import com.ironsource.mediationsdk.model.OfferwallPlacement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.model.ProviderSettingsHolder;
import com.ironsource.mediationsdk.sdk.InternalOfferwallApi;
import com.ironsource.mediationsdk.sdk.InternalOfferwallListener;
import com.ironsource.mediationsdk.sdk.OfferwallAdapterApi;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

class OfferwallManager implements InternalOfferwallApi, InternalOfferwallListener {
    private final String TAG = getClass().getName();

    private OfferwallAdapterApi mAdapter;

    private InternalOfferwallListener mListenersWrapper;

    private IronSourceLoggerManager mLoggerManager;

    private AtomicBoolean mAtomicShouldPerformInit;
    private AtomicBoolean mIsOfferwallAvailable;
    private ServerResponseWrapper mServerResponseWrapper;
    private ProviderSettings mProviderSettings;
    private String mCurrentPlacementName;
    private Activity mActivity;

    OfferwallManager() {
        this.mAtomicShouldPerformInit = new AtomicBoolean(true);
        this.mIsOfferwallAvailable = new AtomicBoolean(false);
        this.mLoggerManager = IronSourceLoggerManager.getLogger();
    }


    public synchronized void initOfferwall(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":initOfferwall(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mActivity = activity;

        this.mServerResponseWrapper = IronSourceObject.getInstance().getCurrentServerResponse();

        if (this.mServerResponseWrapper == null) {
            reportInitFail(ErrorBuilder.buildInitFailedError("Please check configurations for Offerwall adapters", "Offerwall"));
            return;
        }

        this.mProviderSettings = this.mServerResponseWrapper.getProviderSettingsHolder().getProviderSettings("SupersonicAds");

        if (this.mProviderSettings == null) {
            reportInitFail(ErrorBuilder.buildInitFailedError("Please check configurations for Offerwall adapters", "Offerwall"));
            return;
        }

        AbstractAdapter offerwallAdapter = startOfferwallAdapter();

        if (offerwallAdapter == null) {
            reportInitFail(ErrorBuilder.buildInitFailedError("Please check configurations for Offerwall adapters", "Offerwall"));
            return;
        }

        setCustomParams(offerwallAdapter);
        offerwallAdapter.setLogListener(this.mLoggerManager);

        this.mAdapter = ((OfferwallAdapterApi) offerwallAdapter);


        this.mAdapter.setInternalOfferwallListener(this);

        this.mAdapter.initOfferwall(activity, appKey, userId, this.mProviderSettings.getRewardedVideoSettings());
    }


    public void showOfferwall() {
    }


    public void showOfferwall(String placementName) {
        String logMessage = "OWManager:showOfferwall(" + placementName + ")";

        try {
            if (!IronSourceUtils.isNetworkConnected(this.mActivity)) {
                this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildNoInternetConnectionShowFailError("Offerwall"));
                return;
            }

            this.mCurrentPlacementName = placementName;

            OfferwallPlacement placement = this.mServerResponseWrapper.getConfigurations().getOfferwallConfigurations().getOfferwallPlacement(placementName);

            if (placement == null) {
                String noPlacementMessage = "Placement is not valid, please make sure you are using the right placements, using the default placement.";
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, noPlacementMessage, 3);


                placement = this.mServerResponseWrapper.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
                if (placement == null) {
                    String noDefaultPlacement = "Default placement was not found, please make sure you are using the right placements.";
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, noDefaultPlacement, 3);
                    return;
                }
            }
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, logMessage, 1);


            if ((this.mIsOfferwallAvailable != null) && (this.mIsOfferwallAvailable.get()) &&
                    (this.mAdapter != null)) {
                this.mAdapter.showOfferwall(String.valueOf(placement.getPlacementId()), this.mProviderSettings.getRewardedVideoSettings());
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.INTERNAL, logMessage, e);
        }
    }

    public synchronized boolean isOfferwallAvailable() {
        boolean result = false;

        if (this.mIsOfferwallAvailable != null) {
            result = this.mIsOfferwallAvailable.get();
        }
        return result;
    }

    public void getOfferwallCredits() {
        if (this.mAdapter != null) {
            this.mAdapter.getOfferwallCredits();
        }
    }


    public void setOfferwallListener(OfferwallListener offerwallListener) {
    }


    public void setInternalOfferwallListener(InternalOfferwallListener listener) {
        this.mListenersWrapper = listener;
    }


    public void onOfferwallAvailable(boolean isAvailable) {
        onOfferwallAvailable(isAvailable, null);
    }

    public void onOfferwallAvailable(boolean isAvailable, IronSourceError error) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onOfferwallAvailable(isAvailable: " + isAvailable + ")", 1);

        if (isAvailable) {
            this.mIsOfferwallAvailable.set(true);
            if (this.mListenersWrapper != null)
                this.mListenersWrapper.onOfferwallAvailable(true);
        } else {
            reportInitFail(error);
        }
    }

    public void onOfferwallOpened() {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onOfferwallOpened()", 1);

        JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
        try {
            if (!TextUtils.isEmpty(this.mCurrentPlacementName)) {
                data.put("placement", this.mCurrentPlacementName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        EventData event = new EventData(305, data);
        RewardedVideoEventsManager.getInstance().log(event);

        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onOfferwallOpened();
        }
    }

    public void onOfferwallShowFailed(IronSourceError error) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onOfferwallShowFailed(" + error + ")", 1);

        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onOfferwallShowFailed(error);
        }
    }

    public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onOfferwallAdCredited()", 1);

        if (this.mListenersWrapper != null) {
            return this.mListenersWrapper.onOfferwallAdCredited(credits, totalCredits, totalCreditsFlag);
        }
        return false;
    }

    public void onGetOfferwallCreditsFailed(IronSourceError error) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onGetOfferwallCreditsFailed(" + error + ")", 1);

        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onGetOfferwallCreditsFailed(error);
        }
    }

    public void onOfferwallClosed() {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onOfferwallClosed()", 1);

        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onOfferwallClosed();
        }
    }

    private synchronized void reportInitFail(IronSourceError error) {
        if (this.mIsOfferwallAvailable != null) {
            this.mIsOfferwallAvailable.set(false);
        }
        if (this.mAtomicShouldPerformInit != null) {
            this.mAtomicShouldPerformInit.set(true);
        }
        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.onOfferwallAvailable(false, error);
        }
    }

    private AbstractAdapter startOfferwallAdapter() {
        AbstractAdapter providerAdapter = null ;
        try {
            IronSourceObject sso = IronSourceObject.getInstance();

            providerAdapter = sso.getExistingAdapter("SupersonicAds");

            if (providerAdapter == null) {
                Class<?> mAdapterClass = Class.forName("com.ironsource.adapters." + "SupersonicAds".toLowerCase() + "." + "SupersonicAds" + "Adapter");
                Method startAdapterMethod = mAdapterClass.getMethod("startAdapter", new Class[]{String.class});
                providerAdapter = (AbstractAdapter) startAdapterMethod.invoke(mAdapterClass, new Object[]{"SupersonicAds"});

                if (providerAdapter == null) {
                    return null;
                }
            }
            sso.addOWAdapter(providerAdapter);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "SupersonicAds initialization failed - please verify that required dependencies are in you build path.", 2);
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":startOfferwallAdapter", e);

            return null;
        }
        return providerAdapter;
    }

    private void setCustomParams(AbstractAdapter providerAdapter) {
        try {
            Integer age = IronSourceObject.getInstance().getAge();
            if (age != null) {
                providerAdapter.setAge(age.intValue());
            }
            String gender = IronSourceObject.getInstance().getGender();
            if (gender != null) {
                providerAdapter.setGender(gender);
            }
            String segment = IronSourceObject.getInstance().getMediationSegment();
            if (segment != null) {
                providerAdapter.setMediationSegment(segment);
            }
            Boolean consent = IronSourceObject.getInstance().getConsent();
            if (consent != null) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, "Offerwall | setConsent(consent:" + consent + ")", 1);
                providerAdapter.setConsent(consent.booleanValue());
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, ":setCustomParams():" + e.toString(), 3);
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/OfferwallManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */