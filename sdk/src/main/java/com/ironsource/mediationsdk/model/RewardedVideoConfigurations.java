package com.ironsource.mediationsdk.model;

import java.util.ArrayList;


public class RewardedVideoConfigurations {
    private ArrayList<Placement> mRVPlacements;
    private ApplicationEvents mRVEvents;
    private int mRVAdaptersSmartLoadAmount;
    private int mRVAdaptersTimeOutInSeconds;
    private String mBackFillProviderName;
    private String mPremiumProviderName;
    private static final int DEFAULT_RV_PLACEMENT_ID = 0;
    private Placement mDefaultRVPlacement;

    public RewardedVideoConfigurations() {
        this.mRVPlacements = new ArrayList();
        this.mRVEvents = new ApplicationEvents();
    }

    public RewardedVideoConfigurations(int adaptersSmartLoadAmount, int adaptersSmartLoadTimeout, ApplicationEvents events) {
        this.mRVPlacements = new ArrayList();
        this.mRVAdaptersSmartLoadAmount = adaptersSmartLoadAmount;
        this.mRVAdaptersTimeOutInSeconds = adaptersSmartLoadTimeout;
        this.mRVEvents = events;
    }

    public int getRewardedVideoAdaptersSmartLoadTimeout() {
        return this.mRVAdaptersTimeOutInSeconds;
    }

    public void addRewardedVideoPlacement(Placement placement) {
        if (placement != null) {
            this.mRVPlacements.add(placement);

            if (placement.getPlacementId() == 0)
                this.mDefaultRVPlacement = placement;
        }
    }

    public Placement getRewardedVideoPlacement(String placementName) {
        for (Placement placement : this.mRVPlacements) {
            if (placement.getPlacementName().equals(placementName)) {
                return placement;
            }
        }
        return null;
    }

    public Placement getDefaultRewardedVideoPlacement() {
        return this.mDefaultRVPlacement;
    }

    public int getRewardedVideoAdaptersSmartLoadAmount() {
        return this.mRVAdaptersSmartLoadAmount;
    }

    public ApplicationEvents getRewardedVideoEventsConfigurations() {
        return this.mRVEvents;
    }

    public String getBackFillProviderName() {
        return this.mBackFillProviderName;
    }

    public void setBackFillProviderName(String backFillProviderName) {
        this.mBackFillProviderName = backFillProviderName;
    }

    public String getPremiumProviderName() {
        return this.mPremiumProviderName;
    }

    public void setPremiumProviderName(String premiumProviderName) {
        this.mPremiumProviderName = premiumProviderName;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/RewardedVideoConfigurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */