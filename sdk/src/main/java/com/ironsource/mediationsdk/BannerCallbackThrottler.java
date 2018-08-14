package com.ironsource.mediationsdk;

import android.os.Handler;
import android.os.Looper;

import com.ironsource.mediationsdk.logger.IronSourceError;


public class BannerCallbackThrottler {
    private static final long MAX_CALL_RATE_SEC = 15L;
    private static BannerCallbackThrottler sInstance;
    private long mLastInvoked;
    private boolean mIsWaitingForInvocation;

    public static synchronized BannerCallbackThrottler getInstance() {
        if (sInstance == null) {
            sInstance = new BannerCallbackThrottler();
        }

        return sInstance;
    }


    private BannerCallbackThrottler() {
        this.mLastInvoked = 0L;
        this.mIsWaitingForInvocation = false;
    }


    public void sendBannerAdLoadFailed(final IronSourceBannerLayout banner, final IronSourceError error) {
        synchronized (this) {
            if (this.mIsWaitingForInvocation) {
                return;
            }

            long timeSinceLastCallMs = System.currentTimeMillis() - this.mLastInvoked;
            if (timeSinceLastCallMs > MAX_CALL_RATE_SEC * 1000) {
                invokeCallback(banner, error);
                return;
            }

            this.mIsWaitingForInvocation = true;
            long nextCallMs = MAX_CALL_RATE_SEC * 1000 - timeSinceLastCallMs;

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    BannerCallbackThrottler.this.invokeCallback(banner, error);
                    BannerCallbackThrottler.this.mIsWaitingForInvocation = false;
                }
            }, nextCallMs);
        }
    }

    public boolean hasPendingInvocation() {
        return false ;
    }

    private void invokeCallback(IronSourceBannerLayout banner, IronSourceError error) {
        this.mLastInvoked = System.currentTimeMillis();
        banner.sendBannerAdLoadFailed(error);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/BannerCallbackThrottler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */