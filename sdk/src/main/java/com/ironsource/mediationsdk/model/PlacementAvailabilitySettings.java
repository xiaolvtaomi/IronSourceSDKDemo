package com.ironsource.mediationsdk.model;


public class PlacementAvailabilitySettings {
    private boolean isDeliveryEnabled;

    private boolean isCappingEnabled;

    private boolean isPacingEnabled;
    private PlacementCappingType cappingType;
    private int cappingValue;
    private int pacingValue;

    private PlacementAvailabilitySettings(boolean isDeliveryEnabled, boolean isCappingEnabled, boolean isPacingEnabled, PlacementCappingType cappingType, int cappingValue, int pacingValue) {
        this.isDeliveryEnabled = isDeliveryEnabled;
        this.isCappingEnabled = isCappingEnabled;
        this.isPacingEnabled = isPacingEnabled;
        this.cappingType = cappingType;
        this.cappingValue = cappingValue;
        this.pacingValue = pacingValue;
    }

    public boolean isDeliveryEnabled() {
        return this.isDeliveryEnabled;
    }

    public boolean isCappingEnabled() {
        return this.isCappingEnabled;
    }

    public boolean isPacingEnabled() {
        return this.isPacingEnabled;
    }

    public PlacementCappingType getCappingType() {
        return this.cappingType;
    }

    public int getCappingValue() {
        return this.cappingValue;
    }

    public int getPacingValue() {
        return this.pacingValue;
    }

    public static class PlacementAvailabilitySettingsBuilder {
        private boolean isDeliveryEnabled = true;
        private boolean isCappingEnabled = false;
        private boolean isPacingEnabled = false;
        private PlacementCappingType cappingType = null;
        private int cappingValue = 0;
        private int pacingValue = 0;


        public PlacementAvailabilitySettingsBuilder delivery(boolean isDeliveryEnabled) {
            this.isDeliveryEnabled = isDeliveryEnabled;
            return this;
        }

        public PlacementAvailabilitySettingsBuilder capping(boolean isCappingEnabled, PlacementCappingType cappingType, int cappingValue) {
            this.isCappingEnabled = isCappingEnabled;
            this.cappingType = (cappingType == null ? PlacementCappingType.PER_DAY : cappingType);
            this.cappingValue = cappingValue;
            return this;
        }

        public PlacementAvailabilitySettingsBuilder pacing(boolean isPacingEnabled, int pacingValue) {
            this.isPacingEnabled = isPacingEnabled;
            this.pacingValue = pacingValue;
            return this;
        }

        public PlacementAvailabilitySettings build() {
            return new PlacementAvailabilitySettings(this.isDeliveryEnabled, this.isCappingEnabled, this.isPacingEnabled, this.cappingType, this.cappingValue, this.pacingValue);
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/PlacementAvailabilitySettings.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */