package com.ironsource.eventsmodule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;


public class DataBaseEventsStorage
        extends SQLiteOpenHelper
        implements IEventsStorageHelper {
    private static DataBaseEventsStorage mInstance;
    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";
    private final int DB_RETRY_NUM = 4;
    private final int DB_OPEN_BACKOFF_TIME = 400;

    private final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS events";

    private final String SQL_CREATE_ENTRIES = "CREATE TABLE events (_id INTEGER PRIMARY KEY,eventid INTEGER,timestamp INTEGER,type TEXT,data TEXT )";


    public DataBaseEventsStorage(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    public static synchronized DataBaseEventsStorage getInstance(Context context, String databaseName, int databaseVersion) {
        if (mInstance == null) {
            mInstance = new DataBaseEventsStorage(context, databaseName, databaseVersion);
        }
        return mInstance;
    }


    public synchronized void saveEvents(List<EventData> events, String type) {
        if ((events == null) || (events.isEmpty())) {
            return;
        }
        SQLiteDatabase db = null;
        try {
            db = getDataBaseWithRetries(true);
            for (EventData toInsert : events) {
                ContentValues values = getContentValuesForEvent(toInsert, type);


                if ((db != null) && (values != null)) {
                    db.insert("events", null, values);
                }
            }
        } catch (Throwable e) {
            Log.e("IronSource", "Exception while saving events: ", e);
        } finally {
            if ((db != null) && (db.isOpen())) {
                db.close();
            }
        }
    }

    public synchronized ArrayList<EventData> loadEvents(String type) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<EventData> events = new ArrayList();
        try {
            db = getDataBaseWithRetries(false);

            String whereClause = "type = ?";
            String[] whereArgs = {type};
            String orderByClause = "timestamp ASC";


            cursor = db.query("events", null, whereClause, whereArgs, null, null, orderByClause);


            if (cursor.getCount() > 0) {
                cursor.moveToFirst();


                while (!cursor.isAfterLast()) {
                    int eventId = cursor.getInt(cursor.getColumnIndex("eventid"));
                    long timeStamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                    String data = cursor.getString(cursor.getColumnIndex("data"));

                    EventData event = new EventData(eventId, timeStamp, new JSONObject(data));
                    events.add(event);

                    cursor.moveToNext();
                }

                cursor.close();
            }
        } catch (Throwable e) {
            Log.e("IronSource", "Exception while loading events: ", e);
        } finally {
            if ((cursor != null) && (!cursor.isClosed()))
                cursor.close();
            if ((db != null) && (db.isOpen())) {
                db.close();
            }
        }
        return events;
    }

    public synchronized void clearEvents(String type) {
        SQLiteDatabase db = null;


        String whereClause = "type = ?";
        String[] whereArgs = {type};
        try {
            db = getDataBaseWithRetries(true);

            db.delete("events", whereClause, whereArgs);
        } catch (Throwable e) {
            Log.e("IronSource", "Exception while clearing events: ", e);
        } finally {
            if ((db != null) && (db.isOpen()))
                db.close();
        }
    }

    private ContentValues getContentValuesForEvent(EventData event, String type) {
        ContentValues values = null;

        if (event != null) {
            values = new ContentValues(4);


            values.put("eventid", Integer.valueOf(event.getEventId()));
            values.put("timestamp", Long.valueOf(event.getTimeStamp()));
            values.put("type", type);
            values.put("data", event.getAdditionalData());
        }

        return values;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY,eventid INTEGER,timestamp INTEGER,type TEXT,data TEXT )");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS events");
        onCreate(db);
    }


    private synchronized SQLiteDatabase getDataBaseWithRetries(boolean writable)
            throws Throwable {
        int count = 0;
        for (; ; ) {
            try {
                if (writable) {
                    return getWritableDatabase();
                }

                return getReadableDatabase();
            } catch (Throwable t) {
                count++;
                if (count >= 4)
                    throw t;
                SystemClock.sleep(count * 400);
            }
        }
    }

    static abstract class EventEntry
            implements BaseColumns {
        public static final String TABLE_NAME = "events";
        public static final int NUMBER_OF_COLUMNS = 4;
        public static final String COLUMN_NAME_EVENT_ID = "eventid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DATA = "data";
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/eventsmodule/DataBaseEventsStorage.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */