package com.ironsource.sdk.listeners.internals;

import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ProductType;

import org.json.JSONObject;

public abstract interface DSAdProductListener {
    public abstract void onAdProductInitSuccess(SSAEnums.ProductType paramProductType, String paramString, AdUnitsReady paramAdUnitsReady);

    public abstract void onAdProductInitFailed(SSAEnums.ProductType paramProductType, String paramString1, String paramString2);

    public abstract void onAdProductOpen(SSAEnums.ProductType paramProductType, String paramString);

    public abstract void onAdProductClose(SSAEnums.ProductType paramProductType, String paramString);

    public abstract void onAdProductClick(SSAEnums.ProductType paramProductType, String paramString);

    public abstract void onAdProductEventNotificationReceived(SSAEnums.ProductType paramProductType, String paramString1, String paramString2, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/internals/DSAdProductListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */