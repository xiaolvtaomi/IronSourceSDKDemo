package com.ironsource.mediationsdk;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.ISDemandOnlyInterstitialListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

import java.util.HashMap;
import java.util.Map;


public class CallbackThrotteler {
    private static final long MAX_CALL_RATE_SEC = 15L;
    private static final String MEDIATION = "mediation";
    private Map<String, Long> mLastInvoked;
    private Map<String, Boolean> mIsWaitingForInvocation;
    private InterstitialListener mListener;
    private ISDemandOnlyInterstitialListener mDemandOnlyListener;

    public CallbackThrotteler() {
        this.mListener = null;
        this.mDemandOnlyListener = null;
        this.mLastInvoked = new HashMap();
        this.mIsWaitingForInvocation = new HashMap();
    }


    public void setInterstitialListener(InterstitialListener listener) {
        this.mListener = listener;
    }


    public void setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener listener) {
        this.mDemandOnlyListener = listener;
    }


    public void onInterstitialAdLoadFailed(IronSourceError error) {
        synchronized (this) {
            onInterstitialAdLoadFailedInternal(MEDIATION, error);
        }
    }


    public void onInterstitialAdLoadFailed(String instanceId, IronSourceError error) {
        synchronized (this) {
            onInterstitialAdLoadFailedInternal(instanceId, error);
        }
    }

    public boolean hasPendingInvocation(String instanceId) {
        synchronized(this) {
            return this.hasPendingInvocationInternal(instanceId);
        }
    }

    public boolean hasPendingInvocation() {
        synchronized(this) {
            return this.hasPendingInvocationInternal(MEDIATION);
        }
    }

    private boolean hasPendingInvocationInternal(String instanceId) {
        if (TextUtils.isEmpty(instanceId)) {
            return false;
        }

        if (!this.mIsWaitingForInvocation.containsKey(instanceId)) {
            return false;
        }

        return ((Boolean) this.mIsWaitingForInvocation.get(instanceId)).booleanValue();
    }


    private void invokeCallback(String instanceId, IronSourceError error) {
        this.mLastInvoked.put(instanceId, Long.valueOf(System.currentTimeMillis()));
        if (instanceId.equalsIgnoreCase(MEDIATION)) {
            if (this.mListener != null) {
                this.mListener.onInterstitialAdLoadFailed(error);
            }
        } else if (this.mDemandOnlyListener != null) {
            this.mDemandOnlyListener.onInterstitialAdLoadFailed(instanceId, error);
        }
    }


    private void onInterstitialAdLoadFailedInternal(final String instanceId, final IronSourceError error) {
        if (hasPendingInvocationInternal(instanceId)) {
            return;
        }

        if (!this.mLastInvoked.containsKey(instanceId)) {
            invokeCallback(instanceId, error);
            return;
        }

        long timeSinceLastCallMs = System.currentTimeMillis() - ((Long) this.mLastInvoked.get(instanceId)).longValue();
        if (timeSinceLastCallMs > MAX_CALL_RATE_SEC*1000) {
            invokeCallback(instanceId, error);
            return;
        }

        this.mIsWaitingForInvocation.put(instanceId, Boolean.valueOf(true));
        long nextCallMs = MAX_CALL_RATE_SEC*1000 - timeSinceLastCallMs;

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                CallbackThrotteler.this.invokeCallback(instanceId, error);
                CallbackThrotteler.this.mIsWaitingForInvocation.put(instanceId, Boolean.valueOf(false));
            }
        }, nextCallMs);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/CallbackThrotteler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */