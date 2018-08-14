package com.ironsource.sdk.listeners;

import org.json.JSONObject;

public abstract interface OnOfferWallListener {
    public abstract void onOWShowSuccess(String paramString);

    public abstract void onOWShowFail(String paramString);

    public abstract boolean onOWAdCredited(int paramInt1, int paramInt2, boolean paramBoolean);

    public abstract void onGetOWCreditsFailed(String paramString);

    public abstract void onOWAdClosed();

    /**
     * @deprecated
     */
    public abstract void onOWGeneric(String paramString1, String paramString2);

    public abstract void onOfferwallInitSuccess();

    public abstract void onOfferwallInitFail(String paramString);

    public abstract void onOfferwallEventNotificationReceived(String paramString, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/listeners/OnOfferWallListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */