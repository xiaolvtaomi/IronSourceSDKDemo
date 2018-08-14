package com.ironsource.mediationsdk.utils;


public class SessionDepthManager {
    private int mRewardedVideoDepth = 1;
    private int mInterstitialDepth = 1;
    private int mOfferwallDepth = 1;
    private int mBannerDepth = 1;

    public static final int NONE = -1;

    public static final int OFFERWALL = 0;
    public static final int REWARDEDVIDEO = 1;
    public static final int INTERSTITIAL = 2;
    public static final int BANNER = 3;
    private static SessionDepthManager mInstance;

    public static synchronized SessionDepthManager getInstance() {
        if (mInstance == null) {
            mInstance = new SessionDepthManager();
        }
        return mInstance;
    }

    public synchronized void increaseSessionDepth(int adUnit) {
        switch (adUnit) {
            case OFFERWALL:
                this.mOfferwallDepth += 1;
                break;

            case REWARDEDVIDEO:
                this.mRewardedVideoDepth += 1;
                break;

            case INTERSTITIAL:
                this.mInterstitialDepth += 1;
                break;

            case BANNER:
                this.mBannerDepth += 1;
                break;
        }

    }


    public synchronized int getSessionDepth(int adUnit) {
        switch (adUnit) {
            case OFFERWALL:
                return this.mOfferwallDepth;

            case REWARDEDVIDEO:
                return this.mRewardedVideoDepth;

            case INTERSTITIAL:
                return this.mInterstitialDepth;

            case BANNER:
                return this.mBannerDepth;
        }

        return NONE;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/SessionDepthManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */