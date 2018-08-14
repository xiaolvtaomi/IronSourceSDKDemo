 package com.ironsource.mediationsdk.model;

 import java.util.ArrayList;




 public class BannerConfigurations
 {
   private ApplicationEvents mBNEvents;
   private int mBNAdaptersSmartLoadAmount;
   private long mBNAdaptersTimeOutInMilliseconds;
   private ArrayList<BannerPlacement> mBNPlacements;
   private static final int DEFAULT_BN_PLACEMENT_ID = 0;
   private BannerPlacement mDefaultBNPlacement;
   private int mBNRefreshInterval;

   public BannerConfigurations()
   {
     this.mBNEvents = new ApplicationEvents();
     this.mBNPlacements = new ArrayList();
   }

   public BannerConfigurations(int adaptersSmartLoadAmount, long adaptersSmartLoadTimeoutInMillis, ApplicationEvents events, int refreshInterval)
   {
     this.mBNPlacements = new ArrayList();
     this.mBNAdaptersSmartLoadAmount = adaptersSmartLoadAmount;
     this.mBNAdaptersTimeOutInMilliseconds = adaptersSmartLoadTimeoutInMillis;
     this.mBNEvents = events;
     this.mBNRefreshInterval = refreshInterval;
   }

   public int getBannerAdaptersSmartLoadAmount() {
     return this.mBNAdaptersSmartLoadAmount;
   }

   public long getBannerAdaptersSmartLoadTimeout() {
     return this.mBNAdaptersTimeOutInMilliseconds;
   }

   public ApplicationEvents getBannerEventsConfigurations() {
     return this.mBNEvents;
   }

   public void addBannerPlacement(BannerPlacement placement) {
     if (placement != null) {
       this.mBNPlacements.add(placement);

       if (placement.getPlacementId() == 0) {
         this.mDefaultBNPlacement = placement;
       }
     }
   }

   public BannerPlacement getBannerPlacement(String placementName) {
     for (BannerPlacement placement : this.mBNPlacements) {
       if (placement.getPlacementName().equals(placementName)) {
         return placement;
       }
     }
     return null;
   }

   public BannerPlacement getDefaultBannerPlacement() {
     return this.mDefaultBNPlacement;
   }

   public int getBannerRefreshInterval() {
     return this.mBNRefreshInterval;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/BannerConfigurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */