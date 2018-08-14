package com.ironsource.mediationsdk.sdk;

import android.app.Activity;
import org.json.JSONObject;

public abstract interface OfferwallAdapterApi
{
  public abstract void setInternalOfferwallListener(InternalOfferwallListener paramInternalOfferwallListener);
  
  public abstract void showOfferwall(String paramString, JSONObject paramJSONObject);
  
  public abstract boolean isOfferwallAvailable();
  
  public abstract void getOfferwallCredits();
  
  public abstract void initOfferwall(Activity paramActivity, String paramString1, String paramString2, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/OfferwallAdapterApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */