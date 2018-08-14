package com.ironsource.mediationsdk;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.ProviderSettings;
import com.ironsource.mediationsdk.sdk.BannerManagerListener;
import com.ironsource.mediationsdk.utils.CappingManager;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.json.JSONObject;


public class BannerManager
        implements BannerManagerListener {
    private static final int ADAPTER_MIN_MAJOR_VERSION = 4;
    private static final int ADAPTER_MIN_MINOR_VERSION = 2;
    private BannerSmash mActiveSmash;
    private IronSourceBannerLayout mIronsourceBanner;
    private BannerPlacement mCurrentPlacement;
    private BANNER_STATE mState;
    private IronSourceLoggerManager mLoggerManager;
    private String mAppKey;
    private String mUserId;
    private Activity mActivity;
    private final CopyOnWriteArrayList<BannerSmash> mSmashArray = new CopyOnWriteArrayList();
    private long mReloadInterval;
    private Timer mReloadTimer;
    private Timer mIterationTimer;
    private Boolean mIsInForeground;

    private static enum BANNER_STATE {
        NOT_INITIATED,
        READY_TO_LOAD,
        FIRST_LOAD_IN_PROGRESS,
        LOAD_IN_PROGRESS,
        RELOAD_IN_PROGRESS;

        private BANNER_STATE() {
        }
    }

    public BannerManager() {
        this.mLoggerManager = IronSourceLoggerManager.getLogger();
        this.mState = BANNER_STATE.NOT_INITIATED;
        this.mIsInForeground = Boolean.valueOf(true);
    }

    public synchronized void setConsent(boolean consent) {
        synchronized (this.mSmashArray) {
            for (BannerSmash smash : this.mSmashArray) {
                smash.setConsent(consent);
            }
        }
    }

    private boolean isValidBannerVersion(String version) {
        try {
            String[] arr = version.split(Pattern.quote("."));
            if ((arr != null) && (arr.length < 2)) {
                return false;
            }

            int firstDigit = Integer.parseInt(arr[0]);
            if (firstDigit < 4) {
                return false;
            }

            int secondDigit = Integer.parseInt(arr[1]);
            if (secondDigit < 2) {
                return false;
            }

            return true;
        } catch (Exception e) {
        }
        return false;
    }

    public synchronized void initBannerManager(List<ProviderSettings> adaptersConfigs, Activity activity, String appKey, String userId, long timeout, int reloadInterval) {
        debugLog("initBannerManager(appKey: " + appKey + ", userId: " + userId + ")");
        this.mAppKey = appKey;
        this.mUserId = userId;
        this.mActivity = activity;
        this.mReloadInterval = reloadInterval;

        for (int i = 0; i < adaptersConfigs.size(); i++) {
            ProviderSettings config = (ProviderSettings) adaptersConfigs.get(i);
            AbstractAdapter adapter = loadAdapter(config);
            if ((adapter != null) && (isValidBannerVersion(adapter.getVersion()))) {
                BannerSmash smash = new BannerSmash(this, config, adapter, timeout, i + 1);
                this.mSmashArray.add(smash);
            } else {
                debugLog(config.getProviderInstanceName() + " can't load adapter or wrong version");
            }
        }

        this.mCurrentPlacement = null;
        setState(BANNER_STATE.READY_TO_LOAD);
    }

    public synchronized IronSourceBannerLayout createBanner(Activity activity, EBannerSize size) {
        return new IronSourceBannerLayout(activity, size, this);
    }

    public synchronized void loadBanner(IronSourceBannerLayout banner, BannerPlacement placement) {
        try {
            if ((this.mState != BANNER_STATE.READY_TO_LOAD) || (BannerCallbackThrottler.getInstance().hasPendingInvocation())) {
                this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "A banner is already loaded", 3);
                return;
            }

            setState(BANNER_STATE.FIRST_LOAD_IN_PROGRESS);
            this.mIronsourceBanner = banner;
            this.mCurrentPlacement = placement;
            sendMediationEvent(3001);

            if (CappingManager.isBnPlacementCapped(this.mActivity, placement.getPlacementName())) {
                BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(banner, new IronSourceError(604, "placement " + placement.getPlacementName() + " is capped"));
                sendMediationEvent(3111, new Object[][]{{"errorCode", Integer.valueOf(604)}});
                setState(BANNER_STATE.READY_TO_LOAD);
                return;
            }

            synchronized (this.mSmashArray) {
                for (BannerSmash smash : this.mSmashArray) {
                    smash.setReadyToLoad(true);
                }

                BannerSmash smash = (BannerSmash) this.mSmashArray.get(0);
                sendProviderEvent(3002, smash);
                smash.loadBanner(banner, this.mActivity, this.mAppKey, this.mUserId);
            }
        } catch (Exception e) {
            BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(banner, new IronSourceError(605, "loadBanner() failed " + e.getMessage()));
            String message = e.getMessage();
            message = message.substring(0, Math.min(message.length(), 100));
            sendMediationEvent(3111, new Object[][]{{"errorCode", Integer.valueOf(605)}, {"errorMessage", message}});
            setState(BANNER_STATE.READY_TO_LOAD);
        }
    }

    public synchronized void destroyBanner(IronSourceBannerLayout banner) {
        if (banner == null) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "destroyBanner banner cannot be null", 3);
            return;
        }

        if (banner.isDestroyed()) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.API, "Banner is already destroyed and can't be used anymore. Please create a new one using IronSource.createBanner API", 3);
            return;
        }

        sendMediationEvent(3100);
        stopReloadTimer();
        stopIterationTimer();
        banner.destroyBanner();
        this.mIronsourceBanner = null;
        this.mCurrentPlacement = null;

        if (this.mActiveSmash != null) {
            sendProviderEvent(3305, this.mActiveSmash);
            this.mActiveSmash.destroyBanner();
            this.mActiveSmash = null;
        }

        setState(BANNER_STATE.READY_TO_LOAD);
    }

    private void errorLog(String text) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "BannerManager " + text, 3);
    }

    private void debugLog(String text) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "BannerManager " + text, 0);
    }

    private void setState(BANNER_STATE state) {
        this.mState = state;
        debugLog("state=" + state.name());
    }

    private void callbackLog(String text, BannerSmash smash) {
        this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_CALLBACK, "BannerManager " + text + " " + smash.getName(), 0);
    }

    private void bindView(
            BannerSmash smash,
            View adView,
            FrameLayout.LayoutParams frameLayoutParams,
            RelativeLayout.LayoutParams relativeLayoutParams) {
        this.mActiveSmash = smash;
        if (frameLayoutParams != null) {
            this.mIronsourceBanner.addViewWithFrameLayoutParams(adView, frameLayoutParams);
        } else {
            this.mIronsourceBanner.addViewWithRelativeLayoutParams(adView, relativeLayoutParams);
        }
    }

    public void onBannerAdLoaded(BannerSmash smash, View adView, FrameLayout.LayoutParams frameLayoutParams, RelativeLayout.LayoutParams relativeLayoutParams) {
        callbackLog("onBannerAdLoaded", smash);

        if (this.mState == BANNER_STATE.FIRST_LOAD_IN_PROGRESS) {
            sendProviderEvent(3005, smash);
            bindView(smash, adView, frameLayoutParams, relativeLayoutParams);
            CappingManager.incrementBnShowCounter(this.mActivity, this.mCurrentPlacement.getPlacementName());
            if (CappingManager.isBnPlacementCapped(this.mActivity, this.mCurrentPlacement.getPlacementName())) {
                sendMediationEvent(3400);
            }

            this.mIronsourceBanner.sendBannerAdLoaded(smash);
            sendMediationEvent(3110);

            setState(BANNER_STATE.RELOAD_IN_PROGRESS);
            startReloadTimer();
        } else if (this.mState == BANNER_STATE.LOAD_IN_PROGRESS) {
            sendProviderEvent(3015, smash);
            bindView(smash, adView, frameLayoutParams, relativeLayoutParams);
            setState(BANNER_STATE.RELOAD_IN_PROGRESS);
            startReloadTimer();
        }
    }

    public void onBannerAdLoadFailed(IronSourceError error, BannerSmash smash) {
        callbackLog("onBannerAdLoadFailed " + error.getErrorMessage(), smash);
        if ((this.mState != BANNER_STATE.FIRST_LOAD_IN_PROGRESS) && (this.mState != BANNER_STATE.LOAD_IN_PROGRESS)) {
            debugLog("onBannerAdLoadFailed " + smash.getName() + " wrong state=" + this.mState.name());
            return;
        }

        sendProviderEvent(3300, smash, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});

        if (loadNextSmash()) {
            return;
        }

        if (this.mState == BANNER_STATE.FIRST_LOAD_IN_PROGRESS) {
            BannerCallbackThrottler.getInstance().sendBannerAdLoadFailed(this.mIronsourceBanner, new IronSourceError(606, "No ads to show"));
            sendMediationEvent(3111, new Object[][]{{"errorCode", Integer.valueOf(606)}});
            setState(BANNER_STATE.READY_TO_LOAD);
        } else {
            sendMediationEvent(3201);
            resetIteration();
            startIterationTimer();
        }
    }

    public void onBannerAdReloaded(BannerSmash smash) {
        callbackLog("onBannerAdReloaded", smash);

        if (this.mState != BANNER_STATE.RELOAD_IN_PROGRESS) {
            debugLog("onBannerAdReloaded " + smash.getName() + " wrong state=" + this.mState.name());
            return;
        }

        sendProviderEvent(3015, smash);
        startReloadTimer();
    }

    public void onBannerAdReloadFailed(IronSourceError error, BannerSmash smash) {
        callbackLog("onBannerAdReloadFailed " + error.getErrorMessage(), smash);
        if (this.mState != BANNER_STATE.RELOAD_IN_PROGRESS) {
            debugLog("onBannerAdReloadFailed " + smash.getName() + " wrong state=" + this.mState.name());
            return;
        }

        sendProviderEvent(3301, smash, new Object[][]{{"errorCode", Integer.valueOf(error.getErrorCode())}});
        setState(BANNER_STATE.LOAD_IN_PROGRESS);
        if (!loadNextSmash()) {
            sendMediationEvent(3201);
            resetIteration();
            startIterationTimer();
        }
    }

    public void onBannerAdClicked(BannerSmash smash) {
        callbackLog("onBannerAdClicked", smash);
        sendMediationEvent(3112);
        this.mIronsourceBanner.sendBannerAdClicked();
        sendProviderEvent(3008, smash);
    }

    public void onBannerAdScreenDismissed(BannerSmash smash) {
        callbackLog("onBannerAdScreenDismissed", smash);
        sendMediationEvent(3114);
        this.mIronsourceBanner.sendBannerAdScreenDismissed();
        sendProviderEvent(3303, smash);
    }

    public void onBannerAdScreenPresented(BannerSmash smash) {
        callbackLog("onBannerAdScreenPresented", smash);
        sendMediationEvent(3113);
        this.mIronsourceBanner.sendBannerAdScreenPresented();
        sendProviderEvent(3302, smash);
    }

    public void onBannerAdLeftApplication(BannerSmash smash) {
        callbackLog("onBannerAdLeftApplication", smash);
        sendMediationEvent(3115, (Object[][]) null);
        this.mIronsourceBanner.sendBannerAdLeftApplication();
        sendProviderEvent(3304, smash, (Object[][]) null);
    }

    private void sendMediationEvent(int eventId) {
        sendMediationEvent(eventId, (Object[][]) null);
    }

    private void sendMediationEvent(int eventId, Object[][] keyVals) {
        JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
        try {
            if (this.mIronsourceBanner != null) {
                data.put("bannerAdSize", this.mIronsourceBanner.getSize().getValue());
            }
            if (this.mCurrentPlacement != null) {
                data.put("placement", this.mCurrentPlacement.getPlacementName());
            }

            if (keyVals != null) {
                for (Object[] pair : keyVals) {
                    data.put(pair[0].toString(), pair[1]);
                }
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "sendMediationEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        InterstitialEventsManager.getInstance().log(event);
    }

    private void sendProviderEvent(int eventId, BannerSmash smash) {
        sendProviderEvent(eventId, smash, (Object[][]) null);
    }

    private void sendProviderEvent(int eventId, BannerSmash smash, Object[][] keyVals) {
        JSONObject data = IronSourceUtils.getProviderAdditionalData(smash);
        try {
            if (this.mIronsourceBanner != null) {
                data.put("bannerAdSize", this.mIronsourceBanner.getSize().getValue());
            }
            if (this.mCurrentPlacement != null) {
                data.put("placement", this.mCurrentPlacement.getPlacementName());
            }

            if (keyVals != null) {
                for (Object[] pair : keyVals) {
                    data.put(pair[0].toString(), pair[1]);
                }
            }
        } catch (Exception e) {
            this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "sendProviderEvent " + Log.getStackTraceString(e), 3);
        }

        EventData event = new EventData(eventId, data);
        InterstitialEventsManager.getInstance().log(event);
    }

    private void resetIteration() {
        synchronized (this.mSmashArray) {
            for (BannerSmash smash : this.mSmashArray) {
                smash.setReadyToLoad(true);
            }
        }
    }

    private boolean loadNextSmash() {
        synchronized (this.mSmashArray) {
            for (BannerSmash smash : this.mSmashArray) {
                if ((smash.isReadyToLoad()) && (this.mActiveSmash != smash)) {
                    sendProviderEvent(3002, smash);
                    smash.loadBanner(this.mIronsourceBanner, this.mActivity, this.mAppKey, this.mUserId);
                    return true;
                }
            }
            return false;
        }
    }

    public void onPause(Activity activity) {
        synchronized (this.mSmashArray) {
            this.mIsInForeground = Boolean.valueOf(false);
            for (BannerSmash smash : this.mSmashArray) {
                smash.onPause(activity);
            }
        }
    }

    public void onResume(Activity activity) {
        synchronized (this.mSmashArray) {
            this.mIsInForeground = Boolean.valueOf(true);
            for (BannerSmash smash : this.mSmashArray) {
                smash.onResume(activity);
            }
        }
    }

    private void startReloadTimer() {
        try {
            stopReloadTimer();

            this.mReloadTimer = new Timer();
            this.mReloadTimer.schedule(new TimerTask() {

                public void run() {
                    BannerManager.this.onReloadTimer();
                }
            }, this.mReloadInterval * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopReloadTimer() {
        if (this.mReloadTimer != null) {
            this.mReloadTimer.cancel();
            this.mReloadTimer = null;
        }
    }

    private void onReloadTimer() {
        if (this.mState != BANNER_STATE.RELOAD_IN_PROGRESS) {
            debugLog("onReloadTimer wrong state=" + this.mState.name());
            return;
        }

        if (this.mIsInForeground.booleanValue()) {
            sendMediationEvent(3011);
            sendProviderEvent(3012, this.mActiveSmash);
            this.mActiveSmash.reloadBanner();
        } else {
            sendMediationEvent(3200, new Object[][]{{"errorCode", Integer.valueOf(614)}});
            startReloadTimer();
        }
    }

    private void startIterationTimer() {
        try {
            stopIterationTimer();

            this.mIterationTimer = new Timer();
            this.mIterationTimer.schedule(new TimerTask() {

                public void run() {
                    BannerManager.this.loadNextSmash();
                }
            }, this.mReloadInterval * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopIterationTimer() {
        if (this.mIterationTimer != null) {
            this.mIterationTimer.cancel();
            this.mIterationTimer = null;
        }
    }

    private AbstractAdapter getLoadedAdapterOrFetchByReflection(String name, String reflectionName) {
        try {
            IronSourceObject sso = IronSourceObject.getInstance();
            AbstractAdapter providerAdapter = sso.getExistingAdapter(name);

            if (providerAdapter != null) {
                debugLog("using previously loaded " + name);
                return providerAdapter;
            }

            debugLog("loading " + name + " with reflection");
            Class<?> mAdapterClass = Class.forName("com.ironsource.adapters." + reflectionName.toLowerCase() + "." + reflectionName + "Adapter");
            Method startAdapterMethod = mAdapterClass.getMethod("startAdapter", new Class[]{String.class});
            return (AbstractAdapter) startAdapterMethod.invoke(mAdapterClass, new Object[]{name});
        } catch (Exception ex) {
            errorLog("getLoadedAdapterOrFetchByReflection " + ex.getMessage());
        }
        return null;
    }


    private AbstractAdapter loadAdapter(ProviderSettings config) {
        String name = config.getProviderInstanceName();
        String reflectionName = config.isMultipleInstances() ? config.getProviderTypeForReflection() : config.getProviderName();
        debugLog("loadAdapter(" + name + ")");
        AbstractAdapter providerAdapter;

        try {
            providerAdapter = getLoadedAdapterOrFetchByReflection(name, reflectionName);

            if (providerAdapter == null) {
                return null;
            }

            IronSourceObject.getInstance().addToBannerAdaptersList(providerAdapter);
            providerAdapter.setLogListener(this.mLoggerManager);
        } catch (Throwable e) {
            errorLog("loadAdapter(" + name + ") " + e.getMessage());
            return null;
        }
        return providerAdapter;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/BannerManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */