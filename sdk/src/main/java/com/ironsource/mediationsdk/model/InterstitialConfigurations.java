 package com.ironsource.mediationsdk.model;

 import java.util.ArrayList;





 public class InterstitialConfigurations
 {
   private ArrayList<InterstitialPlacement> mISPlacements;
   private ApplicationEvents mISEvents;
   private int mISAdaptersSmartLoadAmount;
   private int mISAdaptersTimeOutInSeconds;
   private String mBackFillProviderName;
   private String mPremiumProviderName;
   private static final int DEFAULT_IS_PLACEMENT_ID = 0;
   private InterstitialPlacement mDefaultISPlacement;

   public InterstitialConfigurations()
   {
     this.mISPlacements = new ArrayList();
     this.mISEvents = new ApplicationEvents();
   }

   public InterstitialConfigurations(int adaptersSmartLoadAmount, int adaptersSmartLoadTimeout, ApplicationEvents events) {
     this.mISPlacements = new ArrayList();
     this.mISAdaptersSmartLoadAmount = adaptersSmartLoadAmount;
     this.mISAdaptersTimeOutInSeconds = adaptersSmartLoadTimeout;
     this.mISEvents = events;
   }

   public void addInterstitialPlacement(InterstitialPlacement placement) {
     if (placement != null) {
       this.mISPlacements.add(placement);

       if (placement.getPlacementId() == 0) {
         this.mDefaultISPlacement = placement;
       }
     }
   }

   public InterstitialPlacement getInterstitialPlacement(String placementName) {
     for (InterstitialPlacement placement : this.mISPlacements) {
       if (placement.getPlacementName().equals(placementName)) {
         return placement;
       }
     }
     return null;
   }

   public InterstitialPlacement getDefaultInterstitialPlacement() {
     return this.mDefaultISPlacement;
   }

   public int getInterstitialAdaptersSmartLoadAmount() {
     return this.mISAdaptersSmartLoadAmount;
   }

   public int getInterstitialAdaptersSmartLoadTimeout() {
     return this.mISAdaptersTimeOutInSeconds;
   }

   public ApplicationEvents getInterstitialEventsConfigurations() {
     return this.mISEvents;
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


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/InterstitialConfigurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */