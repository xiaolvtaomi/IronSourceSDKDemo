package com.ironsource.mediationsdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Base64;

import com.ironsource.mediationsdk.AbstractAdapter;
import com.ironsource.mediationsdk.AbstractSmash;
import com.ironsource.mediationsdk.BannerSmash;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.logger.ThreadExceptionHandler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceUtils {
    private static final String SDK_VERSION = "6.7.10";
    private static final String SHARED_PREFERENCES_NAME = "Mediation_Shared_Preferences";
    private static final String LAST_RESPONSE = "last_response";
    private static final String GENERAL_PROPERTIES = "general_properties";
    private static final String DEFAULT_RV_EVENTS_URL = "default_rv_events_url";
    private static final String DEFAULT_IS_EVENTS_URL = "default_is_events_url";
    private static final String DEFAULT_RV_EVENTS_FORMATTER_TYPE = "default_rv_events_formatter_type";
    private static final String DEFAULT_IS_EVENTS_FORMATTER_TYPE = "default_is_events_formatter_type";
    private static final String DEFAULT_RV_OPT_OUT_EVENTS = "default_rv_opt_out_events";
    private static final String DEFAULT_IS_OPT_OUT_EVENTS = "default_is_opt_out_events";
    private static final String PROVIDER_KEY = "provider";
    private static final String SDK_VERSION_KEY = "providerSDKVersion";
    private static final String ADAPTER_VERSION_KEY = "providerAdapterVersion";
    private static final String SUB_PROVIDER_ID_KEY = "spId";
    private static final String PROVIDER_PRIORITY = "providerPriority";
    private static final String NETWORK_INSTANCE_KEY = "networkInstance";
    public static final String KEY = "C38FB23A402222A0C17D34A92F971D1F";
    private static int serr = 1;
    private static String mAbt = "";

    private static void setSerr(int value) {
        serr = value;
    }

    public static int getSerr() {
        return serr;
    }

    static void setABT(String abt) {
        mAbt = abt;
    }

    public static String getAbt() {
        return mAbt;
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Throwable e) {
            if (input == null) {
                IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getMD5(input:null)", e);
            } else
                IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getMD5(input:" + input + ")", e);
        }
        return "";
    }

    private static String getSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, digest);
            return String.format("%064x", new Object[]{number});
        } catch (NoSuchAlgorithmException e) {
            if (input == null) {
                IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getSHA256(input:null)", e);
            } else
                IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getSHA256(input:" + input + ")", e);
        }
        return "";
    }


    public static String getTransId(String strToTransId) {
        return getSHA256(strToTransId);
    }

    public static int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }


    public static String getConnectionType(Context c) {
        String CONNECTION_WIFI = "WIFI";
        String CONNECTION_MOBILE = "MOBILE";

        if (c == null) {
            return "cellular";
        }

        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return "cellular";
        }

        NetworkInfo info = cm.getActiveNetworkInfo();

        if ((info != null) && (info.isConnected())) {
            if (info.getTypeName().equalsIgnoreCase("MOBILE")) {
                return "cellular";
            }
            if (info.getTypeName().equalsIgnoreCase("WIFI")) {
                return "wifi";
            }
        } else {
            return "none";
        }

        return "cellular";
    }


    public static String getSDKVersion() {
        return SDK_VERSION;
    }


    public static void createAndStartWorker(Runnable runnable, String threadName) {
        Thread worker = new Thread(runnable, threadName);
        worker.setUncaughtExceptionHandler(new ThreadExceptionHandler());
        worker.start();
    }


    public static String getBase64Auth(String loginUsername, String loginPass) {
        String source = loginUsername + ":" + loginPass;
        return "Basic " + Base64.encodeToString(source.getBytes(), 10);
    }

    private static String getDefaultEventsUrlByEventType(String eventType) {
        if ("IS".equals(eventType))
            return "default_is_events_url";
        if ("RV".equals(eventType)) {
            return DEFAULT_RV_EVENTS_URL;
        }
        return "";
    }

    private static String getDefaultOptOutEventsByEventType(String eventType) {
        if ("IS".equals(eventType))
            return "default_is_opt_out_events";
        if ("RV".equals(eventType)) {
            return "default_rv_opt_out_events";
        }
        return "";
    }

    private static String getDefaultFormatterTypeByEventType(String eventType) {
        if ("IS".equals(eventType))
            return "default_is_events_formatter_type";
        if ("RV".equals(eventType)) {
            return "default_rv_events_formatter_type";
        }
        return "";
    }

    public static synchronized void saveDefaultEventsURL(Context context, String eventType, String eventsUrl) {
        try {
            SharedPreferences preferences = context.getSharedPreferences("SHARED_PREFERENCES_NAME", 0);
            Editor editor = preferences.edit();
            editor.putString(getDefaultEventsUrlByEventType(eventType), eventsUrl);
            editor.commit();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultEventsURL(eventType: " + eventType + ", eventsUrl:" + eventsUrl + ")", e);
        }
    }

    public static synchronized void saveDefaultOptOutEvents(Context context, String eventType, int[] optOutEvents) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            Editor editor = preferences.edit();

            String optOutEventsString = null;
            if (optOutEvents != null) {
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < optOutEvents.length; i++) {
                    str.append(optOutEvents[i]).append(",");
                }
                optOutEventsString = str.toString();
            }
            editor.putString(getDefaultOptOutEventsByEventType(eventType), optOutEventsString);
            editor.commit();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultOptOutEvents(eventType: " + eventType + ", optOutEvents:" + optOutEvents + ")", e);
        }
    }

    public static synchronized void saveDefaultEventsFormatterType(Context context, String eventType, String formatterType) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            Editor editor = preferences.edit();
            editor.putString(getDefaultFormatterTypeByEventType(eventType), formatterType);
            editor.commit();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:saveDefaultEventsFormatterType(eventType: " + eventType + ", formatterType:" + formatterType + ")", e);
        }
    }

    public static synchronized String getDefaultEventsFormatterType(Context context, String eventType, String defaultFormatterType) {
        String formatterType = defaultFormatterType;
        try {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            formatterType = preferences.getString(getDefaultFormatterTypeByEventType(eventType), defaultFormatterType);
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:getDefaultEventsFormatterType(eventType: " + eventType + ", defaultFormatterType:" + defaultFormatterType + ")", e);
        }

        return formatterType;
    }

    public static synchronized String getDefaultEventsURL(Context context, String eventType, String defaultEventsURL) {
        String serverUrl = defaultEventsURL;
        try {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            serverUrl = preferences.getString(getDefaultEventsUrlByEventType(eventType), defaultEventsURL);
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:getDefaultEventsURL(eventType: " + eventType + ", defaultEventsURL:" + defaultEventsURL + ")", e);
        }

        return serverUrl;
    }

    public static synchronized int[] getDefaultOptOutEvents(Context context, String eventType) {
        int[] optOutEvents = null;
        try {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
            String optOutEventsString = preferences.getString(getDefaultOptOutEventsByEventType(eventType), null);
            if (!TextUtils.isEmpty(optOutEventsString)) {
                StringTokenizer stringTokenizer = new StringTokenizer(optOutEventsString, ",");

                ArrayList<Integer> result = new ArrayList();
                while (stringTokenizer.hasMoreTokens()) {
                    result.add(Integer.valueOf(Integer.parseInt(stringTokenizer.nextToken())));
                }

                optOutEvents = new int[result.size()];
                for (int i = 0; i < optOutEvents.length; i++) {
                    optOutEvents[i] = ((Integer) result.get(i)).intValue();
                }
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:getDefaultOptOutEvents(eventType: " + eventType + ")", e);
        }

        return optOutEvents;
    }


    public static synchronized void saveLastResponse(Context context, String response) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putString(LAST_RESPONSE, response);
        editor.apply();
    }


    public static String getLastResponse(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return preferences.getString(LAST_RESPONSE, "");
    }


    static synchronized void saveGeneralProperties(Context context, JSONObject properties) {
        if ((context == null) || (properties == null)) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putString(GENERAL_PROPERTIES, properties.toString());
        editor.apply();
    }


    public static synchronized JSONObject getGeneralProperties(Context context) {
        JSONObject result = new JSONObject();

        if (context == null) {
            return result;
        }
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        String generalPropertiesString = preferences.getString(GENERAL_PROPERTIES, result.toString());
        try {
            result = new JSONObject(generalPropertiesString);
        } catch (JSONException localJSONException) {
        }

        return result;
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected();
    }


    public static long getTimeStamp() {
        return System.currentTimeMillis();
    }

    public static JSONObject getProviderAdditionalData(AbstractSmash smash, boolean isDemandOnlyMode) {
        JSONObject data = new JSONObject();
        try {
            if (isDemandOnlyMode)
                data.put("networkInstance", "true");
            data.put("spId", smash.getSubProviderId());
            data.put("provider", smash.getName());
            data.put("providerSDKVersion", smash.getAdapter().getCoreSDKVersion());
            data.put("providerAdapterVersion", smash.getAdapter().getVersion());
            data.put("providerPriority", smash.getProviderPriority());
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:getProviderAdditionalData(adapter: " + smash.getName() + ")", e);
        }

        return data;
    }

    public static JSONObject getProviderAdditionalData(BannerSmash smash) {
        JSONObject data = new JSONObject();
        try {
            data.put("spId", smash.getSubProviderId());
            data.put("provider", smash.getName());
            data.put("providerSDKVersion", smash.getAdapter().getCoreSDKVersion());
            data.put("providerAdapterVersion", smash.getAdapter().getVersion());
            data.put("providerPriority", smash.getProviderPriority());
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "IronSourceUtils:getProviderAdditionalData(adapter: " + smash.getName() + ")", e);
        }

        return data;
    }

    public static JSONObject getMediationAdditionalData(boolean isDemandOnlyMode) {
        JSONObject data = new JSONObject();
        try {
            if (isDemandOnlyMode)
                data.put("networkInstance", "true");
            data.put("provider", "Mediation");
        } catch (JSONException localJSONException) {
        }

        return data;
    }

    static void saveStringToSharedPrefs(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String getStringFromSharedPrefs(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return preferences.getString(key, defaultValue);
    }

    static void saveBooleanToSharedPrefs(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    static boolean getBooleanFromSharedPrefs(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return preferences.getBoolean(key, defaultValue);
    }

    static void saveIntToSharedPrefs(Context context, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    static int getIntFromSharedPrefs(Context context, String key, int defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return preferences.getInt(key, defaultValue);
    }

    static void saveLongToSharedPrefs(Context context, String key, long value) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    static long getLongFromSharedPrefs(Context context, String key, long defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        return preferences.getLong(key, defaultValue);
    }


    public static JSONObject mergeJsons(JSONObject mainJson, JSONObject secondaryJson) {
        try {
            if ((mainJson == null) && (secondaryJson == null))
                return new JSONObject();
            if (mainJson == null)
                return secondaryJson;
            if (secondaryJson == null) {
                return mainJson;
            }
            Iterator it = secondaryJson.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (!mainJson.has(key))
                    mainJson.put(key, secondaryJson.get(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mainJson;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/IronSourceUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */