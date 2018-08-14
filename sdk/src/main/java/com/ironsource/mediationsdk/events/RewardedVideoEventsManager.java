package com.ironsource.mediationsdk.events;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.utils.SessionDepthManager;

import org.json.JSONObject;


public class RewardedVideoEventsManager
        extends BaseEventsManager {
    private static RewardedVideoEventsManager sInstance;
    private String mCurrentRVPlacment;
    private String mCurrentOWPlacment;

    private RewardedVideoEventsManager() {
        this.mFormatterType = "outcome";
        this.mAdUnitType = 3;
        this.mEventType = "RV";
        this.mCurrentRVPlacment = "";
        this.mCurrentOWPlacment = "";
    }

    public static synchronized RewardedVideoEventsManager getInstance() {
        if (sInstance == null) {
            sInstance = new RewardedVideoEventsManager();
            sInstance.initState();
        }
        return sInstance;
    }

    protected boolean shouldExtractCurrentPlacement(EventData event) {
        return (event.getEventId() == 2) || (event.getEventId() == 10);
    }


    protected boolean shouldIncludeCurrentPlacement(EventData event) {
        return (event.getEventId() == 5) || (event.getEventId() == 6) || (event.getEventId() == 8) || (event.getEventId() == 9) || (event.getEventId() == 19) || (event.getEventId() == 20) || (event.getEventId() == 305);
    }


    protected boolean isTopPriorityEvent(EventData currentEvent) {
        return (currentEvent.getEventId() == 6) || (currentEvent.getEventId() == 5) || (currentEvent.getEventId() == 10) || (currentEvent.getEventId() == 14) || (currentEvent.getEventId() == 305);
    }

    protected int getSessionDepth(EventData event) {
        int sessionDepth = SessionDepthManager.getInstance().getSessionDepth(1);
        if ((event.getEventId() == 15) || ((event.getEventId() >= 300) && (event.getEventId() < 400))) {
            sessionDepth = SessionDepthManager.getInstance().getSessionDepth(0);
        }
        return sessionDepth;
    }


    protected void setCurrentPlacement(EventData event) {
        if ((event.getEventId() == 15) || ((event.getEventId() >= 300) && (event.getEventId() < 400))) {
            this.mCurrentOWPlacment = event.getAdditionalDataJSON().optString("placement");
        } else {
            this.mCurrentRVPlacment = event.getAdditionalDataJSON().optString("placement");
        }
    }

    protected String getCurrentPlacement(int eventId) {
        if ((eventId == 15) || ((eventId >= 300) && (eventId < 400))) {
            return this.mCurrentOWPlacment;
        }

        return this.mCurrentRVPlacment;
    }


    protected boolean increaseSessionDepthIfNeeded(EventData event) {
        if (event.getEventId() == 6) {
            SessionDepthManager.getInstance().increaseSessionDepth(1);
            return false;
        }
        if (event.getEventId() == 305) {
            SessionDepthManager.getInstance().increaseSessionDepth(0);
            return false;
        }

        return false;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/events/RewardedVideoEventsManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */