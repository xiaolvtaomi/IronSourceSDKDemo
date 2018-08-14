package com.ironsource.mediationsdk;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.ironsource.environment.DeviceStatus;
import com.ironsource.environment.NetworkStateReceiver;
import com.ironsource.environment.NetworkStateReceiver.NetworkStateReceiverListener;
import com.ironsource.mediationsdk.IronSource.AD_UNIT;
import com.ironsource.mediationsdk.IronSourceObject.IResponseListener;
import com.ironsource.mediationsdk.config.ConfigValidationResult;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.model.ServerSegmetData;
import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.mediationsdk.sdk.SegmentListener;
import com.ironsource.mediationsdk.utils.ErrorBuilder;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.ironsource.mediationsdk.utils.ServerResponseWrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

class MediationInitializer implements NetworkStateReceiverListener {
    private static MediationInitializer sInstance;
    private final String GENERAL_PROPERTIES_USER_ID = "userId";
    private final String GENERAL_PROPERTIES_APP_KEY = "appKey";
    private final String TAG = this.getClass().getSimpleName();
    private int mRetryDelay;
    private int mRetryCounter;
    private int mRetryLimit;
    private int mRetryGrowLimit;
    private int mRetryAvailabilityLimit;
    private boolean mIsRevived;
    private boolean mDidReportInitialAvailability = false;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler;
    private boolean mListenForInit = false;
    private AtomicBoolean mAtomicShouldPerformInit;
    private NetworkStateReceiver mNetworkStateReceiver;
    private CountDownTimer mCountDownTimer;
    private List<MediationInitializer.OnMediationInitializationListener> mOnMediationInitializationListeners = new ArrayList();
    private Activity mActivity;
    private String mUserId;
    private String mAppKey;
    private ServerResponseWrapper mServerResponseWrapper;
    private MediationInitializer.EInitStatus mInitStatus;
    private String mUserIdType;
    private SegmentListener mSegmentListener;
    private boolean mIsInProgressMoreThan15Secs;
    private MediationInitializer.InitRunnable initRunnable = new MediationInitializer.InitRunnable() {
        public void run() {
            try {
                IronSourceObject ironSourceObject = IronSourceObject.getInstance();
                ConfigValidationResult validationResult = MediationInitializer.this.validateUserId(MediationInitializer.this.mUserId);
                if (validationResult.isValid()) {
                    MediationInitializer.this.mUserIdType = "userGenerated";
                } else {
                    MediationInitializer.this.mUserId = ironSourceObject.getAdvertiserId(MediationInitializer.this.mActivity);
                    if (!TextUtils.isEmpty(MediationInitializer.this.mUserId)) {
                        MediationInitializer.this.mUserIdType = "GAID";
                    } else {
                        MediationInitializer.this.mUserId = DeviceStatus.getOrGenerateOnceUniqueIdentifier(MediationInitializer.this.mActivity);
                        if (!TextUtils.isEmpty(MediationInitializer.this.mUserId)) {
                            MediationInitializer.this.mUserIdType = "UUID";
                        } else {
                            MediationInitializer.this.mUserId = "";
                        }
                    }

                    ironSourceObject.setIronSourceUserId(MediationInitializer.this.mUserId);
                }

                GeneralProperties.getProperties().putKey("userIdType", MediationInitializer.this.mUserIdType);
                if (!TextUtils.isEmpty(MediationInitializer.this.mUserId)) {
                    GeneralProperties.getProperties().putKey("userId", MediationInitializer.this.mUserId);
                }

                if (!TextUtils.isEmpty(MediationInitializer.this.mAppKey)) {
                    GeneralProperties.getProperties().putKey("appKey", MediationInitializer.this.mAppKey);
                }

                MediationInitializer.this.mServerResponseWrapper = ironSourceObject.getServerResponse(MediationInitializer.this.mActivity, MediationInitializer.this.mUserId, this.listener);
                Iterator var7;
                MediationInitializer.OnMediationInitializationListener listener;
                if (MediationInitializer.this.mServerResponseWrapper != null) {
                    MediationInitializer.this.mHandler.removeCallbacks(this);
                    if (MediationInitializer.this.mServerResponseWrapper.isValidResponse()) {
                        MediationInitializer.this.setInitStatus(MediationInitializer.EInitStatus.INITIATED);
                        if (MediationInitializer.this.mServerResponseWrapper.getConfigurations().getApplicationConfigurations().getIntegration()) {
                            IntegrationHelper.validateIntegration(MediationInitializer.this.mActivity);
                        }

                        List<AD_UNIT> adUnits = MediationInitializer.this.mServerResponseWrapper.getInitiatedAdUnits();
                        Iterator var4 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                        while (var4.hasNext()) {
                            MediationInitializer.OnMediationInitializationListener listenerx = (MediationInitializer.OnMediationInitializationListener) var4.next();
                            listenerx.onInitSuccess(adUnits, MediationInitializer.this.wasInitRevived());
                        }

                        if (MediationInitializer.this.mSegmentListener != null) {
                            ServerSegmetData data = MediationInitializer.this.mServerResponseWrapper.getConfigurations().getApplicationConfigurations().getSegmetData();
                            if (data != null) {
                                MediationInitializer.this.mSegmentListener.onSegmentReceived(data.getSegmentName());
                            } else {
                                MediationInitializer.this.mSegmentListener.onSegmentReceived("");
                            }
                        }
                    } else if (!MediationInitializer.this.mDidReportInitialAvailability) {
                        MediationInitializer.this.setInitStatus(MediationInitializer.EInitStatus.INIT_FAILED);
                        MediationInitializer.this.mDidReportInitialAvailability = true;
                        var7 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                        while (var7.hasNext()) {
                            listener = (MediationInitializer.OnMediationInitializationListener) var7.next();
                            listener.onInitFailed("serverResponseIsNotValid");
                        }
                    }
                } else {
                    if (MediationInitializer.this.mRetryCounter == 3) {
                        MediationInitializer.this.mIsInProgressMoreThan15Secs = true;
                        var7 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                        while (var7.hasNext()) {
                            listener = (MediationInitializer.OnMediationInitializationListener) var7.next();
                            listener.onStillInProgressAfter15Secs();
                        }
                    }

                    if (this.isRecoverable && MediationInitializer.this.mRetryCounter < MediationInitializer.this.mRetryLimit) {
                        MediationInitializer.this.mIsRevived = true;
                        MediationInitializer.this.mHandler.postDelayed(this, (long) (MediationInitializer.this.mRetryDelay * 1000));
                        if (MediationInitializer.this.mRetryCounter < MediationInitializer.this.mRetryGrowLimit) {
                            MediationInitializer.this.mRetryDelay = MediationInitializer.this.mRetryDelay * 2;
                        }
                    }

                    if ((!this.isRecoverable || MediationInitializer.this.mRetryCounter == MediationInitializer.this.mRetryAvailabilityLimit) && !MediationInitializer.this.mDidReportInitialAvailability) {
                        MediationInitializer.this.mDidReportInitialAvailability = true;
                        if (TextUtils.isEmpty(this.reason)) {
                            this.reason = "noServerResponse";
                        }

                        var7 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                        while (var7.hasNext()) {
                            listener = (MediationInitializer.OnMediationInitializationListener) var7.next();
                            listener.onInitFailed(this.reason);
                        }

                        MediationInitializer.this.setInitStatus(MediationInitializer.EInitStatus.INIT_FAILED);
                        IronSourceLoggerManager.getLogger().log(IronSourceTag.API, "Mediation availability false reason: No server response", 1);
                    }

                    MediationInitializer.this.mRetryCounter++;
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }

        }
    };

    public static synchronized MediationInitializer getInstance() {
        if (sInstance == null) {
            sInstance = new MediationInitializer();
        }

        return sInstance;
    }

    private MediationInitializer() {
        this.mInitStatus = MediationInitializer.EInitStatus.NOT_INIT;
        this.mHandlerThread = new HandlerThread("IronSourceInitiatorHandler");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mRetryDelay = 1;
        this.mRetryCounter = 0;
        this.mRetryLimit = 62;
        this.mRetryGrowLimit = 12;
        this.mRetryAvailabilityLimit = 5;
        this.mAtomicShouldPerformInit = new AtomicBoolean(true);
        this.mIsRevived = false;
        this.mIsInProgressMoreThan15Secs = false;
    }

    private synchronized void setInitStatus(MediationInitializer.EInitStatus status) {
        IronSourceLoggerManager.getLogger().log(IronSourceTag.INTERNAL, "setInitStatus(old status: " + this.mInitStatus + ", new status: " + status + ")", 0);
        this.mInitStatus = status;
    }

    public synchronized void init(Activity activity, String appKey, String userId, AD_UNIT... adUnits) {
        try {
            if (this.mAtomicShouldPerformInit != null && this.mAtomicShouldPerformInit.compareAndSet(true, false)) {
                this.setInitStatus(MediationInitializer.EInitStatus.INIT_IN_PROGRESS);
                this.mActivity = activity;
                this.mUserId = userId;
                this.mAppKey = appKey;
                if (IronSourceUtils.isNetworkConnected(activity)) {
                    this.mHandler.post(this.initRunnable);
                } else {
                    this.mListenForInit = true;
                    if (this.mNetworkStateReceiver == null) {
                        this.mNetworkStateReceiver = new NetworkStateReceiver(activity, this);
                    }

                    activity.getApplicationContext().registerReceiver(this.mNetworkStateReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                    (new Handler(Looper.getMainLooper())).post(new Runnable() {
                        public void run() {
                            MediationInitializer.this.mCountDownTimer = (new CountDownTimer(60000L, 15000L) {
                                public void onTick(long millisUntilFinished) {
                                    if (millisUntilFinished <= 45000L) {
                                        MediationInitializer.this.mIsInProgressMoreThan15Secs = true;
                                        Iterator var3 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                                        while (var3.hasNext()) {
                                            MediationInitializer.OnMediationInitializationListener listener = (MediationInitializer.OnMediationInitializationListener) var3.next();
                                            listener.onStillInProgressAfter15Secs();
                                        }
                                    }

                                }

                                public void onFinish() {
                                    if (!MediationInitializer.this.mDidReportInitialAvailability) {
                                        MediationInitializer.this.mDidReportInitialAvailability = true;
                                        Iterator var1 = MediationInitializer.this.mOnMediationInitializationListeners.iterator();

                                        while (var1.hasNext()) {
                                            MediationInitializer.OnMediationInitializationListener listener = (MediationInitializer.OnMediationInitializationListener) var1.next();
                                            listener.onInitFailed("noInternetConnection");
                                        }

                                        IronSourceLoggerManager.getLogger().log(IronSourceTag.API, "Mediation availability false reason: No internet connection", 1);
                                    }

                                }
                            }).start();
                        }
                    });
                }
            } else {
                IronSourceLoggerManager.getLogger().log(IronSourceTag.API, this.TAG + ": Multiple calls to init are not allowed", 2);
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    public void onNetworkAvailabilityChanged(boolean connected) {
        if (this.mListenForInit && connected) {
            if (this.mCountDownTimer != null) {
                this.mCountDownTimer.cancel();
            }

            this.mListenForInit = false;
            this.mIsRevived = true;
            this.mHandler.post(this.initRunnable);
        }

    }

    private boolean wasInitRevived() {
        return this.mIsRevived;
    }

    public synchronized MediationInitializer.EInitStatus getCurrentInitStatus() {
        return this.mInitStatus;
    }

    public synchronized boolean isInProgressMoreThan15Secs() {
        return this.mIsInProgressMoreThan15Secs;
    }

    public void addMediationInitializationListener(MediationInitializer.OnMediationInitializationListener listener) {
        if (listener != null) {
            this.mOnMediationInitializationListeners.add(listener);
        }
    }

    public void removeMediationInitializationListener(MediationInitializer.OnMediationInitializationListener listener) {
        if (listener != null && this.mOnMediationInitializationListeners.size() != 0) {
            this.mOnMediationInitializationListeners.remove(listener);
        }
    }

    void setSegmentListener(SegmentListener listener) {
        this.mSegmentListener = listener;
    }

    private ConfigValidationResult validateUserId(String userId) {
        ConfigValidationResult result = new ConfigValidationResult();
        IronSourceError error;
        if (userId != null) {
            if (!this.validateLength(userId, 1, 64)) {
                error = ErrorBuilder.buildInvalidCredentialsError("userId", userId, (String) null);
                result.setInvalid(error);
            }
        } else {
            error = ErrorBuilder.buildInvalidCredentialsError("userId", userId, "it's missing");
            result.setInvalid(error);
        }

        return result;
    }

    private boolean validateLength(String key, int minLength, int maxLength) {
        return key == null ? false : key.length() >= minLength && key.length() <= maxLength;
    }

    abstract class InitRunnable implements Runnable {
        boolean isRecoverable = true;
        String reason;
        protected IResponseListener listener = new IResponseListener() {
            public void onUnrecoverableError(String errorMessage) {
                InitRunnable.this.isRecoverable = false;
                InitRunnable.this.reason = errorMessage;
            }
        };

        InitRunnable() {
        }
    }

    interface OnMediationInitializationListener {
        void onInitSuccess(List<AD_UNIT> var1, boolean var2);

        void onInitFailed(String var1);

        void onStillInProgressAfter15Secs();
    }

    static enum EInitStatus {
        NOT_INIT,
        INIT_IN_PROGRESS,
        INIT_FAILED,
        INITIATED;

        private EInitStatus() {
        }
    }
}