package com.ironsource.eventsmodule;

import android.content.Context;

public abstract interface IEventsManager {
    public abstract void setBackupThreshold(int paramInt);

    public abstract void setMaxNumberOfEvents(int paramInt);

    public abstract void setMaxEventsPerBatch(int paramInt);

    public abstract void setOptOutEvents(int[] paramArrayOfInt, Context paramContext);

    public abstract void setEventsUrl(String paramString, Context paramContext);

    public abstract void setIsEventsEnabled(boolean paramBoolean);

    public abstract void log(EventData paramEventData);

    public abstract void setFormatterType(String paramString, Context paramContext);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/eventsmodule/IEventsManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */