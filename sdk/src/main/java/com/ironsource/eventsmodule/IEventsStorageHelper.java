package com.ironsource.eventsmodule;

import java.util.ArrayList;
import java.util.List;

public abstract interface IEventsStorageHelper {
    public abstract void saveEvents(List<EventData> paramList, String paramString);

    public abstract ArrayList<EventData> loadEvents(String paramString);

    public abstract void clearEvents(String paramString);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/eventsmodule/IEventsStorageHelper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */