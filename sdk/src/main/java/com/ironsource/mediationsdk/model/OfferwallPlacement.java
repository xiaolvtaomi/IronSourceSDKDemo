package com.ironsource.mediationsdk.model;


public class OfferwallPlacement {
    private int mPlacementId;
    private String mPlacementName;

    public OfferwallPlacement(int placementId, String placementName) {
        this.mPlacementId = placementId;
        this.mPlacementName = placementName;
    }

    public int getPlacementId() {
        return this.mPlacementId;
    }

    public String getPlacementName() {
        return this.mPlacementName;
    }

    public String toString() {
        return "placement name: " + this.mPlacementName + ", placement id: " + this.mPlacementId;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/OfferwallPlacement.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */