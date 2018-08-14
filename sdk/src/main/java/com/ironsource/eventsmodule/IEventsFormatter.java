package com.ironsource.eventsmodule;

import java.util.ArrayList;

import org.json.JSONObject;

public abstract interface IEventsFormatter {
    public abstract String format(ArrayList<EventData> paramArrayList, JSONObject paramJSONObject);
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/eventsmodule/IEventsFormatter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */