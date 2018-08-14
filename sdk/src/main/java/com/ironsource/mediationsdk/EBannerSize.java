package com.ironsource.mediationsdk;


public enum EBannerSize {
    BANNER("banner"),
    LARGE("large"),
    RECTANGLE("rectangle"),


    TABLET("tablet"),

    SMART("smart");

    private String mValue;

    private EBannerSize(String value) {
        this.mValue = value;
    }

    public String toString() {
        return this.mValue;
    }

    public int getValue() {
        return ordinal() + 1;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/EBannerSize.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */