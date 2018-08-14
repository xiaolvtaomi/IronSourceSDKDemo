package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

public abstract interface OfferwallApi
{
  public abstract void showOfferwall();
  
  public abstract void showOfferwall(String paramString);
  
  public abstract boolean isOfferwallAvailable();
  
  public abstract void getOfferwallCredits();
  
  public abstract void setOfferwallListener(OfferwallListener paramOfferwallListener);
  
  public abstract void initOfferwall(Activity paramActivity, String paramString1, String paramString2);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/OfferwallApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */