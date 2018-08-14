package com.ironsource.mediationsdk.model;


public enum PlacementCappingType {
    PER_DAY("d"), PER_HOUR("h");

    public String value;

    private PlacementCappingType(String value) {
        this.value = value;
    }

    public String toString() {
        return this.value;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/PlacementCappingType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */