package com.ironsource.mediationsdk.utils;

import android.content.Context;

import com.ironsource.mediationsdk.AbstractSmash;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class DailyCappingManager {
    private static final int RAND_MINUTES = 10;
    private Map<String, Integer> mSmashIdToMaxShowsPerDay;
    private Map<String, Integer> mSmashIdToCounter;
    private Map<String, String> mSmashIdToCounterDate;
    private String mAdUnitName;
    private Context mContext;
    private Timer mTimer = null;

    private DailyCappingListener mListener;
    private IronSourceLoggerManager mLogger;

    public DailyCappingManager(String adUnitName, DailyCappingListener listener) {
        this.mAdUnitName = adUnitName;
        this.mListener = listener;
        this.mSmashIdToMaxShowsPerDay = new HashMap();
        this.mSmashIdToCounter = new HashMap();
        this.mSmashIdToCounterDate = new HashMap();
        this.mLogger = IronSourceLoggerManager.getLogger();
        scheduleTimer();
    }


    public void setContext(Context context) {
        this.mContext = context;
    }


    public void addSmash(AbstractSmash smash) {
        synchronized (this) {
            try {
                if (smash.getMaxAdsPerDay() != 99) {
                    this.mSmashIdToMaxShowsPerDay.put(getUniqueId(smash), Integer.valueOf(smash.getMaxAdsPerDay()));
                }
            } catch (Exception e) {
                this.mLogger.logException(IronSourceLogger.IronSourceTag.INTERNAL, "addSmash", e);
            }
        }
    }


    public void increaseShowCounter(AbstractSmash smash) {
        synchronized (this) {
            try {
                String smashId = getUniqueId(smash);
                if (!this.mSmashIdToMaxShowsPerDay.containsKey(smashId)) {
                    return;
                }

                int count = getTodayShowCount(smashId);
                saveCounter(smashId, count + 1);
            } catch (Exception e) {
                this.mLogger.logException(IronSourceLogger.IronSourceTag.INTERNAL, "increaseShowCounter", e);
            }
        }
    }


    public boolean shouldSendCapReleasedEvent(AbstractSmash smash) {
        synchronized (this) {
            try {
                String smashId = getUniqueId(smash);
                if (!this.mSmashIdToMaxShowsPerDay.containsKey(smashId)) {
                    return false;
                }

                if (getTodayDate().equalsIgnoreCase(getCounterDate(smashId))) {
                    return false;
                }

                return ((Integer) this.mSmashIdToMaxShowsPerDay.get(smashId)).intValue() <= getCounter(smashId);
            } catch (Exception e) {
                this.mLogger.logException(IronSourceLogger.IronSourceTag.INTERNAL, "shouldSendCapReleasedEvent", e);
                return false;
            }
        }
    }


    public boolean isCapped(AbstractSmash smash) {
        synchronized (this) {
            try {
                String smashId = getUniqueId(smash);
                if (!this.mSmashIdToMaxShowsPerDay.containsKey(smashId)) {
                    return false;
                }

                return ((Integer) this.mSmashIdToMaxShowsPerDay.get(smashId)).intValue() <= getTodayShowCount(smashId);
            } catch (Exception e) {
                this.mLogger.logException(IronSourceLogger.IronSourceTag.INTERNAL, "isCapped", e);
                return false;
            }
        }
    }


    private void onTimerTick() {
        synchronized (this) {
            try {
                for (String smashId : this.mSmashIdToMaxShowsPerDay.keySet()) {
                    zeroCounter(smashId);
                }

                this.mListener.onDailyCapReleased();
                scheduleTimer();
            } catch (Exception e) {
                this.mLogger.logException(IronSourceLogger.IronSourceTag.INTERNAL, "onTimerTick", e);
            }
        }
    }


    private void scheduleTimer() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = new Timer();
        this.mTimer.schedule(new TimerTask() {
            public void run() {
                DailyCappingManager.this.onTimerTick();
            }
        }, getUtcMidnight());
    }


    private Date getUtcMidnight() {
        Random random = new Random();
        Calendar date = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, random.nextInt(10));
        date.set(Calendar.SECOND, random.nextInt(60));
        date.set(Calendar.MILLISECOND, random.nextInt(1000));
        date.add(Calendar.DAY_OF_MONTH, 1);
        return date.getTime();
    }


    private int getTodayShowCount(String smashId) {
        if (!getTodayDate().equalsIgnoreCase(getCounterDate(smashId))) {
            zeroCounter(smashId);
        }

        return getCounter(smashId);
    }


    private String getCounterDate(String smashId) {
        if (this.mSmashIdToCounterDate.containsKey(smashId)) {
            return (String) this.mSmashIdToCounterDate.get(smashId);
        }
        String ret = IronSourceUtils.getStringFromSharedPrefs(this.mContext, getDayKeyName(smashId), getTodayDate());
        this.mSmashIdToCounterDate.put(smashId, ret);
        return ret;
    }


    private int getCounter(String smashId) {
        if (this.mSmashIdToCounter.containsKey(smashId)) {
            return ((Integer) this.mSmashIdToCounter.get(smashId)).intValue();
        }
        int ret = IronSourceUtils.getIntFromSharedPrefs(this.mContext, getCounterKeyName(smashId), 0);
        this.mSmashIdToCounter.put(smashId, Integer.valueOf(ret));
        return ret;
    }


    private void saveCounter(String smashId, int counter) {
        this.mSmashIdToCounter.put(smashId, Integer.valueOf(counter));
        this.mSmashIdToCounterDate.put(smashId, getTodayDate());
        IronSourceUtils.saveIntToSharedPrefs(this.mContext, getCounterKeyName(smashId), counter);
        IronSourceUtils.saveStringToSharedPrefs(this.mContext, getDayKeyName(smashId), getTodayDate());
    }


    private void zeroCounter(String smashId) {
        this.mSmashIdToCounter.put(smashId, Integer.valueOf(0));
        this.mSmashIdToCounterDate.put(smashId, getTodayDate());
        IronSourceUtils.saveIntToSharedPrefs(this.mContext, getCounterKeyName(smashId), 0);
        IronSourceUtils.saveStringToSharedPrefs(this.mContext, getDayKeyName(smashId), getTodayDate());
    }


    private String getUniqueId(AbstractSmash smash) {
        return this.mAdUnitName + "_" + smash.getSubProviderId() + "_" + smash.getName();
    }


    private String getCounterKeyName(String smashId) {
        return smashId + "_counter";
    }


    private String getDayKeyName(String smashId) {
        return smashId + "_day";
    }


    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/DailyCappingManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */