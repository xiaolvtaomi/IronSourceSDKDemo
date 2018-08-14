package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface OfferwallListener
{
  public abstract void onOfferwallAvailable(boolean paramBoolean);
  
  public abstract void onOfferwallOpened();
  
  public abstract void onOfferwallShowFailed(IronSourceError paramIronSourceError);
  
  public abstract boolean onOfferwallAdCredited(int paramInt1, int paramInt2, boolean paramBoolean);
  
  public abstract void onGetOfferwallCreditsFailed(IronSourceError paramIronSourceError);
  
  public abstract void onOfferwallClosed();
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/OfferwallListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */