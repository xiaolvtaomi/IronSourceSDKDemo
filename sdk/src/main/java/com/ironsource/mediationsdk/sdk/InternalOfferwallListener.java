package com.ironsource.mediationsdk.sdk;

import com.ironsource.mediationsdk.logger.IronSourceError;

public abstract interface InternalOfferwallListener
  extends OfferwallListener
{
  public abstract void onOfferwallAvailable(boolean paramBoolean, IronSourceError paramIronSourceError);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/InternalOfferwallListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */