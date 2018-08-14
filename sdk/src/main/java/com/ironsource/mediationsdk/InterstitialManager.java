package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.ironsource.mediationsdk.sdk.InterstitialApi;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.ironsource.mediationsdk.sdk.InterstitialManagerListener;
import com.ironsource.mediationsdk.sdk.ListenersWrapper;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialApi;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialListener;
import com.ironsource.mediationsdk.sdk.RewardedInterstitialManagerListener;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.DailyCappingListener;
import com.ironsource.mediationsdk.utils.DailyCappingManager;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONObject;


class InterstitialManager
        extends AbstractAdUnitManager
        implements InterstitialApi, InterstitialManagerListener, MediationInitializer.OnMediationInitializationListener, RewardedInterstitialManagerListener, RewardedInterstitialApi, DailyCappingListener {
    private final String TAG = getClass().getName();

    private ListenersWrapper mInterstitialListenersWrapper;

    private RewardedInterstitialListener mRewardedInterstitialListenerWrapper;
    private ISDemandOnlyInterstitialListener mISDemandOnlyInterstitialListener;
    private boolean mDidCallLoadInterstitial;
    private boolean mIsLoadInterstitialInProgress;
    private boolean mDidFinishToInitInterstitial;
    private InterstitialPlacement mCurrentPlacement;
    private CallbackThrotteler mCallbackThrotteler;
    private boolean mShouldSendAdReadyEvent;
    private Map<String, InterstitialSmash> mInstanceIdToSmashMap;
    private CopyOnWriteArraySet<String> mInstancesToLoad;

    InterstitialManager() {
        this.mInstancesToLoad = new CopyOnWriteArraySet();
        this.mInstanceIdToSmashMap = new ConcurrentHashMap();
        this.mCallbackThrotteler = new CallbackThrotteler();
        this.mShouldSendAdReadyEvent = false;
        this.mIsLoadInterstitialInProgress = false;
        this.mDidCallLoadInterstitial = false;
        this.mDailyCappingManager = new DailyCappingManager("interstitial", this);
    }


    public void setInterstitialListener(InterstitialListener listener) {
        this.mInterstitialListenersWrapper = ((ListenersWrapper) listener);
        this.mCallbackThrotteler.setInterstitialListener(listener);
    }

    public void setRewardedInterstitialListener(RewardedInterstitialListener listener) {
        this.mRewardedInterstitialListenerWrapper = listener;
    }

    public synchronized void initInterstitial(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":initInterstitial(appKey: " + appKey + ", userId: " + userId + ")", 1);
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        Iterator localIterator;
        if (this.mIsInISDemandOnlyMode) {
            this.mSmartLoadAmount = this.mSmashArray.size();
            AbstractSmash smash;
            for (localIterator = this.mSmashArray.iterator(); localIterator.hasNext(); ) {
                smash = (AbstractSmash) localIterator.next();
                if (startAdapter((InterstitialSmash) smash) == null) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);
                }
                this.mInstanceIdToSmashMap.put(smash.getSubProviderId(), (InterstitialSmash) smash);
            }
        } else {
            this.mDailyCappingManager.setContext(this.mActivity);
            int dailyCappedCount = 0;
            for (AbstractSmash smash : this.mSmashArray) {
                if (this.mDailyCappingManager.shouldSendCapReleasedEvent(smash)) {
                    logProviderEvent(250, smash, new Object[][]{{"status", "false"}});
                }
                if (this.mDailyCappingManager.isCapped(smash)) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY);
                    dailyCappedCount++;
                }
            }

            if (dailyCappedCount == this.mSmashArray.size()) {
                this.mDidFinishToInitInterstitial = true;
            }

            for (int i = 0; i < this.mSmartLoadAmount; i++) {
                if (startNextAdapter() == null) {
                    break;
                }
            }
        }

    }

    public synchronized void loadInterstitial() {
        try {
            if ((this.mIsLoadInterstitialInProgress) || (this.mCallbackThrotteler.hasPendingInvocation())) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Load Interstitial is already in progress", 1);
                return;
            }

            MediationInitializer.EInitStatus initStatus = MediationInitializer.getInstance().getCurrentInitStatus();

            if (initStatus == MediationInitializer.EInitStatus.NOT_INIT) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() must be called before loadInterstitial()", 3);
                return;
            }

            if (initStatus == MediationInitializer.EInitStatus.INIT_IN_PROGRESS) {
                if (MediationInitializer.getInstance().isInProgressMoreThan15Secs()) {
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
                    this.mCallbackThrotteler.onInterstitialAdLoadFailed(ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
                } else {
                    logMediationEvent(22, (Object[][]) null);
                    this.mDidCallLoadInterstitial = true;
                    this.mShouldSendAdReadyEvent = true;
                }

                return;
            }

            if (initStatus == MediationInitializer.EInitStatus.INIT_FAILED) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
                return;
            }


            if (this.mSmashArray.size() == 0) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "the server response does not contain interstitial data", 3);
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(ErrorBuilder.buildInitFailedError("the server response does not contain interstitial data", "Interstitial"));
                return;
            }

            logMediationEvent(22, (Object[][]) null);
            this.mShouldSendAdReadyEvent = true;

            changeStateToInitiated();


            if (smashesCount(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.INITIATED}) == 0) {
                if (!this.mDidFinishToInitInterstitial) {
                    return;
                }

                IronSourceError error = ErrorBuilder.buildGenericError("no ads to load");
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, error.getErrorMessage(), 1);

                this.mCallbackThrotteler.onInterstitialAdLoadFailed(error);

                logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
                this.mShouldSendAdReadyEvent = false;
                return;
            }

            this.mDidCallLoadInterstitial = true;
            this.mIsLoadInterstitialInProgress = true;

            int loading = 0;
            for (AbstractSmash smash : this.mSmashArray) {
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.LOAD_PENDING);
                    loadAdapterAndSendEvent((InterstitialSmash) smash);

                    loading++;
                    if (loading >= this.mSmartLoadAmount)
                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            IronSourceError error = ErrorBuilder.buildLoadFailedError("loadInterstitial exception " + e.getMessage());
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, error.getErrorMessage(), 3);
            this.mCallbackThrotteler.onInterstitialAdLoadFailed(error);
            if (this.mShouldSendAdReadyEvent) {
                this.mShouldSendAdReadyEvent = false;
                logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
            }
        }
    }


    public void showInterstitial(String placementName) {
        if ((this.mShouldTrackNetworkState) && (this.mActivity != null) && (!IronSourceUtils.isNetworkConnected(this.mActivity))) {
            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildNoInternetConnectionShowFailError("Interstitial"));
            return;
        }


        if (!this.mDidCallLoadInterstitial) {
            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError("Interstitial", "showInterstitial failed - You need to load interstitial before showing it"));

            return;
        }

        for (int i = 0; i < this.mSmashArray.size(); i++) {
            AbstractSmash smash = (AbstractSmash) this.mSmashArray.get(i);
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                CappingManager.incrementShowCounter(this.mActivity, this.mCurrentPlacement);


                logProviderEvent(23, smash, new Object[][]{{"placement", placementName}});

                sendShowChanceEvents(smash, i, placementName);

                ((InterstitialSmash) smash).showInterstitial();

                this.mDailyCappingManager.increaseShowCounter(smash);
                if (this.mDailyCappingManager.isCapped(smash)) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY);
                    logProviderEvent(250, smash, new Object[][]{{"status", "true"}});
                }

                this.mDidCallLoadInterstitial = false;

                if (!smash.isMediationAvailable()) {
                    startNextAdapter();
                }
                return;
            }
        }


        this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(ErrorBuilder.buildShowFailedError("Interstitial", "showInterstitial failed - No adapters ready to show"));
    }


    public synchronized boolean isInterstitialReady() {
        if ((this.mShouldTrackNetworkState) && (this.mActivity != null) && (!IronSourceUtils.isNetworkConnected(this.mActivity))) {
            return false;
        }
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) && (((InterstitialSmash) smash).isInterstitialReady())) {
                return true;
            }
        }
        return false;
    }


    public void setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener listener) {
        this.mISDemandOnlyInterstitialListener = listener;
        this.mCallbackThrotteler.setISDemandOnlyInterstitialListener(listener);
    }

    public synchronized void loadInterstitial(String instanceId) {
        try {
            if (this.mCallbackThrotteler.hasPendingInvocation(instanceId)) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Load Interstitial for " + instanceId + " is already in progress", 1);
                return;
            }

            MediationInitializer.EInitStatus initStatus = MediationInitializer.getInstance().getCurrentInitStatus();

            if (initStatus == MediationInitializer.EInitStatus.NOT_INIT) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() must be called before loadInterstitial()", 3);
                return;
            }

            if (initStatus == MediationInitializer.EInitStatus.INIT_IN_PROGRESS) {
                if (MediationInitializer.getInstance().isInProgressMoreThan15Secs()) {
                    this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
                    this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
                } else {
                    this.mInstancesToLoad.add(instanceId);
                }

                return;
            }

            if (initStatus == MediationInitializer.EInitStatus.INIT_FAILED) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "init() had failed", 3);
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
                return;
            }

            if (!this.mInstanceIdToSmashMap.containsKey(instanceId)) {
                IronSourceError error = ErrorBuilder.buildNonExistentInstanceError("Interstitial");
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, error);
                logMediationEvent(22, (Object[][]) null);
                logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
                return;
            }

            InterstitialSmash smash = (InterstitialSmash) this.mInstanceIdToSmashMap.get(instanceId);
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INIT_PENDING) {
                this.mInstancesToLoad.add(instanceId);
            } else {
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.LOAD_PENDING);
                loadAdapterAndSendEvent(smash);
            }
        } catch (Exception e) {
            IronSourceError error = ErrorBuilder.buildLoadFailedError("loadInterstitial exception");
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, error.getErrorMessage(), 3);
            this.mCallbackThrotteler.onInterstitialAdLoadFailed(error);
        }
    }


    public void showInterstitial(String instanceId, String placementName) {
        if ((this.mShouldTrackNetworkState) && (this.mActivity != null) && (!IronSourceUtils.isNetworkConnected(this.mActivity))) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildNoInternetConnectionShowFailError("Interstitial"));
            return;
        }

        boolean existingInstanceId = false;

        for (int i = 0; i < this.mSmashArray.size(); i++) {
            AbstractSmash smash = (AbstractSmash) this.mSmashArray.get(i);
            if (smash.getSubProviderId().equals(instanceId)) {
                existingInstanceId = true;
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                    CappingManager.incrementShowCounter(this.mActivity, this.mCurrentPlacement);

                    logProviderEvent(23, smash, new Object[][]{{"placement", placementName}});

                    sendShowChanceEvents(smash, i, placementName);

                    ((InterstitialSmash) smash).showInterstitial();

                    changeStateToInitiatedForInstanceId(instanceId);

                    return;
                }
            }
        }

        if (!existingInstanceId) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildNonExistentInstanceError("no ads to show"));
        } else {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdShowFailed(instanceId, ErrorBuilder.buildShowFailedError("Interstitial", "no ads to show"));
        }
    }

    public synchronized boolean isInterstitialReady(String instanceId) {
        if ((this.mShouldTrackNetworkState) && (this.mActivity != null) && (!IronSourceUtils.isNetworkConnected(this.mActivity))) {
            return false;
        }
        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getSubProviderId().equals(instanceId)) {
                return (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) && (((InterstitialSmash) smash).isInterstitialReady());
            }
        }
        return false;
    }


    public synchronized void onInterstitialInitSuccess(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + " :onInterstitialInitSuccess()", 1);
        this.mDidFinishToInitInterstitial = true;

        if (this.mIsInISDemandOnlyMode) {
            String instanceId = smash.getSubProviderId();
            if (this.mInstancesToLoad.contains(instanceId)) {
                this.mInstancesToLoad.remove(instanceId);
                loadInterstitial(instanceId);
            }
        } else if (this.mDidCallLoadInterstitial) {
            if (smashesCount(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.AVAILABLE, AbstractSmash.MEDIATION_STATE.LOAD_PENDING}) < this.mSmartLoadAmount) {
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.LOAD_PENDING);
                loadAdapterAndSendEvent(smash);
            }
        }
    }

    public synchronized void onInterstitialInitFailed(IronSourceError error, InterstitialSmash smash) {
        try {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialInitFailed(" + error + ")", 1);

            if (this.mIsInISDemandOnlyMode) {
                String instanceId = smash.getSubProviderId();
                if (this.mInstancesToLoad.contains(instanceId)) {
                    this.mInstancesToLoad.remove(instanceId);
                    this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, ErrorBuilder.buildGenericError("no ads to show"));
                    logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(510)}});
                    logProviderEvent(227, smash, new Object[][]{{"errorCode", Integer.valueOf(510)}});
                }
            } else if (smashesCount(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.INIT_FAILED}) >= this.mSmashArray.size()) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, "Smart Loading - initialization failed - no adapters are initiated and no more left to init, error: " + error.getErrorMessage(), 2);
                if (this.mDidCallLoadInterstitial) {
                    this.mCallbackThrotteler.onInterstitialAdLoadFailed(ErrorBuilder.buildGenericError("no ads to show"));
                    logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(510)}});
                    this.mShouldSendAdReadyEvent = false;
                }


                this.mDidFinishToInitInterstitial = true;
            } else {
                startNextAdapter();
                completeIterationRound();
            }
        } catch (Exception e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onInterstitialInitFailed(error:" + error + ", " + "provider:" + smash.getName() + ")", e);
        }
    }

    public synchronized void onInterstitialAdReady(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdReady()", 1);

        logProviderEvent(27, smash, new Object[][]{{"status", "true"}});

        if (this.mIsInISDemandOnlyMode) {
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.AVAILABLE);
            this.mISDemandOnlyInterstitialListener.onInterstitialAdReady(smash.getSubProviderId());
            logMediationEvent(27, new Object[][]{{"status", "true"}});
        } else {
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.AVAILABLE);
            this.mIsLoadInterstitialInProgress = false;

            if (this.mShouldSendAdReadyEvent) {
                this.mShouldSendAdReadyEvent = false;
                this.mInterstitialListenersWrapper.onInterstitialAdReady();
                logMediationEvent(27, new Object[][]{{"status", "true"}});
            }
        }
    }

    public synchronized void onInterstitialAdLoadFailed(IronSourceError error, InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdLoadFailed(" + error + ")", 1);

        logProviderEvent(227, smash, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});


        if (this.mIsInISDemandOnlyMode) {
            this.mCallbackThrotteler.onInterstitialAdLoadFailed(smash.getSubProviderId(), error);
            logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
        } else {
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE);

            int availableOrLoadPending = smashesCount(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.AVAILABLE, AbstractSmash.MEDIATION_STATE.LOAD_PENDING});
            if (availableOrLoadPending >= this.mSmartLoadAmount) {
                return;
            }

            for (AbstractSmash asmash : this.mSmashArray) {
                if (asmash.getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED) {
                    asmash.setMediationState(AbstractSmash.MEDIATION_STATE.LOAD_PENDING);
                    loadAdapterAndSendEvent((InterstitialSmash) asmash);
                    return;
                }
            }

            if (startNextAdapter() != null) {
                return;
            }

            if ((this.mDidCallLoadInterstitial) && (availableOrLoadPending == 0)) {
                completeIterationRound();
                this.mIsLoadInterstitialInProgress = false;
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(error);
                logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
            }
        }
    }

    public void onInterstitialAdOpened(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdOpened()", 1);

        logProviderEvent(25, smash, (Object[][]) null);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdOpened(smash.getSubProviderId());
        } else {
            this.mInterstitialListenersWrapper.onInterstitialAdOpened();
        }
    }

    public void onInterstitialAdClosed(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdClosed()", 1);

        logProviderEvent(26, smash, (Object[][]) null);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdClosed(smash.getSubProviderId());
        } else {
            this.mInterstitialListenersWrapper.onInterstitialAdClosed();
        }
    }

    public void onInterstitialAdShowSucceeded(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdShowSucceeded()", 1);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdShowSucceeded(smash.getSubProviderId());
        } else {
            boolean hasAvailable = false;

            for (AbstractSmash asmash : this.mSmashArray) {
                if (asmash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                    hasAvailable = true;
                    completeAdapterShow(asmash);
                }
            }

            if ((!hasAvailable) && ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.EXHAUSTED) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY))) {
                completeIterationRound();
            }

            changeStateToInitiated();
            this.mInterstitialListenersWrapper.onInterstitialAdShowSucceeded();
        }
    }

    public void onInterstitialAdShowFailed(IronSourceError error, InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdShowFailed(" + error + ")", 1);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdShowFailed(smash.getSubProviderId(), error);
        } else {
            completeAdapterShow(smash);

            for (AbstractSmash asmash : this.mSmashArray) {
                if (asmash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                    this.mDidCallLoadInterstitial = true;
                    showInterstitial(this.mCurrentPlacement.getPlacementName());
                    return;
                }
            }

            this.mInterstitialListenersWrapper.onInterstitialAdShowFailed(error);
        }
    }

    public void onInterstitialAdClicked(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onInterstitialAdClicked()", 1);

        logProviderEvent(28, smash, (Object[][]) null);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyInterstitialListener.onInterstitialAdClicked(smash.getSubProviderId());
        } else {
            this.mInterstitialListenersWrapper.onInterstitialAdClicked();
        }
    }

    public void onInterstitialAdVisible(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash
                .getInstanceName() + ":onInterstitialAdVisible()", 1);


        logProviderEvent(31, smash, new Object[][]{{"placement", this.mCurrentPlacement.getPlacementName()}});
    }

    public void onInterstitialAdRewarded(InterstitialSmash smash) {
        logProviderEvent(290, smash, (Object[][]) null);

        if (this.mRewardedInterstitialListenerWrapper != null) {
            this.mRewardedInterstitialListenerWrapper.onInterstitialAdRewarded();
        }
    }

    void shouldTrackNetworkState(Context context, boolean track) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, this.TAG + " Should Track Network State: " + track, 0);
        this.mShouldTrackNetworkState = track;
    }


    public void onInitSuccess(List<IronSource.AD_UNIT> adUnits, boolean revived) {
    }


    public void onInitFailed(String reason) {
        if (this.mIsInISDemandOnlyMode) {
            for (String instanceId : this.mInstancesToLoad) {
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
            }
            this.mInstancesToLoad.clear();

        } else if (this.mDidCallLoadInterstitial) {
            this.mCallbackThrotteler.onInterstitialAdLoadFailed(ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
            this.mDidCallLoadInterstitial = false;
            this.mIsLoadInterstitialInProgress = false;
        }
    }


    public void onStillInProgressAfter15Secs() {
        if (this.mIsInISDemandOnlyMode) {
            for (String instanceId : this.mInstancesToLoad) {
                this.mCallbackThrotteler.onInterstitialAdLoadFailed(instanceId, ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial"));
            }
            this.mInstancesToLoad.clear();

        } else if (this.mDidCallLoadInterstitial) {
            IronSourceError error = ErrorBuilder.buildInitFailedError("init() had failed", "Interstitial");
            this.mCallbackThrotteler.onInterstitialAdLoadFailed(error);
            this.mDidCallLoadInterstitial = false;
            this.mIsLoadInterstitialInProgress = false;
            if (this.mShouldSendAdReadyEvent) {
                logMediationEvent(227, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
                this.mShouldSendAdReadyEvent = false;
            }
        }
    }


    private boolean isIterationRoundComplete() {
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_INITIATED) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INIT_PENDING) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.LOAD_PENDING) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE)) {
                return false;
            }
        }
        return true;
    }

    private void completeIterationRound() {
        if (isIterationRoundComplete()) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Reset Iteration", 0);

            for (AbstractSmash smash : this.mSmashArray) {
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.EXHAUSTED) {
                    smash.completeIteration();
                }
            }
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "End of Reset Iteration", 0);
        }
    }

    private void completeAdapterShow(AbstractSmash smash) {
        if (!smash.isMediationAvailable()) {
            startNextAdapter();
            completeIterationRound();
        } else {
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.INITIATED);
        }
    }


    private AbstractAdapter startNextAdapter() {
        AbstractAdapter initiatedAdapter = null;

        int activeAdapters = 0;

        for (int i = 0; (i < this.mSmashArray.size()) && (initiatedAdapter == null); i++) {
            if ((((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) || (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED) ||
                    (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.INIT_PENDING) || (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.LOAD_PENDING)) {
                activeAdapters++;

                if (activeAdapters >= this.mSmartLoadAmount)
                    break;
            } else if (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_INITIATED) {
                initiatedAdapter = startAdapter((InterstitialSmash) this.mSmashArray.get(i));

                if (initiatedAdapter == null) {
                    ((AbstractSmash) this.mSmashArray.get(i)).setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);
                }
            }
        }
        return initiatedAdapter;
    }


    private synchronized AbstractAdapter startAdapter(InterstitialSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":startAdapter(" + smash.getName() + ")", 1);
        AbstractAdapter providerAdapter;

        try {
            providerAdapter = getLoadedAdapterOrFetchByReflection(smash);

            if (providerAdapter == null) {
                return null;
            }
            IronSourceObject.getInstance().addToISAdaptersList(providerAdapter);


            providerAdapter.setLogListener(this.mLoggerManager);

            smash.setAdapterForSmash(providerAdapter);
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.INIT_PENDING);

            if (this.mRewardedInterstitialListenerWrapper != null) {
                smash.setRewardedInterstitialManagerListener(this);
            }
            setCustomParams(smash);

            smash.initInterstitial(this.mActivity, this.mAppKey, this.mUserId);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":startAdapter(" + smash.getName() + ")", e);

            smash.setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);

            IronSourceError error = ErrorBuilder.buildInitFailedError(smash.getName() + " initialization failed - please verify that required dependencies are in you build path.", "Interstitial");
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, error.toString(), 2);

            return null;
        }
        return providerAdapter;
    }

    void setCurrentPlacement(InterstitialPlacement currentPlacement) {
        this.mCurrentPlacement = currentPlacement;
    }

    private synchronized void loadAdapterAndSendEvent(InterstitialSmash smash) {
        if (this.mIsInISDemandOnlyMode) {
            logMediationEvent(22, (Object[][]) null);
        }
        logProviderEvent(22, smash, (Object[][]) null);
        smash.loadInterstitial();
    }

    private synchronized void changeStateToInitiatedForInstanceId(String instanceId) {
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.getSubProviderId().equals(instanceId)) && (
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) ||
                            (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.LOAD_PENDING) ||
                            (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE))) {
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.INITIATED);
                break;
            }
        }
    }

    private synchronized void changeStateToInitiated() {
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.LOAD_PENDING) || (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE)) {
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.INITIATED);
            }
        }
    }

    private void sendShowChanceEvent(AbstractSmash smash, String placementName, boolean status) {
        logProviderEvent(219, smash, new Object[][]{{"placement", placementName}, {"status", status ? "true" : "false"}});
    }

    private void sendShowChanceEvents(AbstractSmash selectedSmash, int priority, String placementName) {
        sendShowChanceEvent(selectedSmash, placementName, true);


        if (!this.mIsInISDemandOnlyMode) {
            for (int i = 0; (i < this.mSmashArray.size()) && (i < priority); i++) {
                AbstractSmash smash = (AbstractSmash) this.mSmashArray.get(i);
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE) {
                    sendShowChanceEvent(smash, placementName, false);
                }
            }
        }
    }

    private void logMediationEvent(int eventId, Object[][] keyVals) {
        JSONObject data = IronSourceUtils.getMediationAdditionalData(this.mIsInISDemandOnlyMode);
        try {
            if (keyVals != null) {
                for (Object[] pair : keyVals) {
                    data.put(pair[0].toString(), pair[1]);
                }
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "InterstitialManager logMediationEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        InterstitialEventsManager.getInstance().log(event);
    }

    private void logProviderEvent(int eventId, AbstractSmash smash, Object[][] keyVals) {
        JSONObject data = IronSourceUtils.getProviderAdditionalData(smash, this.mIsInISDemandOnlyMode);
        try {
            if (keyVals != null) {
                for (Object[] pair : keyVals) {
                    data.put(pair[0].toString(), pair[1]);
                }
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "InterstitialManager logProviderEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        InterstitialEventsManager.getInstance().log(event);
    }

    private int smashesCount(AbstractSmash.MEDIATION_STATE... states) {
        synchronized (this.mSmashArray) {
            int ret = 0;
            for (AbstractSmash smash : this.mSmashArray) {
                for (AbstractSmash.MEDIATION_STATE state : states) {
                    if (smash.getMediationState() == state) {
                        ret++;
                    }
                }
            }

            return ret;
        }
    }

    public void onDailyCapReleased() {
        if (this.mSmashArray != null) {
            for (AbstractSmash smash : this.mSmashArray) {
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY) {
                    logProviderEvent(250, smash, new Object[][]{{"status", "false"}});
                    if (smash.isCappedPerSession()) {
                        smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION);
                    } else if (smash.isExhausted()) {
                        smash.setMediationState(AbstractSmash.MEDIATION_STATE.EXHAUSTED);
                    } else {
                        smash.setMediationState(AbstractSmash.MEDIATION_STATE.INITIATED);
                    }
                }
            }
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/InterstitialManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */