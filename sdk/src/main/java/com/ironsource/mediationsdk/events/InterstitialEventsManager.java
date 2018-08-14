package com.ironsource.mediationsdk.events;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.utils.SessionDepthManager;

import org.json.JSONObject;


public class InterstitialEventsManager
        extends BaseEventsManager {
    private static InterstitialEventsManager sInstance;
    private String mCurrentISPlacement;

    private InterstitialEventsManager() {
        this.mFormatterType = "ironbeast";
        this.mAdUnitType = 2;
        this.mEventType = "IS";
        this.mCurrentISPlacement = "";
    }

    public static synchronized InterstitialEventsManager getInstance() {
        if (sInstance == null) {
            sInstance = new InterstitialEventsManager();
            sInstance.initState();
        }

        return sInstance;
    }

    protected boolean shouldExtractCurrentPlacement(EventData event) {
        return (event.getEventId() == 23) || (event.getEventId() == 3001);
    }


    protected boolean shouldIncludeCurrentPlacement(EventData event) {
        return (event.getEventId() == 25) || (event.getEventId() == 26) || (event.getEventId() == 28) || (event.getEventId() == 29) || (event.getEventId() == 34);
    }


    protected boolean isTopPriorityEvent(EventData currentEvent) {
        return (currentEvent.getEventId() == 26) || (currentEvent.getEventId() == 25) || (currentEvent.getEventId() == 3005) || (currentEvent.getEventId() == 3015);
    }

    protected int getSessionDepth(EventData event) {
        boolean isBanner = (event.getEventId() >= 3000) && (event.getEventId() < 4000);
        return SessionDepthManager.getInstance().getSessionDepth(isBanner ? 3 : 2);
    }

    protected boolean increaseSessionDepthIfNeeded(EventData event) {
        if (event.getEventId() == 26) {
            SessionDepthManager.getInstance().increaseSessionDepth(2);
        } else if (event.getEventId() == 3305) {
            SessionDepthManager.getInstance().increaseSessionDepth(3);
        }

        return false;
    }

    protected void setCurrentPlacement(EventData event) {
        this.mCurrentISPlacement = event.getAdditionalDataJSON().optString("placement");
    }

    protected String getCurrentPlacement(int eventId) {
        return this.mCurrentISPlacement;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/events/InterstitialEventsManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */