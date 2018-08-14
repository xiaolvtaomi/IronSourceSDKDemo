package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.ironsource.environment.DeviceStatus;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.events.SuperLooper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.LogListener;
import com.ironsource.mediationsdk.logger.PublisherLogger;
import com.ironsource.mediationsdk.model.ApplicationConfigurations;
import com.ironsource.mediationsdk.model.ApplicationEvents;
import com.ironsource.mediationsdk.model.ApplicationLogger;
import com.ironsource.mediationsdk.model.BannerConfigurations;
import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.Configurations;
import com.ironsource.mediationsdk.model.InterstitialConfigurations;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.OfferwallConfigurations;
import com.ironsource.mediationsdk.model.OfferwallPlacement;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.model.ProviderOrder;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.model.ProviderSettingsHolder;
import com.ironsource.mediationsdk.model.RewardedVideoConfigurations;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyRewardedVideoListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.ListenersWrapper;
import com.ironsource.mediationsdk.sdk.OfferwallListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.sdk.SegmentListener;
import com.ironsource.mediationsdk.server.HttpFunctions;
import com.ironsource.mediationsdk.server.ServerURL;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.CappingManager.ECappingStatus;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.GeneralPropertiesWorker;
import com.ironsource.mediationsdk.utils.IronSourceAES;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

public class IronSourceObject implements com.ironsource.mediationsdk.sdk.IronSourceInterface, MediationInitializer.OnMediationInitializationListener {
    private final String TAG = getClass().getName();

    private ArrayList<IronSource.AD_UNIT> mDemandOnlyAdUnits;

    private ArrayList<AbstractAdapter> mRewardedVideoAdaptersList;

    private ArrayList<AbstractAdapter> mInterstitialAdaptersList;

    private ArrayList<AbstractAdapter> mBannerAdaptersList;

    private AbstractAdapter mOfferwallAdapter;

    private RewardedVideoManager mRewardedVideoManager;

    private InterstitialManager mInterstitialManager;
    private OfferwallManager mOfferwallManager;
    private BannerManager mBannerManager;
    private IronSourceLoggerManager mLoggerManager;
    private ListenersWrapper mListenersWrapper;
    private PublisherLogger mPublisherLogger;
    private AtomicBoolean mEventManagersInit;
    private final Object mServerResponseLocker = new Object();
    private ServerResponseWrapper mCurrentServerResponse = null;

    private String mAppKey = null;
    private String mUserId = null;
    private Integer mUserAge = null;
    private String mUserGender = null;
    private String mSegment = null;
    private String mDynamicUserId = null;
    private Map<String, String> mRvServerParams = null;
    private String mMediationType = null;
    private AtomicBoolean mAtomicIsFirstInit;
    private boolean mInitSucceeded = false;
    private List<IronSource.AD_UNIT> mInitiatedAdUnits;
    private String mSessionId = null;

    private Activity mActivity;
    private Set<IronSource.AD_UNIT> mAdUnitsToInitialize;
    private Set<IronSource.AD_UNIT> mRequestedAdUnits;
    private boolean mShouldSendGetInstanceEvent = true;

    private IronSourceSegment mIronSegment;

    private final String KEY_INIT_COUNTER = "sessionDepth";

    private int mInitCounter;

    private static IronSourceObject sInstance;

    private boolean mDidInitInterstitial;
    private boolean mDidInitBanner;
    private boolean mIsBnLoadBeforeInitCompleted;
    private IronSourceBannerLayout mBnLayoutToLoad;
    private String mBnPlacementToLoad;
    private Boolean mConsent = null;

    public static synchronized IronSourceObject getInstance() {
        if (sInstance == null) {
            sInstance = new IronSourceObject();
        }

        return sInstance;
    }

    private IronSourceObject() {
        initializeManagers();


        this.mEventManagersInit = new AtomicBoolean();
        this.mDemandOnlyAdUnits = new ArrayList();
        this.mRewardedVideoAdaptersList = new ArrayList();
        this.mInterstitialAdaptersList = new ArrayList();
        this.mBannerAdaptersList = new ArrayList();
        this.mAdUnitsToInitialize = new HashSet();
        this.mRequestedAdUnits = new HashSet();

        this.mAtomicIsFirstInit = new AtomicBoolean(true);
        this.mInitCounter = 0;
        this.mDidInitInterstitial = false;
        this.mDidInitBanner = false;
        this.mSessionId = UUID.randomUUID().toString();
        this.mIsBnLoadBeforeInitCompleted = false;
        this.mBnPlacementToLoad = null;
    }

    public synchronized void init(Activity activity, String appKey, boolean isDemandOnlyInit, IronSource.AD_UNIT... adUnits) {
        if ((this.mAtomicIsFirstInit != null) && (this.mAtomicIsFirstInit.compareAndSet(true, false))) {
            if ((adUnits == null) || (adUnits.length == 0)) {
                for (IronSource.AD_UNIT adUnit : IronSource.AD_UNIT.values()) {
                    this.mAdUnitsToInitialize.add(adUnit);
                }
                this.mDidInitInterstitial = true;
                this.mDidInitBanner = true;
            } else {
                for (IronSource.AD_UNIT adUnit : adUnits) {
                    this.mAdUnitsToInitialize.add(adUnit);
                    this.mRequestedAdUnits.add(adUnit);
                    if (adUnit.equals(IronSource.AD_UNIT.INTERSTITIAL)) {
                        this.mDidInitInterstitial = true;
                    }
                    if (adUnit.equals(IronSource.AD_UNIT.BANNER)) {
                        this.mDidInitBanner = true;
                    }
                }
            }

            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init(appKey:" + appKey + ")", 1);

            if (activity == null) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Init Fail - provided activity is null", 2);
                return;
            }

            this.mActivity = activity;
            prepareEventManagers(activity);


            ConfigValidationResult validationResultAppKey = validateAppKey(appKey);
            if (validationResultAppKey.isValid()) {
                this.mAppKey = appKey;
            } else {
                if (this.mAdUnitsToInitialize.contains(IronSource.AD_UNIT.REWARDED_VIDEO))
                    this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                if (this.mAdUnitsToInitialize.contains(IronSource.AD_UNIT.OFFERWALL)) {
                    this.mListenersWrapper.onOfferwallAvailable(false, validationResultAppKey.getIronSourceError());
                }
                IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, validationResultAppKey.getIronSourceError().toString(), 1);

                return;
            }

            if (this.mShouldSendGetInstanceEvent) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(isDemandOnlyInit);
                try {
                    if (adUnits != null) {
                        IronSource.AD_UNIT[] arrayOfAD_UNIT2 = adUnits;

                        for (int i = 0; i < arrayOfAD_UNIT2.length; i++) {
                            IronSource.AD_UNIT adUnit = arrayOfAD_UNIT2[i];
                            data.put(adUnit.toString(), true);
                        }
                    }
                    data.put("sessionDepth", ++this.mInitCounter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EventData instanceEvent = new EventData(14, data);
                RewardedVideoEventsManager.getInstance().log(instanceEvent);
                this.mShouldSendGetInstanceEvent = false;
            }

            if (this.mAdUnitsToInitialize.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                MediationInitializer.getInstance().addMediationInitializationListener(this.mInterstitialManager);
            }

            MediationInitializer.getInstance().addMediationInitializationListener(this);
            MediationInitializer.getInstance().init(activity, appKey, this.mUserId, adUnits);
        } else if (adUnits != null) {
            attachAdUnits(isDemandOnlyInit, adUnits);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Multiple calls to init without ad units are not allowed", 3);
        }
    }

    public synchronized void initISDemandOnly(Activity activity, String appKey, IronSource.AD_UNIT... adUnits) {
        ArrayList<IronSource.AD_UNIT> validAdUnitsList = new ArrayList();

        if (adUnits != null) {
            for (IronSource.AD_UNIT adUnit : adUnits) {
                if ((adUnit.equals(IronSource.AD_UNIT.BANNER)) || (adUnit.equals(IronSource.AD_UNIT.OFFERWALL))) {
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, adUnit + " ad unit cannot be initialized in demand only mode", 3);
                } else {
                    if (adUnit.equals(IronSource.AD_UNIT.INTERSTITIAL)) {
                        this.mDidInitInterstitial = true;
                    }

                    validAdUnitsList.add(adUnit);

                    if (!this.mDemandOnlyAdUnits.contains(adUnit)) {
                        this.mDemandOnlyAdUnits.add(adUnit);
                        if (adUnit.equals(IronSource.AD_UNIT.INTERSTITIAL)) {
                            this.mInterstitialManager.mIsInISDemandOnlyMode = true;
                        }
                    }
                }
            }
            if (validAdUnitsList.size() > 0) {
                IronSource.AD_UNIT[] validAdUnitsArr = new IronSource.AD_UNIT[validAdUnitsList.size()];
                validAdUnitsArr = (IronSource.AD_UNIT[]) validAdUnitsList.toArray(validAdUnitsArr);

                init(activity, appKey, true, validAdUnitsArr);
            }
        }
    }

    private synchronized void attachAdUnits(boolean isDemandOnlyInit, IronSource.AD_UNIT... adUnits) {
        for (IronSource.AD_UNIT adUnit : adUnits) {
            if (adUnit.equals(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mDidInitInterstitial = true;
            } else if (adUnit.equals(IronSource.AD_UNIT.BANNER)) {
                this.mDidInitBanner = true;
            }
        }
        if (MediationInitializer.getInstance().getCurrentInitStatus() == MediationInitializer.EInitStatus.INIT_FAILED) {
            try {
                if (this.mListenersWrapper != null) {
                    for (IronSource.AD_UNIT adUnit : adUnits) {
                        if (!this.mAdUnitsToInitialize.contains(adUnit))
                            notifyPublisherAboutInitFailed(adUnit, true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!this.mInitSucceeded) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(isDemandOnlyInit);
                boolean shouldSentInstanceEvent = false;

                for (int i = 0; i < adUnits.length; i++) {
                    IronSource.AD_UNIT adUnit = adUnits[i];
                    if (!this.mAdUnitsToInitialize.contains(adUnit)) {
                        shouldSentInstanceEvent = true;

                        this.mAdUnitsToInitialize.add(adUnit);
                        this.mRequestedAdUnits.add(adUnit);
                        try {
                            data.put(adUnit.toString(), true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, adUnit + " ad unit has already been initialized", 3);

                        if ((isDemandOnlyInit) &&
                                (this.mDemandOnlyAdUnits.contains(adUnit))) {
                            this.mDemandOnlyAdUnits.remove(adUnit);
                        }
                    }
                }

                if (shouldSentInstanceEvent) {
                    try {
                        data.put("sessionDepth", ++this.mInitCounter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    RewardedVideoEventsManager.getInstance().log(new EventData(14, data));
                }
            } else {
                if (this.mInitiatedAdUnits == null) {
                    return;
                }
                JSONObject data = IronSourceUtils.getMediationAdditionalData(isDemandOnlyInit);
                boolean shouldSentInstanceEvent = false;

                for (int i = 0; i < adUnits.length; i++) {
                    IronSource.AD_UNIT adUnit = adUnits[i];

                    if (!this.mAdUnitsToInitialize.contains(adUnit)) {
                        shouldSentInstanceEvent = true;

                        this.mAdUnitsToInitialize.add(adUnit);
                        this.mRequestedAdUnits.add(adUnit);
                        try {
                            data.put(adUnit.toString(), true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        if ((this.mInitiatedAdUnits != null) && (this.mInitiatedAdUnits.contains(adUnit))) {
                            startAdUnit(adUnit);
                        } else {
                            notifyPublisherAboutInitFailed(adUnit, false);
                        }
                    } else {
                        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, adUnit + " ad unit has already been initialized", 3);

                        if ((isDemandOnlyInit) &&
                                (this.mDemandOnlyAdUnits.contains(adUnit))) {
                            this.mDemandOnlyAdUnits.remove(adUnit);
                        }
                    }
                }

                if (shouldSentInstanceEvent) {
                    try {
                        data.put("sessionDepth", ++this.mInitCounter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    EventData instanceEvent = new EventData(14, data);
                    RewardedVideoEventsManager.getInstance().log(instanceEvent);
                }
            }
        }
    }

    public void onInitSuccess(List<IronSource.AD_UNIT> adUnits, boolean revived) {
        try {
            this.mInitiatedAdUnits = adUnits;
            this.mInitSucceeded = true;
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "onInitSuccess()", 1);


            if (revived) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
                try {
                    data.put("revived", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RewardedVideoEventsManager.getInstance().log( new EventData(114, data));
            }


            InterstitialEventsManager.getInstance().triggerEventsSend();
            RewardedVideoEventsManager.getInstance().triggerEventsSend();

            IronSource.AD_UNIT[] adunits = IronSource.AD_UNIT.values();
            for(int i = 0; i < adunits.length; i++) {
                IronSource.AD_UNIT adUnit = adunits[i];
                if(this.mAdUnitsToInitialize.contains(adUnit)) {
                    if(adUnits.contains(adUnit)) {
                        this.startAdUnit(adUnit);
                    } else {
                        this.notifyPublisherAboutInitFailed(adUnit, false);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAdUnit(IronSource.AD_UNIT adUnit) {
        switch (adUnit) {
            case REWARDED_VIDEO:
                startRewardedVideo();
                break;
            case INTERSTITIAL:
                startInterstitial();
                break;
            case OFFERWALL:
                this.mOfferwallManager.initOfferwall(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
                break;
            case BANNER:
                startBanner();
        }
    }

    private void startRewardedVideo() {
        this.mRewardedVideoManager.mIsInISDemandOnlyMode = this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO);
        if (this.mRewardedVideoManager.mIsInISDemandOnlyMode) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Rewarded Video started in Demand Only mode", 0);
        }
        int rewardedVideoTimeout = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdaptersSmartLoadTimeout();

        for (int i = 0; i < this.mCurrentServerResponse.getProviderOrder().getRewardedVideoProviderOrder().size(); i++) {
            String provider = (String) this.mCurrentServerResponse.getProviderOrder().getRewardedVideoProviderOrder().get(i);

            if (!TextUtils.isEmpty(provider)) {
                ProviderSettings providerSettings = this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettings(provider);

                if (providerSettings != null) {
                    RewardedVideoSmash smash = new RewardedVideoSmash(providerSettings, rewardedVideoTimeout);

                    if (validateSmash(smash)) {
                        smash.setRewardedVideoManagerListener(this.mRewardedVideoManager);
                        smash.setProviderPriority(i + 1);
                        this.mRewardedVideoManager.addSmashToArray(smash);
                    }
                }
            }
        }


        if (this.mRewardedVideoManager.mSmashArray.size() > 0) {
            boolean ultraEventsEnabled = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().isUltraEventsEnabled();
            this.mRewardedVideoManager.setIsUltraEventsEnabled(ultraEventsEnabled);

            int smartLoadAmount = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoAdaptersSmartLoadAmount();
            this.mRewardedVideoManager.setSmartLoadAmount(smartLoadAmount);

            String backfillProvider = this.mCurrentServerResponse.getRVBackFillProvider();

            if (!TextUtils.isEmpty(backfillProvider)) {
                ProviderSettings providerSettings = this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettings(backfillProvider);

                if (providerSettings != null) {
                    RewardedVideoSmash backfillSmash = new RewardedVideoSmash(providerSettings, rewardedVideoTimeout);

                    if (validateSmash(backfillSmash)) {
                        backfillSmash.setRewardedVideoManagerListener(this.mRewardedVideoManager);
                        this.mRewardedVideoManager.setBackfillSmash(backfillSmash);
                    }
                }
            }

            String premiumProvider = this.mCurrentServerResponse.getRVPremiumProvider();

            if (!TextUtils.isEmpty(premiumProvider)) {
                ProviderSettings providerSettings = this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettings(premiumProvider);

                if (providerSettings != null) {
                    RewardedVideoSmash premiumSmash = new RewardedVideoSmash(providerSettings, rewardedVideoTimeout);

                    if (validateSmash(premiumSmash)) {
                        premiumSmash.setRewardedVideoManagerListener(this.mRewardedVideoManager);
                        this.mRewardedVideoManager.setPremiumSmash(premiumSmash);
                    }
                }
            }

            this.mRewardedVideoManager.initRewardedVideo(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
        } else {
            notifyPublisherAboutInitFailed(IronSource.AD_UNIT.REWARDED_VIDEO, false);
        }
    }

    private void startInterstitial() {
        this.mInterstitialManager.mIsInISDemandOnlyMode = this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL);
        if (this.mInterstitialManager.mIsInISDemandOnlyMode) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Interstitial started in Demand Only mode", 0);
        }
        int interstitialTimeout = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialAdaptersSmartLoadTimeout();

        for (int i = 0; i < this.mCurrentServerResponse.getProviderOrder().getInterstitialProviderOrder().size(); i++) {
            String provider = (String) this.mCurrentServerResponse.getProviderOrder().getInterstitialProviderOrder().get(i);

            if (!TextUtils.isEmpty(provider)) {
                ProviderSettings providerSettings = this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettings(provider);

                if (providerSettings != null) {
                    InterstitialSmash smash = new InterstitialSmash(providerSettings, interstitialTimeout);

                    if (validateSmash(smash)) {
                        smash.setInterstitialManagerListener(this.mInterstitialManager);

                        smash.setProviderPriority(i + 1);
                        this.mInterstitialManager.addSmashToArray(smash);
                    }
                }
            }
        }

        if (this.mInterstitialManager.mSmashArray.size() > 0) {
            int smartLoadAmount = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialAdaptersSmartLoadAmount();
            this.mInterstitialManager.setSmartLoadAmount(smartLoadAmount);

            this.mInterstitialManager.initInterstitial(this.mActivity, getIronSourceAppKey(), getIronSourceUserId());
        } else {
            notifyPublisherAboutInitFailed(IronSource.AD_UNIT.INTERSTITIAL, false);
        }
    }

    private void startBanner() {
        long bannerTimeout = this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getBannerAdaptersSmartLoadTimeout();
        int bannerInterval = this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getBannerRefreshInterval();


        ArrayList<ProviderSettings> adapterConfigs = new ArrayList();
        for (int i = 0; i < this.mCurrentServerResponse.getProviderOrder().getBannerProviderOrder().size(); i++) {
            String provider = (String) this.mCurrentServerResponse.getProviderOrder().getBannerProviderOrder().get(i);

            if (!TextUtils.isEmpty(provider)) {
                ProviderSettings providerSettings = this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettings(provider);

                if (providerSettings != null) {
                    adapterConfigs.add(providerSettings);
                }
            }
        }

        this.mBannerManager.initBannerManager(adapterConfigs, this.mActivity, getIronSourceAppKey(), getIronSourceUserId(), bannerTimeout, bannerInterval);

        if (this.mIsBnLoadBeforeInitCompleted) {
            this.mIsBnLoadBeforeInitCompleted = false;
            loadBanner(this.mBnLayoutToLoad, this.mBnPlacementToLoad);
            this.mBnLayoutToLoad = null;
            this.mBnPlacementToLoad = null;
        }
    }

    private boolean validateSmash(AbstractSmash smash) {
        return (smash.getMaxAdsPerIteration() >= 1) && (smash.getMaxAdsPerSession() >= 1);
    }

    public void onInitFailed(String reason) {
        try {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "onInitFailed(reason:" + reason + ")", 1);

            if (this.mListenersWrapper != null) {
                for (IronSource.AD_UNIT adUnit : this.mAdUnitsToInitialize) {
                    notifyPublisherAboutInitFailed(adUnit, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStillInProgressAfter15Secs() {
        if (this.mIsBnLoadBeforeInitCompleted) {
            this.mIsBnLoadBeforeInitCompleted = false;
            BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(this.mBnLayoutToLoad, new IronSourceError(603, "init had failed"));
            this.mBnLayoutToLoad = null;
            this.mBnPlacementToLoad = null;
        }
    }

    private void notifyPublisherAboutInitFailed(IronSource.AD_UNIT adUnit, boolean isInitFailed) {
        switch (adUnit) {
            case REWARDED_VIDEO:
                if ((isInitFailed) || (isRewardedVideoConfigurationsReady()) || (this.mRequestedAdUnits.contains(adUnit))) {
                    this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                }
                break;
            case INTERSTITIAL:
                break;
            case OFFERWALL:
                if ((isInitFailed) || (isOfferwallConfigurationsReady()) || (this.mRequestedAdUnits.contains(adUnit)))
                    this.mListenersWrapper.onOfferwallAvailable(false);
                break;
            case BANNER:
                if (this.mIsBnLoadBeforeInitCompleted) {
                    this.mIsBnLoadBeforeInitCompleted = false;
                    BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(this.mBnLayoutToLoad, new IronSourceError(602, "Init had failed"));
                    this.mBnLayoutToLoad = null;
                    this.mBnPlacementToLoad = null;
                }
                break;
        }
    }

    private void prepareEventManagers(Activity activity) {
        if ((this.mEventManagersInit != null) && (this.mEventManagersInit.compareAndSet(false, true))) {
            SuperLooper.getLooper().post(new GeneralPropertiesWorker(activity.getApplicationContext()));

            InterstitialEventsManager.getInstance().start(activity.getApplicationContext(), this.mIronSegment);
            RewardedVideoEventsManager.getInstance().start(activity.getApplicationContext(), this.mIronSegment);
        }
    }

    synchronized void addToRVAdaptersList(AbstractAdapter adapter) {
        if ((this.mRewardedVideoAdaptersList != null) && (adapter != null) && (!this.mRewardedVideoAdaptersList.contains(adapter)))
            this.mRewardedVideoAdaptersList.add(adapter);
    }

    synchronized void addToISAdaptersList(AbstractAdapter adapter) {
        if ((this.mInterstitialAdaptersList != null) && (adapter != null) && (!this.mInterstitialAdaptersList.contains(adapter)))
            this.mInterstitialAdaptersList.add(adapter);
    }

    synchronized void addToBannerAdaptersList(AbstractAdapter adapter) {
        if ((this.mBannerAdaptersList != null) && (adapter != null) && (!this.mBannerAdaptersList.contains(adapter)))
            this.mBannerAdaptersList.add(adapter);
    }

    synchronized void addOWAdapter(AbstractAdapter adapter) {
        this.mOfferwallAdapter = adapter;
    }

    synchronized AbstractAdapter getExistingAdapter(String providerName) {
        try {
            if (this.mRewardedVideoAdaptersList != null) {
                for (AbstractAdapter adapter : this.mRewardedVideoAdaptersList) {
                    if (adapter.getProviderName().equals(providerName)) {
                        return adapter;
                    }
                }
            }
            if (this.mInterstitialAdaptersList != null) {
                for (AbstractAdapter adapter : this.mInterstitialAdaptersList) {
                    if (adapter.getProviderName().equals(providerName)) {
                        return adapter;
                    }
                }
            }
            if (this.mBannerAdaptersList != null) {
                for (AbstractAdapter adapter : this.mBannerAdaptersList) {
                    if (adapter.getProviderName().equals(providerName)) {
                        return adapter;
                    }
                }
            }
            if ((this.mOfferwallAdapter != null) &&
                    (this.mOfferwallAdapter.getProviderName().equals(providerName))) {
                return this.mOfferwallAdapter;
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "getExistingAdapter exception: " + e, 1);
        }
        return null;
    }


    private void initializeManagers() {
        this.mLoggerManager = IronSourceLoggerManager.getLogger(0);
        this.mPublisherLogger = new PublisherLogger(null, 1);
        this.mLoggerManager.addLogger(this.mPublisherLogger);

        this.mListenersWrapper = new ListenersWrapper();

        this.mRewardedVideoManager = new RewardedVideoManager();
        this.mRewardedVideoManager.setRewardedVideoListener(this.mListenersWrapper);
        this.mRewardedVideoManager.setISDemandOnlyRewardedVideoListener(this.mListenersWrapper);

        this.mInterstitialManager = new InterstitialManager();
        this.mInterstitialManager.setInterstitialListener(this.mListenersWrapper);
        this.mInterstitialManager.setRewardedInterstitialListener(this.mListenersWrapper);
        this.mInterstitialManager.setISDemandOnlyInterstitialListener(this.mListenersWrapper);

        this.mOfferwallManager = new OfferwallManager();
        this.mOfferwallManager.setInternalOfferwallListener(this.mListenersWrapper);

        this.mBannerManager = new BannerManager();
    }


    public void onResume(Activity activity) {
        String logMessage = "onResume()";
        try {
            this.mActivity = activity;
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            if (this.mRewardedVideoManager != null) {
                this.mRewardedVideoManager.onResume(activity);
            }
            if (this.mInterstitialManager != null) {
                this.mInterstitialManager.onResume(activity);
            }
            if (this.mBannerManager != null) {
                this.mBannerManager.onResume(activity);
            }
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
        }
    }

    public void onPause(Activity activity) {
        String logMessage = "onPause()";
        try {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            if (this.mRewardedVideoManager != null) {
                this.mRewardedVideoManager.onPause(activity);
            }
            if (this.mInterstitialManager != null) {
                this.mInterstitialManager.onPause(activity);
            }
            if (this.mBannerManager != null) {
                this.mBannerManager.onPause(activity);
            }
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
        }
    }

    public synchronized void setAge(int age) {
        try {
            String logMessage = this.TAG + ":setAge(age:" + age + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            ConfigValidationResult result = new ConfigValidationResult();

            validateAge(age, result);

            if (result.isValid()) {
                this.mUserAge = Integer.valueOf(age);
            } else
                IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, result.getIronSourceError().toString(), 2);
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setAge(age:" + age + ")", e);
        }
    }

    public synchronized void setGender(String gender) {
        try {
            String logMessage = this.TAG + ":setGender(gender:" + gender + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            ConfigValidationResult result = new ConfigValidationResult();

            validateGender(gender, result);

            if (result.isValid()) {
                this.mUserGender = gender;
            } else
                IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, result.getIronSourceError().toString(), 2);
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setGender(gender:" + gender + ")", e);
        }
    }

    public void setMediationSegment(String segment) {
        try {
            String logMessage = this.TAG + ":setMediationSegment(segment:" + segment + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            ConfigValidationResult result = new ConfigValidationResult();

            validateSegment(segment, result);

            if (result.isValid()) {
                this.mSegment = segment;
            } else
                IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, result.getIronSourceError().toString(), 2);
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setMediationSegment(segment:" + segment + ")", e);
        }
    }

    public void setSegment(IronSourceSegment segment) {
        if ((MediationInitializer.getInstance().getCurrentInitStatus() == MediationInitializer.EInitStatus.INIT_IN_PROGRESS) ||
                (MediationInitializer.getInstance().getCurrentInitStatus() == MediationInitializer.EInitStatus.INITIATED)) {
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, "Segments must be set prior to Init. Setting a segment after the init will be ignored", 0);
        } else {
            this.mIronSegment = segment;
        }
    }

    public boolean setDynamicUserId(String dynamicUserId) {
        try {
            String logMessage = this.TAG + ":setDynamicUserId(dynamicUserId:" + dynamicUserId + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            ConfigValidationResult result = new ConfigValidationResult();

            validateDynamicUserId(dynamicUserId, result);

            if (result.isValid()) {
                this.mDynamicUserId = dynamicUserId;
                return true;
            }
            IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, result.getIronSourceError().toString(), 2);
            return false;
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setDynamicUserId(dynamicUserId:" + dynamicUserId + ")", e);
        }
        return false;
    }


    public void setAdaptersDebug(boolean enabled) {
        IronSourceLoggerManager.getLogger().setAdaptersDebug(enabled);
    }

    public void setMediationType(String mediationType) {
        try {
            String logMessage = this.TAG + ":setMediationType(mediationType:" + mediationType + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, logMessage, 1);


            if ((validateLength(mediationType, 1, 64)) && (validateAlphanumeric(mediationType))) {
                this.mMediationType = mediationType;
            } else {
                logMessage = " mediationType value is invalid - should be alphanumeric and 1-64 chars in length";
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, logMessage, 1);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setMediationType(mediationType:" + mediationType + ")", e);
        }
    }


    public synchronized Integer getAge() {
        return this.mUserAge;
    }

    public synchronized String getGender() {
        return this.mUserGender;
    }

    synchronized String getMediationSegment() {
        return this.mSegment;
    }

    synchronized String getDynamicUserId() {
        return this.mDynamicUserId;
    }

    synchronized Map<String, String> getRvServerParams() {
        return this.mRvServerParams;
    }

    public synchronized String getMediationType() {
        return this.mMediationType;
    }


    public void initRewardedVideo(Activity activity, String appKey, String userId) {
    }


    public void initInterstitial(Activity activity, String appKey, String userId) {
    }


    public void initOfferwall(Activity activity, String appKey, String userId) {
    }


    public void showRewardedVideo() {
        String logMessage = "showRewardedVideo()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in demand only mode. Use showISDemandOnlyRewardedVideo instead", 3);
                return;
            }

            if (!isRewardedVideoConfigurationsReady()) {
                this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
                return;
            }

            Placement defaultPlacement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
            if (defaultPlacement != null) {
                String placementName = defaultPlacement.getPlacementName();
                showRewardedVideo(placementName);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
        }
    }

    public void showRewardedVideo(String placementName) {
        String logMessage = "showRewardedVideo(" + placementName + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in demand only mode. Use showISDemandOnlyRewardedVideo instead", 3);
                return;
            }

            if (!isRewardedVideoConfigurationsReady()) {
                this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
                return;
            }

            Placement placement = getPlacementToShowWithEvent(placementName);

            if (placement != null) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
                try {
                    data.put("placement", placement.getPlacementName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                EventData event = new EventData(2, data);
                RewardedVideoEventsManager.getInstance().log(event);

                this.mRewardedVideoManager.setCurrentPlacement(placement);
                this.mRewardedVideoManager.showRewardedVideo(placement.getPlacementName());
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildInitFailedError("showRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
        }
    }

    public boolean isRewardedVideoAvailable() {
        boolean isAvailable = false;
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in demand only mode. Use isISDemandOnlyRewardedVideoAvailable instead", 3);
                return false;
            }

            isAvailable = this.mRewardedVideoManager.isRewardedVideoAvailable();

            JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
            try {
                data.put("status", String.valueOf(isAvailable));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            EventData event = new EventData(18, data);
            RewardedVideoEventsManager.getInstance().log(event);

            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isRewardedVideoAvailable():" + isAvailable, 1);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isRewardedVideoAvailable():" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, "isRewardedVideoAvailable()", e);
            isAvailable = false;
        }

        return isAvailable;
    }

    public void setRewardedVideoListener(RewardedVideoListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setRewardedVideoListener(RVListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setRewardedVideoListener(RVListener)", 1);
        }


        this.mListenersWrapper.setRewardedVideoListener(listener);
    }

    public void setRewardedVideoServerParameters(Map<String, String> params) {
        try {
            if ((params == null) || (params.size() == 0)) {
                return;
            }

            String logMessage = this.TAG + ":setRewardedVideoServerParameters(params:" + params.toString() + ")";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
            this.mRvServerParams = new HashMap(params);
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":setRewardedVideoServerParameters(params:" + params.toString() + ")", e);
        }
    }

    public void clearRewardedVideoServerParameters() {
        this.mRvServerParams = null;
    }


    public void showISDemandOnlyRewardedVideo(String instanceId) {
        String logMessage = "showISDemandOnlyRewardedVideo(" + instanceId + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in mediation mode. Use showRewardedVideo instead", 3);
                return;
            }

            if (!isRewardedVideoConfigurationsReady()) {
                this.mListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
                return;
            }

            Placement defaultPlacement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
            if (defaultPlacement != null) {
                String placementName = defaultPlacement.getPlacementName();
                showISDemandOnlyRewardedVideo(instanceId, placementName);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
        }
    }

    public void showISDemandOnlyRewardedVideo(String instanceId, String placementName) {
        String logMessage = "showISDemandOnlyRewardedVideo(" + instanceId + (placementName == null ? ")" : new StringBuilder().append(" , ").append(placementName).append(")").toString());
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in mediation mode. Use showRewardedVideo instead", 3);
                return;
            }

            if (!isRewardedVideoConfigurationsReady()) {
                this.mListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
                return;
            }

            Placement placement = getPlacementToShowWithEvent(placementName);

            if (placement != null) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
                try {
                    data.put("placement", placement.getPlacementName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                EventData event = new EventData(2, data);
                RewardedVideoEventsManager.getInstance().log(event);

                this.mRewardedVideoManager.setCurrentPlacement(placement);
                this.mRewardedVideoManager.showRewardedVideo(instanceId, placement.getPlacementName());
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyRewardedVideo can't be called before the Rewarded Video ad unit initialization completed successfully", "Rewarded Video"));
        }
    }

    public boolean isISDemandOnlyRewardedVideoAvailable(String instanceId) {
        boolean isAvailable = false;
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.REWARDED_VIDEO)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Rewarded Video was initialized in mediation mode. Use isRewardedVideoAvailable instead", 3);
                return false;
            }

            isAvailable = this.mRewardedVideoManager.isRewardedVideoAvailable(instanceId);

            JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
            try {
                data.put("status", String.valueOf(isAvailable));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            EventData event = new EventData(18, data);
            RewardedVideoEventsManager.getInstance().log(event);

            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyRewardedVideoAvailable():" + isAvailable, 1);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyRewardedVideoAvailable():" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyRewardedVideoAvailable()", e);
            isAvailable = false;
        }

        return isAvailable;
    }

    void setISDemandOnlyRewardedVideoListener(ISDemandOnlyRewardedVideoListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setISDemandOnlyRewardedVideoListener(ISDemandOnlyRewardedVideoListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setISDemandOnlyRewardedVideoListener(ISDemandOnlyRewardedVideoListener)", 1);
        }

        this.mListenersWrapper.setISDemandOnlyRewardedVideoListener(listener);
    }


    private boolean isRewardedVideoConfigurationsReady() {
        return (this.mCurrentServerResponse != null) && (this.mCurrentServerResponse.getConfigurations() != null) && (this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations() != null);
    }

    private Placement getPlacementToShowWithEvent(String placementName) {
        Placement placement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
        if (placement == null) {
            String noPlacementMessage = "Placement is not valid, please make sure you are using the right placements, using the default placement.";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noPlacementMessage, 3);


            placement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();
            if (placement == null) {
                String noDefaultPlacement = "Default placement was not found, please make sure you are using the right placements.";
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noDefaultPlacement, 3);
                return null;
            }
        }

        ECappingStatus cappingStatus = CappingManager.isPlacementCapped(this.mActivity, placement);
        String cappedMessage = getCappingMessage(placement.getPlacementName(), cappingStatus);
        if (!TextUtils.isEmpty(cappedMessage)) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, cappedMessage, 1);
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildCappedPerPlacementError("Rewarded Video", cappedMessage));
            return null;
        }

        return placement;
    }


    public void loadInterstitial() {
        String logMessage = "loadInterstitial()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in demand only mode. Use loadISDemandOnlyInterstitial instead", 3);
                return;
            }

            if (!this.mDidInitInterstitial) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() must be called before loadInterstitial()", 3);
                return;
            }

            this.mInterstitialManager.loadInterstitial();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
        }
    }

    public void showInterstitial() {
        String logMessage = "showInterstitial()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in demand only mode. Use showISDemandOnlyInterstitial instead", 3);
                return;
            }

            if (!isInterstitialConfigurationsReady()) {
                this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
                return;
            }

            InterstitialPlacement defaultPlacement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();
            if (defaultPlacement != null) {
                String placementName = defaultPlacement.getPlacementName();
                showInterstitial(placementName);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
        }
    }

    public void showInterstitial(String placementName) {
        String logMessage = "showInterstitial(" + placementName + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in demand only mode. Use showISDemandOnlyInterstitial instead", 3);
                return;
            }

            if (!isInterstitialConfigurationsReady()) {
                this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
                return;
            }

            InterstitialPlacement placement = getInterstitialPlacementToShowWithEvent(placementName);

            if (placement != null) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
                try {
                    data.put("placement", placement.getPlacementName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                EventData event = new EventData(23, data);
                InterstitialEventsManager.getInstance().log(event);

                this.mInterstitialManager.setCurrentPlacement(placement);
                this.mInterstitialManager.showInterstitial(placement.getPlacementName());
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildInitFailedError("showInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
        }
    }

    public boolean isInterstitialReady() {
        boolean isAvailable = false;
        try {
            if (this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in demand only mode. Use isISDemandOnlyInterstitialReady instead", 3);
                return false;
            }

            isAvailable = this.mInterstitialManager.isInterstitialReady();

            JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
            try {
                data.put("status", String.valueOf(isAvailable));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            EventData event = new EventData(30, data);
            InterstitialEventsManager.getInstance().log(event);

            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isInterstitialReady():" + isAvailable, 1);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isInterstitialReady():" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, "isInterstitialReady()", e);
            isAvailable = false;
        }

        return isAvailable;
    }

    public void setInterstitialListener(InterstitialListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setInterstitialListener(ISListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setInterstitialListener(ISListener)", 1);
        }

        this.mListenersWrapper.setInterstitialListener(listener);
    }


    public void loadISDemandOnlyInterstitial(String instanceId) {
        String logMessage = "loadISDemandOnlyInterstitial(" + instanceId + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in mediation mode. Use loadInterstitial instead", 3);
                return;
            }

            if (!this.mDidInitInterstitial) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() must be called before loadInterstitial()", 3);
                return;
            }

            this.mInterstitialManager.loadInterstitial(instanceId);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
        }
    }

    public void showISDemandOnlyInterstitial(String instanceId) {
        String logMessage = "showISDemandOnlyInterstitial(" + instanceId + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in mediation mode. Use showInterstitial instead", 3);
                return;
            }

            if (!isInterstitialConfigurationsReady()) {
                this.mListenersWrapper.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
                return;
            }

            InterstitialPlacement defaultPlacement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();
            if (defaultPlacement != null) {
                String placementName = defaultPlacement.getPlacementName();
                showISDemandOnlyInterstitial(instanceId, placementName);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
        }
    }

    public void showISDemandOnlyInterstitial(String instanceId, String placementName) {
        String logMessage = "showISDemandOnlyInterstitial(" + instanceId + (placementName == null ? ")" : new StringBuilder().append(" , ").append(placementName).append(")").toString());
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in mediation mode. Use showInterstitial instead", 3);
                return;
            }

            if (!isInterstitialConfigurationsReady()) {
                this.mListenersWrapper.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
                return;
            }

            InterstitialPlacement placement = getInterstitialPlacementToShowWithEvent(placementName);

            if (placement != null) {
                JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
                try {
                    data.put("placement", placement.getPlacementName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                EventData event = new EventData(23, data);
                InterstitialEventsManager.getInstance().log(event);

                this.mInterstitialManager.setCurrentPlacement(placement);
                this.mInterstitialManager.showInterstitial(instanceId, placement.getPlacementName());
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildInitFailedError("showISDemandOnlyInterstitial can't be called before the Interstitial ad unit initialization completed successfully", "Interstitial"));
        }
    }

    public boolean isISDemandOnlyInterstitialReady(String instanceId) {
        boolean isAvailable = false;
        try {
            if (!this.mDemandOnlyAdUnits.contains(IronSource.AD_UNIT.INTERSTITIAL)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Interstitial was initialized in mediation mode. Use isInterstitialReady instead", 3);
                return false;
            }

            isAvailable = this.mInterstitialManager.isInterstitialReady(instanceId);

            JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
            try {
                data.put("status", String.valueOf(isAvailable));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            EventData event = new EventData(30, data);
            InterstitialEventsManager.getInstance().log(event);

            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyInterstitialReady(instanceId: " + instanceId + "):" + isAvailable, 1);
        } catch (Throwable e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyInterstitialReady(instanceId: " + instanceId + "):" + isAvailable, 1);
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, "isISDemandOnlyInterstitialReady(instanceId: " + instanceId + ")", e);
            isAvailable = false;
        }

        return isAvailable;
    }

    public void setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener listener) {
        if (listener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener)", 1);
        }

        this.mListenersWrapper.setISDemandOnlyInterstitialListener(listener);
    }


    private boolean isInterstitialConfigurationsReady() {
        return (this.mCurrentServerResponse != null) && (this.mCurrentServerResponse.getConfigurations() != null) && (this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations() != null);
    }

    private InterstitialPlacement getInterstitialPlacementToShowWithEvent(String placementName) {
        InterstitialPlacement placement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);

        if (placement == null) {
            String noPlacementMessage = "Placement is not valid, please make sure you are using the right placements, using the default placement.";
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noPlacementMessage, 3);


            placement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();

            if (placement == null) {
                String noDefaultPlacement = "Default placement was not found, please make sure you are using the right placements.";
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noDefaultPlacement, 3);
                return null;
            }
        }

        ECappingStatus cappingStatus = getInterstitialCappingStatus(placement.getPlacementName());
        String cappedMessage = getCappingMessage(placement.getPlacementName(), cappingStatus);
        if (!TextUtils.isEmpty(cappedMessage)) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, cappedMessage, 1);
            this.mListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildCappedPerPlacementError("Interstitial", cappedMessage));
            return null;
        }

        return placement;
    }


    private boolean isOfferwallConfigurationsReady() {
        return (this.mCurrentServerResponse != null) && (this.mCurrentServerResponse.getConfigurations() != null) && (this.mCurrentServerResponse.getConfigurations().getOfferwallConfigurations() != null);
    }

    public void showOfferwall() {
        String logMessage = "showOfferwall()";
        try {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

            if (!isOfferwallConfigurationsReady()) {
                this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", "Offerwall"));
                return;
            }

            OfferwallPlacement defaultPlacement = this.mCurrentServerResponse.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
            if (defaultPlacement != null) {
                String placementName = defaultPlacement.getPlacementName();
                showOfferwall(placementName);
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", "Offerwall"));
        }
    }

    public void showOfferwall(String placementName) {
        String logMessage = "showOfferwall(" + placementName + ")";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            if (!isOfferwallConfigurationsReady()) {
                this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", "Offerwall"));
                return;
            }

            OfferwallPlacement placement = this.mCurrentServerResponse.getConfigurations().getOfferwallConfigurations().getOfferwallPlacement(placementName);
            if (placement == null) {
                String noPlacementMessage = "Placement is not valid, please make sure you are using the right placements, using the default placement.";
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noPlacementMessage, 3);


                placement = this.mCurrentServerResponse.getConfigurations().getOfferwallConfigurations().getDefaultOfferwallPlacement();
                if (placement == null) {
                    String noDefaultPlacement = "Default placement was not found, please make sure you are using the right placements.";
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noDefaultPlacement, 3);
                    return;
                }
            }

            this.mOfferwallManager.showOfferwall(placement.getPlacementName());
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
            this.mListenersWrapper.onOfferwallShowFailed(ErrorBuilder.buildInitFailedError("showOfferwall can't be called before the Offerwall ad unit initialization completed successfully", "Offerwall"));
        }
    }

    public boolean isOfferwallAvailable() {
        boolean result = false;
        try {
            if (this.mOfferwallManager != null)
                result = this.mOfferwallManager.isOfferwallAvailable();
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    public void getOfferwallCredits() {
        String logMessage = "getOfferwallCredits()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);
        try {
            this.mOfferwallManager.getOfferwallCredits();
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, logMessage, e);
        }
    }

    public void setOfferwallListener(OfferwallListener offerwallListener) {
        if (offerwallListener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setOfferwallListener(OWListener:null)", 1);
        } else {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setOfferwallListener(OWListener)", 1);
        }

        this.mListenersWrapper.setOfferwallListener(offerwallListener);
    }


    public void setLogListener(LogListener logListener) {
        if (logListener == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setLogListener(LogListener:null)", 1);
        } else {
            this.mPublisherLogger.setLogListener(logListener);
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "setLogListener(LogListener:" + logListener.getClass().getSimpleName() + ")", 1);
        }
    }


    public void setRewardedInterstitialListener(RewardedInterstitialListener listener) {
        this.mListenersWrapper.setRewardedInterstitialListener(listener);
    }


    private boolean isBannerConfigurationsReady() {
        return (this.mCurrentServerResponse != null) && (this.mCurrentServerResponse.getConfigurations() != null) && (this.mCurrentServerResponse.getConfigurations().getBannerConfigurations() != null);
    }

    public IronSourceBannerLayout createBanner(Activity activity, EBannerSize size) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "createBanner()", 1);
        if (activity == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "createBanner() : Activity cannot be null", 3);
            return null;
        }
        return this.mBannerManager.createBanner(activity, size);
    }

    public void loadBanner(IronSourceBannerLayout banner, String placementName) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "loadBanner(" + placementName + ")", 1);

        if (banner == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "loadBanner can't be called with a null parameter", 1);
            return;
        }

        if (!this.mDidInitBanner) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() must be called before loadBanner()", 3);
            return;
        }

        MediationInitializer.EInitStatus initStatus = MediationInitializer.getInstance().getCurrentInitStatus();

        if (initStatus == MediationInitializer.EInitStatus.INIT_FAILED) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
            BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(banner, new IronSourceError(600, "Init had failed"));
            return;
        }

        if (initStatus == MediationInitializer.EInitStatus.INIT_IN_PROGRESS) {
            if (MediationInitializer.getInstance().isInProgressMoreThan15Secs()) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
                BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(banner, new IronSourceError(601, "Init had failed"));
            } else {
                this.mBnLayoutToLoad = banner;
                this.mIsBnLoadBeforeInitCompleted = true;
                this.mBnPlacementToLoad = placementName;
            }

            return;
        }


        if ((this.mCurrentServerResponse == null) || (this.mCurrentServerResponse.getConfigurations() == null) || (this.mCurrentServerResponse.getConfigurations().getBannerConfigurations() == null)) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "No banner configurations found", 3);
            BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(banner, new IronSourceError(615, "No banner configurations found"));
            return;
        }

        this.mBannerManager.loadBanner(banner, getBannerPlacement(placementName));
    }

    public void loadBanner(IronSourceBannerLayout banner) {
        loadBanner(banner, "");
    }

    public void destroyBanner(IronSourceBannerLayout banner) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "destroyBanner()", 1);
        try {
            this.mBannerManager.destroyBanner(banner);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, "destroyBanner()", e);
        }
    }


    ServerResponseWrapper getServerResponse(Context context, String userId, IResponseListener listener) {
        synchronized(this.mServerResponseLocker) {
            if(this.mCurrentServerResponse != null) {
                return new ServerResponseWrapper(this.mCurrentServerResponse);
            } else {
                ServerResponseWrapper response = this.connectAndGetServerResponse(context, userId, listener);
                if(response == null || !response.isValidResponse()) {
                    response = this.getCachedResponse(context, userId);
                }

                if(response != null) {
                    this.mCurrentServerResponse = response;
                    IronSourceUtils.saveLastResponse(context, response.toString());
                    this.initializeSettingsFromServerResponse(this.mCurrentServerResponse, context);
                }

                InterstitialEventsManager.getInstance().setHasServerResponse(true);
                RewardedVideoEventsManager.getInstance().setHasServerResponse(true);
                return response;
            }
        }
    }

    private ServerResponseWrapper getCachedResponse(Context context, String userId) {
        ServerResponseWrapper response = null;


        String cachedResponseString = IronSourceUtils.getLastResponse(context);
        JSONObject cachedJsonObject;
        try {
            cachedJsonObject = new JSONObject(cachedResponseString);
        } catch (JSONException e) {
            cachedJsonObject = new JSONObject();
        }

        String cachedAppKey = cachedJsonObject.optString("appKey");
        String cachedUserId = cachedJsonObject.optString("userId");
        String cachedSettings = cachedJsonObject.optString("response");

        if ((!TextUtils.isEmpty(cachedAppKey)) &&
                (!TextUtils.isEmpty(cachedUserId)) &&
                (!TextUtils.isEmpty(cachedSettings))) {


            if ((getIronSourceAppKey() != null) && (cachedAppKey.equals(getIronSourceAppKey())) && (cachedUserId.equals(userId))) {
                response = new ServerResponseWrapper(context, cachedAppKey, cachedUserId, cachedSettings);

                IronSourceError sse = ErrorBuilder.buildUsingCachedConfigurationError(cachedAppKey, cachedUserId);
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, sse.toString(), 1);


                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, sse.toString() + ": " + response.toString(), 0);
            }
        }

        return response;
    }

    private ServerResponseWrapper connectAndGetServerResponse(Context context, String userId, IResponseListener listener) {
        if (!IronSourceUtils.isNetworkConnected(context)) {
            return null;
        }

        ServerResponseWrapper response = null;
        try {
            String gaid = getAdvertiserId(context);
            if (TextUtils.isEmpty(gaid)) {
                gaid = DeviceStatus.getOrGenerateOnceUniqueIdentifier(context);
                IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.INTERNAL, "using custom identifier", 1);
            }
            Vector<Pair<String, String>> additionParams = null;
            if (this.mIronSegment != null) {
                additionParams = this.mIronSegment.getSegmentData();
            }
            String mediationType = getMediationType();

            String serverResponseString = HttpFunctions.getStringFromURL(ServerURL.getCPVProvidersURL(context, getIronSourceAppKey(), userId, gaid, mediationType, additionParams), listener);

            if (serverResponseString == null) {
                return null;
            }

            if (IronSourceUtils.getSerr() == 1) {
                JSONObject encryptedResponseJson = new JSONObject(serverResponseString);

                String encryptedResponse = encryptedResponseJson.optString("response", null);

                if (TextUtils.isEmpty(encryptedResponse)) {
                    return null;
                }

                serverResponseString = IronSourceAES.decode("C38FB23A402222A0C17D34A92F971D1F", encryptedResponse);
            }

            response = new ServerResponseWrapper(context, getIronSourceAppKey(), userId, serverResponseString);


            if (!response.isValidResponse()) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private void initializeSettingsFromServerResponse(ServerResponseWrapper response, Context context) {
        initializeLoggerManager(response);
        initializeEventsSettings(response, context);
    }

    private void initializeEventsSettings(ServerResponseWrapper response, Context context) {
        boolean isRVEventsEnabled = false;
        if (isRewardedVideoConfigurationsReady()) {
            isRVEventsEnabled = response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().isEventsEnabled();
        }

        boolean isISEventsEnabled = false;
        if (isInterstitialConfigurationsReady()) {
            isISEventsEnabled = response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().isEventsEnabled();
        }

        boolean isBNEventsEnabled = false;
        if (isBannerConfigurationsReady()) {
            isBNEventsEnabled = response.getConfigurations().getBannerConfigurations().getBannerEventsConfigurations().isEventsEnabled();
        }

        if (isRVEventsEnabled) {
            RewardedVideoEventsManager.getInstance().setFormatterType(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsType(), context);
            RewardedVideoEventsManager.getInstance().setEventsUrl(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsURL(), context);
            RewardedVideoEventsManager.getInstance().setMaxNumberOfEvents(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getMaxNumberOfEvents());
            RewardedVideoEventsManager.getInstance().setMaxEventsPerBatch(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getMaxEventsPerBatch());
            RewardedVideoEventsManager.getInstance().setBackupThreshold(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getEventsBackupThreshold());
            RewardedVideoEventsManager.getInstance().setOptOutEvents(response.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoEventsConfigurations().getOptOutEvents(), context);
            RewardedVideoEventsManager.getInstance().setServerSegmentData(response.getConfigurations().getApplicationConfigurations().getSegmetData());
        } else {
            RewardedVideoEventsManager.getInstance().setIsEventsEnabled(false);
        }
        if (isISEventsEnabled) {
            InterstitialEventsManager.getInstance().setFormatterType(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsType(), context);
            InterstitialEventsManager.getInstance().setEventsUrl(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsURL(), context);
            InterstitialEventsManager.getInstance().setMaxNumberOfEvents(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getMaxNumberOfEvents());
            InterstitialEventsManager.getInstance().setMaxEventsPerBatch(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getMaxEventsPerBatch());
            InterstitialEventsManager.getInstance().setBackupThreshold(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getEventsBackupThreshold());
            InterstitialEventsManager.getInstance().setOptOutEvents(response.getConfigurations().getInterstitialConfigurations().getInterstitialEventsConfigurations().getOptOutEvents(), context);
            InterstitialEventsManager.getInstance().setServerSegmentData(response.getConfigurations().getApplicationConfigurations().getSegmetData());
        } else if (isBNEventsEnabled) {
            ApplicationEvents config = response.getConfigurations().getBannerConfigurations().getBannerEventsConfigurations();
            InterstitialEventsManager.getInstance().setFormatterType(config.getEventsType(), context);
            InterstitialEventsManager.getInstance().setEventsUrl(config.getEventsURL(), context);
            InterstitialEventsManager.getInstance().setMaxNumberOfEvents(config.getMaxNumberOfEvents());
            InterstitialEventsManager.getInstance().setMaxEventsPerBatch(config.getMaxEventsPerBatch());
            InterstitialEventsManager.getInstance().setBackupThreshold(config.getEventsBackupThreshold());
            InterstitialEventsManager.getInstance().setOptOutEvents(config.getOptOutEvents(), context);
            InterstitialEventsManager.getInstance().setServerSegmentData(response.getConfigurations().getApplicationConfigurations().getSegmetData());
        } else {
            InterstitialEventsManager.getInstance().setIsEventsEnabled(false);
        }
    }

    private void initializeLoggerManager(ServerResponseWrapper response) {
        this.mPublisherLogger.setDebugLevel(response.getConfigurations().getApplicationConfigurations().getLoggerConfigurations().getPublisherLoggerLevel());
        this.mLoggerManager.setLoggerDebugLevel("console", response.getConfigurations().getApplicationConfigurations().getLoggerConfigurations().getConsoleLoggerLevel());
    }


    public void removeRewardedVideoListener() {
        String logMessage = "removeRewardedVideoListener()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

        this.mListenersWrapper.setRewardedVideoListener(null);
    }

    public void removeInterstitialListener() {
        String logMessage = "removeInterstitialListener()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

        this.mListenersWrapper.setInterstitialListener(null);
    }

    public void removeOfferwallListener() {
        String logMessage = "removeOfferwallListener()";
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, logMessage, 1);

        this.mListenersWrapper.setOfferwallListener(null);
    }

    synchronized void setIronSourceUserId(String userId) {
        this.mUserId = userId;
    }

    public synchronized String getIronSourceAppKey() {
        return this.mAppKey;
    }

    public synchronized String getIronSourceUserId() {
        return this.mUserId;
    }

    private ConfigValidationResult validateAppKey(String appKey) {
        ConfigValidationResult result = new ConfigValidationResult();


        if (appKey != null) {
            if (validateLength(appKey, 5, 10)) {
                if (!validateAlphanumeric(appKey)) {
                    IronSourceError error = ErrorBuilder.buildInvalidCredentialsError("appKey", appKey, "should contain only english characters and numbers");

                    result.setInvalid(error);
                }
            } else {
                IronSourceError error = ErrorBuilder.buildInvalidCredentialsError("appKey", appKey, "length should be between 5-10 characters");

                result.setInvalid(error);
            }
        } else {
            IronSourceError error = new IronSourceError(506, "Init Fail - appKey is missing");
            result.setInvalid(error);
        }

        return result;
    }

    private void validateGender(String gender, ConfigValidationResult result) {
        try {
            if (gender != null) {
                gender = gender.toLowerCase().trim();

                if ((!"male".equals(gender)) &&
                        (!"female".equals(gender)) &&
                        (!"unknown".equals(gender)))
                    result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", "SupersonicAds", "gender value should be one of male/female/unknown."));
            }
        } catch (Exception e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("gender", "SupersonicAds", "gender value should be one of male/female/unknown."));
        }
    }


    private void validateAge(int age, ConfigValidationResult result) {
        try {
            if ((age < 5) || (age > 120)) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("age", "SupersonicAds", "age value should be between 5-120"));
            }
        } catch (NumberFormatException e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("age", "SupersonicAds", "age value should be between 5-120"));
        }
    }

    private void validateSegment(String segment, ConfigValidationResult result) {
        try {
            if ((segment != null) &&
                    (segment.length() > 64)) {
                result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("segment", "SupersonicAds", "segment value should not exceed 64 characters."));
            }
        } catch (Exception e) {
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("segment", "SupersonicAds", "segment value should not exceed 64 characters."));
        }
    }

    private void validateDynamicUserId(String dynamicUserId, ConfigValidationResult result) {
        if (!validateLength(dynamicUserId, 1, 128))
            result.setInvalid(ErrorBuilder.buildInvalidKeyValueError("dynamicUserId", "SupersonicAds", "dynamicUserId is invalid, should be between 1-128 chars in length."));
    }

    private boolean validateLength(String key, int minLength, int maxLength) {
        return (key != null) && (key.length() >= minLength) && (key.length() <= maxLength);
    }

    private boolean validateAlphanumeric(String key) {
        if (key == null) {
            return false;
        }
        String pattern = "^[a-zA-Z0-9]*$";
        return key.matches(pattern);
    }

    public InterstitialPlacement getInterstitialPlacementInfo(String placementName) {
        InterstitialPlacement result = null;
        try {
            result = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "getPlacementInfo(placement: " + placementName + "):" + result, 1);
        } catch (Exception localException) {
        }

        return result;
    }

    public Placement getRewardedVideoPlacementInfo(String placementName) {
        Placement result = null;
        try {
            result = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "getPlacementInfo(placement: " + placementName + "):" + result, 1);
        } catch (Exception localException) {
        }

        return result;
    }

    public String getAdvertiserId(Context context) {
        try {
            String[] deviceInfo = DeviceStatus.getAdvertisingIdInfo(context);
            if ((deviceInfo.length > 0) && (deviceInfo[0] != null))
                return deviceInfo[0];
        } catch (Exception e) {
            return "";
        }

        return "";
    }

    public void shouldTrackNetworkState(Context context, boolean track) {
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.shouldTrackNetworkState(context, track);
        }
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.shouldTrackNetworkState(context, track);
        }
    }

    boolean isInterstitialPlacementCapped(String placementName) {
        boolean isCapped = false;

        ECappingStatus cappingStatus = getInterstitialCappingStatus(placementName);

        if (cappingStatus != null) {
            switch (cappingStatus) {
                case CAPPED_PER_DELIVERY:
                case CAPPED_PER_COUNT:
                case CAPPED_PER_PACE:
                    isCapped = true;
                    break;
            }

        }


        sendIsCappedEvent("Interstitial", isCapped);

        return isCapped;
    }

    boolean isRewardedVideoPlacementCapped(String placementName) {
        boolean isCapped = false;

        ECappingStatus cappingStatus = getRewardedVideoCappingStatus(placementName);

        if (cappingStatus != null) {
            switch (cappingStatus) {
                case CAPPED_PER_DELIVERY:
                case CAPPED_PER_COUNT:
                case CAPPED_PER_PACE:
                    isCapped = true;
                    break;
            }

        }


        sendIsCappedEvent("Rewarded Video", isCapped);

        return isCapped;
    }

    boolean isBannerPlacementCapped(String placementName) {
        if ((this.mCurrentServerResponse == null) || (this.mCurrentServerResponse.getConfigurations() == null) ||
                (this.mCurrentServerResponse.getConfigurations().getBannerConfigurations() == null)) {
            return false;
        }
        BannerPlacement placement = null;
        try {
            placement = this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getBannerPlacement(placementName);

            if (placement == null) {
                placement = this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getDefaultBannerPlacement();

                if (placement == null) {
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Banner default placement was not found", 3);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (placement == null) {
            return false;
        }

        return CappingManager.isBnPlacementCapped(this.mActivity, placement.getPlacementName());
    }

    private ECappingStatus getInterstitialCappingStatus(String placementName) {
        if ((this.mCurrentServerResponse == null) || (this.mCurrentServerResponse.getConfigurations() == null) ||
                (this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations() == null)) {
            return ECappingStatus.NOT_CAPPED;
        }
        InterstitialPlacement placement = null;
        try {
            placement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getInterstitialPlacement(placementName);

            if (placement == null) {
                placement = this.mCurrentServerResponse.getConfigurations().getInterstitialConfigurations().getDefaultInterstitialPlacement();

                if (placement == null) {
                    String noDefaultPlacement = "Default placement was not found";
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noDefaultPlacement, 3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (placement == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        return CappingManager.isPlacementCapped(this.mActivity, placement);
    }

    private ECappingStatus getRewardedVideoCappingStatus(String placementName) {
        if ((this.mCurrentServerResponse == null) || (this.mCurrentServerResponse.getConfigurations() == null) ||
                (this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations() == null)) {
            return ECappingStatus.NOT_CAPPED;
        }
        Placement placement = null;
        try {
            placement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getRewardedVideoPlacement(placementName);

            if (placement == null) {
                placement = this.mCurrentServerResponse.getConfigurations().getRewardedVideoConfigurations().getDefaultRewardedVideoPlacement();

                if (placement == null) {
                    String noDefaultPlacement = "Default placement was not found";
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, noDefaultPlacement, 3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (placement == null) {
            return ECappingStatus.NOT_CAPPED;
        }
        return CappingManager.isPlacementCapped(this.mActivity, placement);
    }

    private void sendIsCappedEvent(String adUnit, boolean isCapped) {
        if (!isCapped) {


            return;
        }

        boolean isDemandOnly = false;

        if ((adUnit.equals("Interstitial")) && (this.mInterstitialManager != null)) {
            isDemandOnly = this.mInterstitialManager.mIsInISDemandOnlyMode;
        } else if ((adUnit.equals("Rewarded Video")) && (this.mRewardedVideoManager != null)) {
            isDemandOnly = this.mRewardedVideoManager.mIsInISDemandOnlyMode;
        }
        JSONObject data = IronSourceUtils.getMediationAdditionalData(isDemandOnly);
        try {
            data.put("reason", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if ("Interstitial".equals(adUnit)) {
            EventData event = new EventData(34, data);
            InterstitialEventsManager.getInstance().log(event);
        } else if ("Rewarded Video".equals(adUnit)) {
            EventData event = new EventData(20, data);
            RewardedVideoEventsManager.getInstance().log(event);
        }
    }

    String getCappingMessage(String placementName, ECappingStatus cappingStatus) {
        if (cappingStatus == null) {
            return null;
        }
        switch (cappingStatus) {
            case CAPPED_PER_DELIVERY:
                return "Placement " + placementName + " is capped by disabled delivery";
            case CAPPED_PER_COUNT:
                return "Placement " + placementName + " has reached its capping limit";
            case CAPPED_PER_PACE:
                return "Placement " + placementName + " has reached its limit as defined per pace";
        }

        return null;
    }

    ServerResponseWrapper getCurrentServerResponse() {
        return this.mCurrentServerResponse;
    }

    void setSegmentListener(SegmentListener listener) {
        if (this.mListenersWrapper != null) {
            this.mListenersWrapper.setSegmentListener(listener);
            MediationInitializer.getInstance().setSegmentListener(this.mListenersWrapper);
        }
    }

    HashSet<String> getAllSettingsForProvider(String providerName, String fieldName) {
        if (this.mCurrentServerResponse == null) {
            return new HashSet();
        }
        return this.mCurrentServerResponse.getProviderSettingsHolder().getProviderSettingsByReflectionName(providerName, fieldName);
    }

    private BannerPlacement getBannerPlacement(String placementName) {
        if (TextUtils.isEmpty(placementName)) {
            return this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getDefaultBannerPlacement();
        }

        BannerPlacement placement = this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getBannerPlacement(placementName);
        if (placement != null) {
            return placement;
        }
        return this.mCurrentServerResponse.getConfigurations().getBannerConfigurations().getDefaultBannerPlacement();
    }

    public synchronized String getSessionId() {
        return this.mSessionId;
    }

    public void setConsent(boolean consent) {
        this.mConsent = Boolean.valueOf(consent);
        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.API, "setConsent : " + consent, 1);
        if (this.mRewardedVideoManager != null) {
            this.mRewardedVideoManager.setConsent(consent);
        }
        if (this.mInterstitialManager != null) {
            this.mInterstitialManager.setConsent(consent);
        }
        if (this.mBannerManager != null) {
            this.mBannerManager.setConsent(consent);
        }

        if (this.mOfferwallAdapter != null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, "Offerwall | setConsent(consent:" + consent + ")", 1);
            this.mOfferwallAdapter.setConsent(consent);
        }

        int code = 40;
        if (!consent) {
            code = 41;
        }

        JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
        EventData instanceEvent = new EventData(code, data);
        RewardedVideoEventsManager.getInstance().log(instanceEvent);
    }

    Boolean getConsent() {
        return this.mConsent;
    }

    public static abstract interface IResponseListener {
        public abstract void onUnrecoverableError(String paramString);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/IronSourceObject.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */