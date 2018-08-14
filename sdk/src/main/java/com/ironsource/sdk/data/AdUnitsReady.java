package com.ironsource.sdk.data;

public class AdUnitsReady
        extends SSAObj {
    private static String TYPE = "type";
    private static String NUM_OF_AD_UNITS = "numOfAdUnits";
    private static String FIRST_CAMPAIGN_CREDITS = "firstCampaignCredits";
    private static String TOTAL_NUMBER_CREDITS = "totalNumberCredits";
    private static String PRODUCT_TYPE = "productType";
    private String mType;
    private String mProductType;
    private String mNumOfAdUnits;
    private String mFirstCampaignCredits;
    private String mTotalNumberCredits;
    private boolean mNumOfAdUnitsExist;

    public AdUnitsReady(String value) {
        super(value);

        if (containsKey(TYPE)) {
            setType(getString(TYPE));
        }

        if (containsKey(NUM_OF_AD_UNITS)) {
            setNumOfAdUnits(getString(NUM_OF_AD_UNITS));
            setNumOfAdUnitsExist(true);
        } else {
            setNumOfAdUnitsExist(false);
        }

        if (containsKey(FIRST_CAMPAIGN_CREDITS)) {
            setFirstCampaignCredits(getString(FIRST_CAMPAIGN_CREDITS));
        }

        if (containsKey(TOTAL_NUMBER_CREDITS)) {
            setTotalNumberCredits(getString(TOTAL_NUMBER_CREDITS));
        }

        if (containsKey(PRODUCT_TYPE)) {
            setProductType(getString(PRODUCT_TYPE));
        }
    }

    public String getType() {
        return this.mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getNumOfAdUnits() {
        return this.mNumOfAdUnits;
    }

    public void setNumOfAdUnits(String numOfAdUnits) {
        this.mNumOfAdUnits = numOfAdUnits;
    }

    public String getFirstCampaignCredits() {
        return this.mFirstCampaignCredits;
    }

    public void setFirstCampaignCredits(String firstCampaignCredits) {
        this.mFirstCampaignCredits = firstCampaignCredits;
    }

    public String getTotalNumberCredits() {
        return this.mTotalNumberCredits;
    }

    public void setTotalNumberCredits(String totalNumberCredits) {
        this.mTotalNumberCredits = totalNumberCredits;
    }

    private void setNumOfAdUnitsExist(boolean value) {
        this.mNumOfAdUnitsExist = value;
    }

    public boolean isNumOfAdUnitsExist() {
        return this.mNumOfAdUnitsExist;
    }

    public String getProductType() {
        return this.mProductType;
    }

    public void setProductType(String productType) {
        this.mProductType = productType;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/AdUnitsReady.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */