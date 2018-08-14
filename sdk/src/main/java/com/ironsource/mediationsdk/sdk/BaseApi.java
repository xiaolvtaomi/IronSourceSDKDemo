package com.ironsource.mediationsdk.sdk;

import android.app.Activity;

public abstract interface BaseApi
{
  public abstract void onResume(Activity paramActivity);
  
  public abstract void onPause(Activity paramActivity);
  
  public abstract void setAge(int paramInt);
  
  public abstract void setGender(String paramString);
  
  public abstract void setMediationSegment(String paramString);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/BaseApi.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */