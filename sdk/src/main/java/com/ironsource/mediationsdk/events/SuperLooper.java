package com.ironsource.mediationsdk.events;

import android.os.Handler;
import android.os.HandlerThread;

import com.ironsource.mediationsdk.logger.ThreadExceptionHandler;


public class SuperLooper
        extends Thread {
    private SupersonicSdkThread mSdkThread;
    private static SuperLooper mInstance;

    private SuperLooper() {
        this.mSdkThread = new SupersonicSdkThread(getClass().getSimpleName());
        this.mSdkThread.start();
        this.mSdkThread.prepareHandler();
    }

    public static synchronized SuperLooper getLooper() {
        if (mInstance == null) {
            mInstance = new SuperLooper();
        }

        return mInstance;
    }

    public synchronized void post(Runnable runnable) {
        if (this.mSdkThread == null) {
            return;
        }

        Handler callbackHandler = this.mSdkThread.getCallbackHandler();

        if (callbackHandler != null) {
            callbackHandler.post(runnable);
        }
    }

    private class SupersonicSdkThread extends HandlerThread {
        private Handler mHandler;

        SupersonicSdkThread(String name) {
            super(name);


            setUncaughtExceptionHandler(new ThreadExceptionHandler());
        }

        void prepareHandler() {
            this.mHandler = new Handler(getLooper());
        }

        Handler getCallbackHandler() {
            return this.mHandler;
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/events/SuperLooper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */