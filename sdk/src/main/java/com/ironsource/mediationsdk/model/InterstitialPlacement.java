 package com.ironsource.mediationsdk.model;


 public class InterstitialPlacement
 {
   private int mPlacementId;

   private String mPlacementName;

   private PlacementAvailabilitySettings mPlacementAvailabilitySettings;

   public InterstitialPlacement(int placementId, String placementName, PlacementAvailabilitySettings placementAvailabilitySettings)
   {
     this.mPlacementId = placementId;
     this.mPlacementName = placementName;
     this.mPlacementAvailabilitySettings = placementAvailabilitySettings;
   }

   public int getPlacementId() {
     return this.mPlacementId;
   }

   public String getPlacementName() {
     return this.mPlacementName;
   }

   public String toString()
   {
     return "placement name: " + this.mPlacementName;
   }

   public PlacementAvailabilitySettings getPlacementAvailabilitySettings() {
     return this.mPlacementAvailabilitySettings;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/InterstitialPlacement.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */