 package com.ironsource.mediationsdk.model;

 import java.util.ArrayList;



 public class OfferwallConfigurations
 {
   private ArrayList<OfferwallPlacement> mOWPlacements;
   private static final int DEFAULT_OW_PLACEMENT_ID = 0;
   private OfferwallPlacement mDefaultOWPlacement;

   public OfferwallConfigurations()
   {
     this.mOWPlacements = new ArrayList();
   }

   public void addOfferwallPlacement(OfferwallPlacement placement) {
     if (placement != null) {
       this.mOWPlacements.add(placement);
       if (0 == placement.getPlacementId()) {
         this.mDefaultOWPlacement = placement;
       }
     }
   }

   public OfferwallPlacement getOfferwallPlacement(String placementName) {
     for (OfferwallPlacement placement : this.mOWPlacements) {
       if (placement.getPlacementName().equals(placementName))
         return placement;
     }
     return null;
   }

   public OfferwallPlacement getDefaultOfferwallPlacement() {
     return this.mDefaultOWPlacement;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/OfferwallConfigurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */