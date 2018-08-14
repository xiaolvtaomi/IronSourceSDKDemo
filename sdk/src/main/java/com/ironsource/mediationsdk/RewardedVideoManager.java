package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.ironsource.environment.NetworkStateReceiver;
import com.ironsource.environment.NetworkStateReceiver.NetworkStateReceiverListener;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyRewardedVideoListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoApi;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;
import com.ironsource.mediationsdk.sdk.RewardedVideoManagerListener;
import com.ironsource.mediationsdk.server.Server;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.DailyCappingListener;
import com.ironsource.mediationsdk.utils.DailyCappingManager;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.json.JSONObject;


class RewardedVideoManager
        extends AbstractAdUnitManager
        implements RewardedVideoApi, RewardedVideoManagerListener, NetworkStateReceiverListener, DailyCappingListener {
    private final String TAG = getClass().getSimpleName();

    private final int CAPPED_PER_SESSION_REASON = 2;
    private final int CAPPED_PER_DAY_REASON = 6;

    private RewardedVideoListener mListenersWrapper;

    private ISDemandOnlyRewardedVideoListener mISDemandOnlyListenersWrapper;

    private boolean mPauseSmartLoadDueToNetworkUnavailability;

    private boolean mIsUltraEventsEnabled;

    private NetworkStateReceiver mNetworkStateReceiver;
    private Placement mCurrentPlacement;
    private List<AbstractSmash.MEDIATION_STATE> mStatesToIgnore;

    RewardedVideoManager() {
        this.mPauseSmartLoadDueToNetworkUnavailability = false;
        this.mIsUltraEventsEnabled = false;

        this.mStatesToIgnore = Arrays.asList(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.INIT_FAILED, AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION, AbstractSmash.MEDIATION_STATE.EXHAUSTED, AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY});


        this.mDailyCappingManager = new DailyCappingManager("rewarded_video", this);
    }


    public void setRewardedVideoListener(RewardedVideoListener listener) {
        this.mListenersWrapper = listener;
    }

    public synchronized void initRewardedVideo(Activity activity, String appKey, String userId) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, this.TAG + ":initRewardedVideo(appKey: " + appKey + ", userId: " + userId + ")", 1);

        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        Iterator localIterator;
        if (this.mIsInISDemandOnlyMode) {
            this.mSmartLoadAmount = this.mSmashArray.size();

            for (localIterator = this.mSmashArray.iterator(); localIterator.hasNext(); ) {
                AbstractSmash smash = (AbstractSmash) localIterator.next();
                if (startAdapter((RewardedVideoSmash) smash) == null) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);
                }
            }
        } else {
            this.mDailyCappingManager.setContext(this.mActivity);

            int dailyCappedCount = 0;
            for (AbstractSmash smash : this.mSmashArray) {
                if (this.mDailyCappingManager.shouldSendCapReleasedEvent(smash)) {
                    logProviderEvent(150, smash, new Object[][]{{"status", "false"}});
                }
                if (this.mDailyCappingManager.isCapped(smash)) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY);
                    dailyCappedCount++;
                }
            }

            if (dailyCappedCount == this.mSmashArray.size()) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(false);
                return;
            }

            for (int i = 0; (i < this.mSmartLoadAmount) && (i < this.mSmashArray.size()); i++) {
                if (loadNextAdapter() == null) {
                    break;
                }
            }
        }
    }

    public synchronized void showRewardedVideo(String placementName) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, this.TAG + ":showRewardedVideo(placementName: " + placementName + ")", 1);


        if (!IronSourceUtils.isNetworkConnected(this.mActivity)) {
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildNoInternetConnectionShowFailError("Rewarded Video"));
            return;
        }

        sendShowCheckAvailabilityEvents(placementName);

        int capped = 0;
        int notAvailable = 0;

        for (int i = 0; i < this.mSmashArray.size(); i++) {
            AbstractSmash smash = (AbstractSmash) this.mSmashArray.get(i);
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                if (((RewardedVideoSmash) smash).isRewardedVideoAvailable()) {
                    showAdapter(smash, i);


                    if ((this.mCanShowPremium) && (!smash.equals(getPremiumSmash()))) {
                        disablePremiumForCurrentSession();
                    }

                    if (smash.isCappedPerSession()) {
                        smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION);
                        logProviderEvent(7, smash, new Object[][]{{"status", "false"}, {"reason", Integer.valueOf(2)}});
                        completeAdapterCap();
                    } else if (this.mDailyCappingManager.isCapped(smash)) {
                        smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY);
                        logProviderEvent(7, smash, new Object[][]{{"status", "false"}, {"reason", Integer.valueOf(6)}});
                        logProviderEvent(150, smash, new Object[][]{{"status", "true"}});
                        completeAdapterCap();
                    } else if (smash.isExhausted()) {
                        loadNextAdapter();
                        completeIterationRound();
                    }

                    return;
                }

                onRewardedVideoAvailabilityChanged(false, (RewardedVideoSmash) smash);

                Exception e = new Exception("FailedToShowVideoException");
                this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.INTERNAL, smash.getInstanceName() + " Failed to show video", e);
            } else if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION) || (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY)) {
                capped++;
            } else if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE) {
                notAvailable++;
            }
        }

        if (isBackFillAvailable()) {
            showAdapter(getBackfillSmash(), this.mSmashArray.size());
        } else if (capped + notAvailable == this.mSmashArray.size()) {
            this.mListenersWrapper.onRewardedVideoAdShowFailed(ErrorBuilder.buildNoAdsToShowError("Rewarded Video"));
        }
    }

    public synchronized boolean isRewardedVideoAvailable() {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, this.TAG + ":isRewardedVideoAvailable()", 1);

        if (this.mPauseSmartLoadDueToNetworkUnavailability) {
            return false;
        }
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.isMediationAvailable()) && (((RewardedVideoSmash) smash).isRewardedVideoAvailable())) {
                return true;
            }
        }

        return false;
    }


    public void setISDemandOnlyRewardedVideoListener(ISDemandOnlyRewardedVideoListener listener) {
        this.mISDemandOnlyListenersWrapper = listener;
    }

    public synchronized void showRewardedVideo(String instanceId, String placementName) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, this.TAG + ":showRewardedVideo(instanceId: " + instanceId + ", placementName: " + placementName + ")", 1);

        if (!IronSourceUtils.isNetworkConnected(this.mActivity)) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildNoInternetConnectionShowFailError("Rewarded Video"));
            return;
        }

        sendShowCheckAvailabilityEvents(placementName);

        boolean existingInstanceId = false;

        for (int i = 0; i < this.mSmashArray.size(); i++) {
            AbstractSmash smash = (AbstractSmash) this.mSmashArray.get(i);
            if (smash.getSubProviderId().equals(instanceId)) {
                existingInstanceId = true;
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                    if (((RewardedVideoSmash) smash).isRewardedVideoAvailable()) {
                        CappingManager.incrementShowCounter(this.mActivity, this.mCurrentPlacement);

                        logProviderEvent(2, smash, new Object[][]{{"placement", this.mCurrentPlacement.getPlacementName()}});

                        sendShowChanceEvents(smash, i, this.mCurrentPlacement.getPlacementName());

                        ((RewardedVideoSmash) smash).showRewardedVideo();

                        if (smash.isCappedPerSession()) {
                            logProviderEvent(7, smash, new Object[][]{{"status", "false"}, {"reason", Integer.valueOf(2)}});
                            onRewardedVideoAvailabilityChanged(false, (RewardedVideoSmash) smash);
                        } else if (this.mDailyCappingManager.isCapped(smash)) {
                            smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY);
                            logProviderEvent(150, smash, new Object[][]{{"status", "true"}});
                            onRewardedVideoAvailabilityChanged(false, (RewardedVideoSmash) smash);
                        }
                    } else {
                        onRewardedVideoAvailabilityChanged(false, (RewardedVideoSmash) smash);

                        Exception e = new Exception("FailedToShowVideoException");
                        this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.INTERNAL, smash.getInstanceName() + " Failed to show video", e);
                    }

                    return;
                }
                if (smash.getMediationState() != AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION)
                    break;
                this.mListenersWrapper.onRewardedVideoAdShowFailed(new IronSourceError(526, "Instance has reached its cap per session"));
                return;
            }
        }


        if (!existingInstanceId) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildNonExistentInstanceError("Rewarded Video"));
        } else
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdShowFailed(instanceId, ErrorBuilder.buildNoAdsToShowError("Rewarded Video"));
    }

    public synchronized boolean isRewardedVideoAvailable(String instanceId) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, this.TAG + ":isRewardedVideoAvailable(instanceId: " + instanceId + ")", 1);

        if (this.mPauseSmartLoadDueToNetworkUnavailability) {
            return false;
        }
        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getSubProviderId().equals(instanceId)) {
                return ((RewardedVideoSmash) smash).isRewardedVideoAvailable();
            }
        }

        return false;
    }


    public void onRewardedVideoAdShowFailed(IronSourceError error, RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdShowFailed(" + error + ")", 1);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdShowFailed(smash.getSubProviderId(), error);
        } else {
            this.mListenersWrapper.onRewardedVideoAdShowFailed(error);
        }
    }

    public void onRewardedVideoAdOpened(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdOpened()", 1);

        logProviderEvent(5, smash, (Object[][]) null);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdOpened(smash.getSubProviderId());
        } else {
            this.mListenersWrapper.onRewardedVideoAdOpened();
        }
    }

    public void onRewardedVideoAdClosed(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdClosed()", 1);

        logProviderEvent(6, smash, (Object[][]) null);

        notifyIsAdAvailableForStatistics();


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdClosed(smash.getSubProviderId());

        } else {
            this.mListenersWrapper.onRewardedVideoAdClosed();


            for (AbstractSmash asmash : this.mSmashArray) {
                if (asmash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE) {
                    try {
                        if (!asmash.getInstanceName().equals(smash.getInstanceName())) {
                            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, asmash.getInstanceName() + ":reload smash", 1);
                            ((RewardedVideoSmash) asmash).fetchRewardedVideo();
                        }
                    } catch (Throwable t) {
                        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, asmash.getInstanceName() + " Failed to call fetchVideo(), " + t.getLocalizedMessage(), 1);
                    }
                }
            }
        }
    }


    public synchronized void onRewardedVideoAvailabilityChanged(boolean available, RewardedVideoSmash smash) {
        if (!this.mPauseSmartLoadDueToNetworkUnavailability) {
            try {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAvailabilityChanged(available:" + available + ")", 1);

                logProviderEvent(7, smash, new Object[][]{{"status", String.valueOf(available)}});


                if (this.mIsInISDemandOnlyMode) {
                    this.mISDemandOnlyListenersWrapper.onRewardedVideoAvailabilityChanged(smash.getSubProviderId(), available);


                    if (shouldNotifyAvailabilityChanged(available)) {
                        logMediationEvent(7, new Object[][]{{"status", String.valueOf(available)}});
                    }
                } else {
                    if (smash.equals(getBackfillSmash())) {
                        if (shouldNotifyAvailabilityChanged(available)) {
                            this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
                        }
                        return;
                    }

                    if (smash.equals(getPremiumSmash())) {
                        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + " is a Premium adapter, canShowPremium: " + canShowPremium(), 1);

                        if (!canShowPremium()) {
                            smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION);

                            if (shouldNotifyAvailabilityChanged(false)) {
                                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
                            }
                            return;
                        }
                    }

                    if ((smash.isMediationAvailable()) && (!this.mDailyCappingManager.isCapped(smash))) {
                        if (available) {
                            if (shouldNotifyAvailabilityChanged(true))
                                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
                        } else {
                            if (shouldNotifyAvailabilityChanged(false)) {
                                notifyAvailabilityChange();
                            }
                            loadNextAdapter();
                            completeIterationRound();
                        }
                    }
                }
            } catch (Throwable e) {
                this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "onRewardedVideoAvailabilityChanged(available:" + available + ", " + "provider:" + smash.getName() + ")", e);
            }
        }
    }

    public void onRewardedVideoAdStarted(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdStarted()", 1);

        logProviderEvent(8, smash, (Object[][]) null);


        this.mListenersWrapper.onRewardedVideoAdStarted();
    }

    public void onRewardedVideoAdEnded(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdEnded()", 1);

        logProviderEvent(9, smash, (Object[][]) null);


        this.mListenersWrapper.onRewardedVideoAdEnded();
    }

    public void onRewardedVideoAdRewarded(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdRewarded()", 1);

        JSONObject data = IronSourceUtils.getProviderAdditionalData(smash, this.mIsInISDemandOnlyMode);
        try {
            data.put("placement", this.mCurrentPlacement.getPlacementName());
            data.put("rewardName", this.mCurrentPlacement.getRewardName());
            data.put("rewardAmount", this.mCurrentPlacement.getRewardAmount());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EventData event = new EventData(10, data);
        Map<String, String> rvServerParams;
        if (!TextUtils.isEmpty(this.mAppKey)) {
            String strToTransId = "" + Long.toString(event.getTimeStamp()) + this.mAppKey + smash.getName();
            String transId = IronSourceUtils.getTransId(strToTransId);

            event.addToAdditionalData("transId", transId);

            if (!TextUtils.isEmpty(IronSourceObject.getInstance().getDynamicUserId())) {
                event.addToAdditionalData("dynamicUserId", IronSourceObject.getInstance().getDynamicUserId());
            }
            rvServerParams = IronSourceObject.getInstance().getRvServerParams();
            if (rvServerParams != null) {
                for (String key : rvServerParams.keySet()) {
                    event.addToAdditionalData("custom_" + key, rvServerParams.get(key));
                }
            }
        }

        RewardedVideoEventsManager.getInstance().log(event);


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdRewarded(smash.getSubProviderId(), this.mCurrentPlacement);
        } else {
            this.mListenersWrapper.onRewardedVideoAdRewarded(this.mCurrentPlacement);
        }
    }

    public void onRewardedVideoAdClicked(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdClicked()", 1);

        logProviderEvent(128, smash, new Object[][]{{"placement", this.mCurrentPlacement.getPlacementName()}});


        if (this.mIsInISDemandOnlyMode) {
            this.mISDemandOnlyListenersWrapper.onRewardedVideoAdClicked(smash.getSubProviderId(), this.mCurrentPlacement);
        } else {
            this.mListenersWrapper.onRewardedVideoAdClicked(this.mCurrentPlacement);
        }
    }

    public void onRewardedVideoAdVisible(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, smash.getInstanceName() + ":onRewardedVideoAdVisible()", 1);
        logProviderEvent(11, smash, new Object[][]{{"placement", this.mCurrentPlacement.getPlacementName()}});
    }


    public void onNetworkAvailabilityChanged(boolean connected) {
        if (this.mShouldTrackNetworkState) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Network Availability Changed To: " + connected, 0);

            if (shouldNotifyNetworkAvailabilityChanged(connected)) {
                this.mPauseSmartLoadDueToNetworkUnavailability = (!connected);
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(connected);
            }
        }
    }

    void shouldTrackNetworkState(Context context, boolean track) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, this.TAG + " Should Track Network State: " + track, 0);

        this.mShouldTrackNetworkState = track;

        if (this.mShouldTrackNetworkState) {
            if (this.mNetworkStateReceiver == null)
                this.mNetworkStateReceiver = new NetworkStateReceiver(context, this);
            context.registerReceiver(this.mNetworkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } else if (this.mNetworkStateReceiver != null) {
            context.unregisterReceiver(this.mNetworkStateReceiver);
        }
    }

    private boolean shouldNotifyNetworkAvailabilityChanged(boolean networkState) {
        boolean shouldNotify = false;

        if (this.mLastMediationAvailabilityState == null) {
            return false;
        }
        if ((networkState) && (!this.mLastMediationAvailabilityState.booleanValue()) && (hasAvailableSmash())) {
            this.mLastMediationAvailabilityState = Boolean.valueOf(true);
            shouldNotify = true;
        } else if ((!networkState) && (this.mLastMediationAvailabilityState.booleanValue())) {
            this.mLastMediationAvailabilityState = Boolean.valueOf(false);
            shouldNotify = true;
        }

        return shouldNotify;
    }


    void setIsUltraEventsEnabled(boolean enabled) {
        this.mIsUltraEventsEnabled = enabled;
    }


    private void reportFalseImpressionsOnHigherPriority(int priority, int placementId) {
        for (int i = 0; (i < priority) && (i < this.mSmashArray.size()); i++) {
            if (!this.mStatesToIgnore.contains(((AbstractSmash) this.mSmashArray.get(i)).getMediationState())) {
                reportImpression(((RewardedVideoSmash) this.mSmashArray.get(i)).getRequestUrl(), false, placementId);
            }
        }
    }


    private synchronized void reportImpression(String adapterUrl, boolean hit, int placementId) {
        String url = "";
        try {
            url = url + adapterUrl;


            url = url + "&sdkVersion=" + IronSourceUtils.getSDKVersion();

            Server.callAsyncRequestURL(url, hit, placementId);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.NETWORK, "reportImpression:(providerURL:" + url + ", " + "hit:" + hit + ")", e);
        }
    }


    void setCurrentPlacement(Placement currentPlacement) {
        this.mCurrentPlacement = currentPlacement;
    }

    protected synchronized void disablePremiumForCurrentSession() {
        super.disablePremiumForCurrentSession();

        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.equals(getPremiumSmash())) {
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION);

                loadNextAdapter();

                break;
            }
        }
    }


    private synchronized AbstractAdapter startAdapter(RewardedVideoSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":startAdapter(" + smash.getInstanceName() + ")", 1);
        AbstractAdapter providerAdapter;

        try {
            providerAdapter = getLoadedAdapterOrFetchByReflection(smash);

            if (providerAdapter == null) {
                return null;
            }
            IronSourceObject.getInstance().addToRVAdaptersList(providerAdapter);


            providerAdapter.setLogListener(this.mLoggerManager);

            smash.setAdapterForSmash(providerAdapter);
            smash.setMediationState(AbstractSmash.MEDIATION_STATE.INITIATED);

            setCustomParams(smash);

            smash.initRewardedVideo(this.mActivity, this.mAppKey, this.mUserId);
        } catch (Throwable e) {
            this.mLoggerManager.logException(IronSourceLogger.IronSourceTag.API, this.TAG + ":startAdapter(" + smash.getName() + ")", e);

            smash.setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);

            if (shouldNotifyAvailabilityChanged(false)) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
            }
            IronSourceError error = ErrorBuilder.buildInitFailedError(smash.getName() + " initialization failed - please verify that required dependencies are in you build path.", "Rewarded Video");
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, error.toString(), 2);

            return null;
        }
        return providerAdapter;
    }

    private AbstractAdapter loadNextAdapter() {
        AbstractAdapter initiatedAdapter = null;

        int activeAdapters = 0;

        for (int i = 0; (i < this.mSmashArray.size()) && (initiatedAdapter == null); i++) {
            if ((((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) || (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED)) {
                activeAdapters++;

                if (activeAdapters >= this.mSmartLoadAmount)
                    break;
            } else if (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_INITIATED) {
                initiatedAdapter = startAdapter((RewardedVideoSmash) this.mSmashArray.get(i));

                if (initiatedAdapter == null) {
                    ((AbstractSmash) this.mSmashArray.get(i)).setMediationState(AbstractSmash.MEDIATION_STATE.INIT_FAILED);
                }
            }
        }
        return initiatedAdapter;
    }

    private synchronized void showAdapter(AbstractSmash smash, int index) {
        CappingManager.incrementShowCounter(this.mActivity, this.mCurrentPlacement);
        this.mDailyCappingManager.increaseShowCounter(smash);


        if (this.mIsUltraEventsEnabled) {
            reportImpression(((RewardedVideoSmash) smash).getRequestUrl(), true, this.mCurrentPlacement.getPlacementId());

            reportFalseImpressionsOnHigherPriority(index, this.mCurrentPlacement.getPlacementId());
        }

        logProviderEvent(2, smash, new Object[][]{{"placement", this.mCurrentPlacement.getPlacementName()}});

        sendShowChanceEvents(smash, index, this.mCurrentPlacement.getPlacementName());

        ((RewardedVideoSmash) smash).showRewardedVideo();
    }


    private synchronized void notifyIsAdAvailableForStatistics() {
        boolean mediationStatus = false;

        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                mediationStatus = true;
                break;
            }
        }

        logMediationEvent(3, new Object[][]{{"status", String.valueOf(mediationStatus)}});


        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                logProviderEvent(3, smash, new Object[][]{{"status", "true"}});
            } else if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE) || (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED)) {
                logProviderEvent(3, smash, new Object[][]{{"status", "false"}});
            }
        }

        if ((getBackfillSmash() != null) && (getBackfillSmash().getAdapter() != null)) {
            logProviderEvent(3, getBackfillSmash(), new Object[][]{{"status", isBackFillAvailable() ? "true" : "false"}});
        }
    }

    private synchronized boolean shouldNotifyAvailabilityChanged(boolean adapterAvailability) {
        boolean shouldNotify = false;


        if (this.mLastMediationAvailabilityState == null) {
            if (adapterAvailability) {
                this.mLastMediationAvailabilityState = Boolean.valueOf(true);
                shouldNotify = true;

            } else if ((!isBackFillAvailable()) && (isAllLoaded())) {
                this.mLastMediationAvailabilityState = Boolean.valueOf(false);
                shouldNotify = true;
            }


        } else if ((adapterAvailability) && (!this.mLastMediationAvailabilityState.booleanValue())) {
            this.mLastMediationAvailabilityState = Boolean.valueOf(true);
            shouldNotify = true;


        } else if ((!adapterAvailability) && (this.mLastMediationAvailabilityState.booleanValue()) && (!hasAvailableSmash()) && (!isBackFillAvailable())) {
            this.mLastMediationAvailabilityState = Boolean.valueOf(false);
            shouldNotify = true;
        }

        return shouldNotify;
    }

    private synchronized boolean isAllLoaded() {
        boolean allLoaded = true;

        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_INITIATED) {
                allLoaded = false;
                break;
            }
        }

        return allLoaded;
    }

    private synchronized boolean hasAvailableSmash() {
        boolean hasAvailableSmash = false;

        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                hasAvailableSmash = true;
                break;
            }
        }

        return hasAvailableSmash;
    }

    private synchronized boolean isBackFillAvailable() {
        if (getBackfillSmash() != null) {
            return ((RewardedVideoSmash) getBackfillSmash()).isRewardedVideoAvailable();
        }
        return false;
    }

    private void sendShowCheckAvailabilityEvents(String placementName) {
        for (int i = 0; i < this.mSmashArray.size(); i++) {
            if (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                createAndSendShowCheckAvailabilityEvent((AbstractSmash) this.mSmashArray.get(i), placementName, true);
            } else if (((AbstractSmash) this.mSmashArray.get(i)).getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE) {
                createAndSendShowCheckAvailabilityEvent((AbstractSmash) this.mSmashArray.get(i), placementName, false);
            }
        }

        if ((getBackfillSmash() != null) && (getBackfillSmash().getAdapter() != null))
            createAndSendShowCheckAvailabilityEvent(getBackfillSmash(), placementName, isBackFillAvailable());
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

    private void createAndSendShowCheckAvailabilityEvent(AbstractSmash smash, String placementName, boolean status) {
        logProviderEvent(19, smash, new Object[][]{{"placement", placementName}, {"status", status ? "true" : "false"}});
    }

    private void sendShowChanceEvent(AbstractSmash smash, String placementName, boolean status) {
        logProviderEvent(119, smash, new Object[][]{{"placement", placementName}, {"status", status ? "true" : "false"}});
    }

    private synchronized void notifyAvailabilityChange() {
        if ((getBackfillSmash() != null) && (!this.mBackFillInitStarted)) {
            this.mBackFillInitStarted = true;
            AbstractAdapter backfillAdapter = startAdapter((RewardedVideoSmash) getBackfillSmash());


            if (backfillAdapter == null) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
            }
        } else if (isBackFillAvailable()) {
            if (shouldNotifyAvailabilityChanged(true)) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
            }
        } else {
            this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
        }
    }


    private synchronized void completeAdapterCap() {
        if (loadNextAdapter() != null) {
            return;
        }

        int cappedOrNotAvailable = smashesCount(new AbstractSmash.MEDIATION_STATE[]{AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE, AbstractSmash.MEDIATION_STATE.CAPPED_PER_SESSION, AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY});

        if (cappedOrNotAvailable < this.mSmashArray.size()) {
            completeIterationRound();
            return;
        }
        if (shouldNotifyAvailabilityChanged(false)) {
            notifyAvailabilityChange();
        }
    }

    private synchronized void completeIterationRound() {
        if (isIterationRoundComplete()) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Reset Iteration", 0);

            boolean isAvailable = false;

            for (AbstractSmash smash : this.mSmashArray) {
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.EXHAUSTED) {
                    smash.completeIteration();
                }
                if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE) {
                    isAvailable = true;
                }
            }
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "End of Reset Iteration", 0);

            if (shouldNotifyAvailabilityChanged(isAvailable)) {
                this.mListenersWrapper.onRewardedVideoAvailabilityChanged(this.mLastMediationAvailabilityState.booleanValue());
            }
        }
    }

    private synchronized boolean isIterationRoundComplete() {
        for (AbstractSmash smash : this.mSmashArray) {
            if ((smash.getMediationState() == AbstractSmash.MEDIATION_STATE.NOT_INITIATED) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.INITIATED) ||
                    (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.AVAILABLE)) {
                return false;
            }
        }
        return true;
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
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "RewardedVideoManager logMediationEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        RewardedVideoEventsManager.getInstance().log(event);
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
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "RewardedVideoManager logProviderEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        RewardedVideoEventsManager.getInstance().log(event);
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
        boolean atLeastOneSmashBecameAvailable = false;
        for (AbstractSmash smash : this.mSmashArray) {
            if (smash.getMediationState() == AbstractSmash.MEDIATION_STATE.CAPPED_PER_DAY) {
                logProviderEvent(150, smash, new Object[][]{{"status", "false"}});
                smash.setMediationState(AbstractSmash.MEDIATION_STATE.NOT_AVAILABLE);
                if ((((RewardedVideoSmash) smash).isRewardedVideoAvailable()) && (smash.isMediationAvailable())) {
                    smash.setMediationState(AbstractSmash.MEDIATION_STATE.AVAILABLE);
                    atLeastOneSmashBecameAvailable = true;
                }
            }
        }

        if ((atLeastOneSmashBecameAvailable) &&
                (shouldNotifyAvailabilityChanged(true))) {
            this.mListenersWrapper.onRewardedVideoAvailabilityChanged(true);
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/RewardedVideoManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */