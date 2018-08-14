package com.ironsource.mediationsdk.events;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.ironsource.eventsmodule.DataBaseEventsStorage;
import com.ironsource.eventsmodule.EventData;
import com.ironsource.eventsmodule.EventsSender;
import com.ironsource.eventsmodule.IEventsManager;
import com.ironsource.eventsmodule.IEventsSenderResultListener;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.IronSourceSegment;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.model.ServerSegmetData;
import com.ironsource.mediationsdk.sdk.GeneralProperties;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;


public abstract class BaseEventsManager
        implements IEventsManager {
    final int DEFAULT_BACKUP_THRESHOLD = 1;
    final int DEFAULT_MAX_NUMBER_OF_EVENTS = 100;
    final int DEFAULT_MAX_EVENTS_PER_BATCH = 5000;

    final int DATABASE_VERSION = 5;
    final String DATABASE_NAME = "supersonic_sdk.db";

    private static final String KEY_SESSION_DEPTH = "sessionDepth";
    final String KEY_PROVIDER = "provider";
    final String KEY_PLACEMENT = "placement";
    private final String MEDIATION_ABT = "abt";

    private boolean mHasServerResponse;

    private boolean mHadTopPriorityEvent = false;

    private DataBaseEventsStorage mDbStorage;

    private AbstractEventsFormatter mFormatter;
    private ArrayList<EventData> mLocalEvents;
    private boolean mIsEventsEnabled = true;

    private int mTotalEvents;

    private String mSessionId;
    private Context mContext;
    private int mMaxNumberOfEvents = DEFAULT_MAX_NUMBER_OF_EVENTS;
    private int mMaxEventsPerBatch = DEFAULT_MAX_EVENTS_PER_BATCH;
    private int mBackupThreshold = DEFAULT_BACKUP_THRESHOLD;
    private int[] mOptOutEvents;
    int mAdUnitType;
    String mFormatterType;
    String mEventType;
    private EventThread mEventThread;
    private IronSourceSegment mSegment;
    private ServerSegmetData mServerSegmentData;
    private IronSourceLoggerManager mLoggerManager;

    void initState() {
        this.mLocalEvents = new ArrayList();
        this.mTotalEvents = 0;
        this.mFormatter = EventsFormatterFactory.getFormatter(this.mFormatterType, this.mAdUnitType);
        this.mEventThread = new EventThread(this.mEventType + "EventThread");
        this.mEventThread.start();
        this.mEventThread.prepareHandler();
        this.mLoggerManager = IronSourceLoggerManager.getLogger();
        this.mSessionId = IronSourceObject.getInstance().getSessionId();
    }

    public synchronized void start(Context context, IronSourceSegment segment) {
        this.mFormatterType = IronSourceUtils.getDefaultEventsFormatterType(context, this.mEventType, this.mFormatterType);
        verifyCurrentFormatter(this.mFormatterType);
        this.mFormatter.setEventsServerUrl(IronSourceUtils.getDefaultEventsURL(context, this.mEventType, null));
        this.mDbStorage = DataBaseEventsStorage.getInstance(context, DATABASE_NAME, 5);
        backupEventsToDb();
        this.mOptOutEvents = IronSourceUtils.getDefaultOptOutEvents(context, this.mEventType);
        this.mSegment = segment;
        this.mContext = context;
    }

    public synchronized void setServerSegmentData(ServerSegmetData serverSegment) {
        this.mServerSegmentData = serverSegment;
    }

    public synchronized void log(final EventData event) {
        this.mEventThread.postTask(new Runnable() {
            public void run() {
                if ((event == null) || (!mIsEventsEnabled)) {
                    return;
                }
                event.addToAdditionalData("eventSessionId", mSessionId);
                event.addToAdditionalData("connectionType", IronSourceUtils.getConnectionType(mContext));

                try {
                    String ret = "{\"eventId\":" + event.getEventId() + ",\"timestamp\":" + event.getTimeStamp() + "," + event.getAdditionalData().substring(1);
                    mLoggerManager.log(IronSourceLogger.IronSourceTag.EVENT, ret.replace(",", "\n"), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (shouldEventBeLogged(event)) {
                    if ((event.getEventId() != 14) && (event.getEventId() != 40) && (event.getEventId() != 41)) {
                        int sessionDepth = getSessionDepth(event);
                        boolean shouldUseNewDepth = increaseSessionDepthIfNeeded(event);
                        if (shouldUseNewDepth) {
                            sessionDepth = getSessionDepth(event);
                        }

                        event.addToAdditionalData(KEY_SESSION_DEPTH, Integer.valueOf(sessionDepth));
                    }

                    if (shouldExtractCurrentPlacement(event)) {
                        setCurrentPlacement(event);
                    } else if ((!TextUtils.isEmpty(getCurrentPlacement(event.getEventId()))) && (shouldIncludeCurrentPlacement(event))) {
                        event.addToAdditionalData(KEY_PLACEMENT, getCurrentPlacement(event.getEventId()));
                    }


                    mLocalEvents.add(event);
                    mTotalEvents++;
                }

                boolean isTopPriority = isTopPriorityEvent(event);

                if ((!mHadTopPriorityEvent) && (isTopPriority)) {
                    mHadTopPriorityEvent = true;
                }
                if (mDbStorage != null) {
                    if (shouldSendEvents()) {
                        sendEvents();
                    } else if ((shouldBackupEventsToDb(mLocalEvents)) || (isTopPriority)) {
                        backupEventsToDb();
                    }
                }
            }
        });
    }


    private void sendEvents() {
        this.mHadTopPriorityEvent = false;

        ArrayList<EventData> storedEvents = this.mDbStorage.loadEvents(this.mEventType);
        ArrayList<EventData> combinedEventList = initCombinedEventList(this.mLocalEvents, storedEvents, this.mMaxEventsPerBatch);

        this.mLocalEvents.clear();
        this.mDbStorage.clearEvents(this.mEventType);
        this.mTotalEvents = 0;

        if (combinedEventList.size() > 0) {
            JSONObject generalProperties = GeneralProperties.getProperties().toJSON();

            try {
                updateSegmentsData(generalProperties);


                String abt = IronSourceUtils.getAbt();
                if (!TextUtils.isEmpty(abt)) {
                    generalProperties.put(MEDIATION_ABT, abt);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            String dataToSend = this.mFormatter.format(combinedEventList, generalProperties);
            new EventsSender(new IEventsSenderResultListener() {
                public synchronized void onEventsSenderResult(final ArrayList<EventData> extraData, final boolean success) {
                    mEventThread.postTask(new Runnable() {
                        public void run() {
                            if (success) {
                                ArrayList<EventData> events = mDbStorage.loadEvents(mEventType);
                                mTotalEvents = (events.size() + mLocalEvents.size());

                            } else if (extraData != null) {
                                mDbStorage.saveEvents(extraData, mEventType);

                                ArrayList<EventData> storedEvents = mDbStorage.loadEvents(mEventType);

                                mTotalEvents = (storedEvents.size() + mLocalEvents.size());
                            }
                        }
                    });
                }
            }).execute(new Object[]{dataToSend, this.mFormatter.getEventsServerUrl(), combinedEventList});
        }
    }

    private ArrayList<EventData> initCombinedEventList(ArrayList<EventData> localEvents, ArrayList<EventData> storedEvents, int maxSize) {
        ArrayList<EventData> allEvents = new ArrayList();
        allEvents.addAll(localEvents);
        allEvents.addAll(storedEvents);

        Collections.sort(allEvents, new Comparator<EventData>() {
            public int compare(EventData event1, EventData event2) {
                if (event1.getTimeStamp() >= event2.getTimeStamp()) {
                    return 1;
                }
                return -1;
            }
        });

        ArrayList<EventData> result;
        if (allEvents.size() <= maxSize) {
            result = new ArrayList(allEvents);
        } else {
            result = new ArrayList(allEvents.subList(0, maxSize));
            List<EventData> eventsToSave = allEvents.subList(maxSize, allEvents.size());
            this.mDbStorage.saveEvents(eventsToSave, this.mEventType);
        }
        return result;
    }

    private void verifyCurrentFormatter(String formatterType) {
        if ((this.mFormatter == null) || (!this.mFormatter.getFormatterType().equals(formatterType))) {
            this.mFormatter = EventsFormatterFactory.getFormatter(formatterType, this.mAdUnitType);
        }
    }

    public void setBackupThreshold(int backupThreshold) {
        if (backupThreshold > 0) {
            this.mBackupThreshold = backupThreshold;
        }
    }

    public void setMaxNumberOfEvents(int maxNumberOfEvents) {
        if (maxNumberOfEvents > 0) {
            this.mMaxNumberOfEvents = maxNumberOfEvents;
        }
    }

    public void setMaxEventsPerBatch(int maxEventsPerBatch) {
        if (maxEventsPerBatch > 0) {
            this.mMaxEventsPerBatch = maxEventsPerBatch;
        }
    }

    public void setOptOutEvents(int[] optOutEvents, Context context) {
        this.mOptOutEvents = optOutEvents;
        IronSourceUtils.saveDefaultOptOutEvents(context, this.mEventType, optOutEvents);
    }

    public void setEventsUrl(String eventsUrl, Context context) {
        if (!TextUtils.isEmpty(eventsUrl)) {
            if (this.mFormatter != null) {
                this.mFormatter.setEventsServerUrl(eventsUrl);
            }
            IronSourceUtils.saveDefaultEventsURL(context, this.mEventType, eventsUrl);
        }
    }

    public void setFormatterType(String formatterType, Context context) {
        if (!TextUtils.isEmpty(formatterType)) {
            this.mFormatterType = formatterType;
            IronSourceUtils.saveDefaultEventsFormatterType(context, this.mEventType, formatterType);
            verifyCurrentFormatter(formatterType);
        }
    }

    public void setIsEventsEnabled(boolean isEnabled) {
        this.mIsEventsEnabled = isEnabled;
    }

    private void backupEventsToDb() {
        this.mDbStorage.saveEvents(this.mLocalEvents, this.mEventType);
        this.mLocalEvents.clear();
    }


    private boolean shouldSendEvents() {
        boolean shouldSendEvents = ((this.mTotalEvents >= this.mMaxNumberOfEvents) || (this.mHadTopPriorityEvent)) && (this.mHasServerResponse);
        return shouldSendEvents;
    }


    private boolean shouldBackupEventsToDb(ArrayList<EventData> events) {
        boolean shouldBackup = false;

        if (events != null) {
            shouldBackup = events.size() >= this.mBackupThreshold;
        }
        return shouldBackup;
    }

    private boolean shouldEventBeLogged(EventData event) {
        boolean logEvent = true;

        if ((event != null) && (this.mOptOutEvents != null) && (this.mOptOutEvents.length > 0)) {
            int eventId = event.getEventId();
            for (int i = 0; i < this.mOptOutEvents.length; i++) {
                if (eventId == this.mOptOutEvents[i]) {
                    logEvent = false;
                    break;
                }
            }
        }

        return logEvent;
    }

    public void setHasServerResponse(boolean hasResponse) {
        this.mHasServerResponse = hasResponse;
    }

    String getProviderNameForEvent(EventData event) {
        String provider = "";
        try {
            JSONObject eventData = new JSONObject(event.getAdditionalData());
            provider = eventData.optString(KEY_PROVIDER, "");
        } catch (JSONException e) {
            return "";
        }
        return provider;
    }

    public void triggerEventsSend() {
        sendEvents();
    }

    public void sendEventToUrl(EventData eventData, String url) {
        try {
            ArrayList<EventData> singleEventArray = new ArrayList();
            singleEventArray.add(eventData);

            String dataToSend = this.mFormatter.format(singleEventArray, GeneralProperties.getProperties().toJSON());
            new EventsSender().execute(new Object[]{dataToSend, url, null});
        } catch (Exception localException) {
        }
    }

    private void updateSegmentsData(JSONObject object) {
        try {
            if (this.mSegment != null) {
                if (this.mSegment.getAge() > 0)
                    object.put("age", this.mSegment.getAge());
                if (!TextUtils.isEmpty(this.mSegment.getGender()))
                    object.put("gen", this.mSegment.getGender());
                if (this.mSegment.getLevel() > 0)
                    object.put("lvl", this.mSegment.getLevel());
                if (this.mSegment.getIsPaying() != null)
                    object.put("pay", this.mSegment.getIsPaying().get());
                if (this.mSegment.getIapt() > 0.0D)
                    object.put("iapt", this.mSegment.getIapt());
                if (this.mSegment.getUcd() > 0L) {
                    object.put("ucd", this.mSegment.getUcd());
                }
            }
            if (this.mServerSegmentData != null) {
                String id = this.mServerSegmentData.getSegmentId();
                if (!TextUtils.isEmpty(id)) {
                    object.put("segmentId", id);
                }
                JSONObject customs = this.mServerSegmentData.getCustomSegments();
                Iterator<String> iterator = customs.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    object.put(key, customs.get(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected abstract boolean shouldExtractCurrentPlacement(EventData paramEventData);


    protected abstract boolean shouldIncludeCurrentPlacement(EventData paramEventData);


    protected abstract boolean isTopPriorityEvent(EventData paramEventData);


    protected abstract int getSessionDepth(EventData paramEventData);

    protected abstract void setCurrentPlacement(EventData paramEventData);

    protected abstract String getCurrentPlacement(int paramInt);

    protected abstract boolean increaseSessionDepthIfNeeded(EventData paramEventData);

    private class EventThread
            extends HandlerThread {
        private Handler mHandler;

        EventThread(String name) {
            super(name);
        }

        void postTask(Runnable task) {
            this.mHandler.post(task);
        }

        void prepareHandler() {
            this.mHandler = new Handler(getLooper());
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/events/BaseEventsManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */