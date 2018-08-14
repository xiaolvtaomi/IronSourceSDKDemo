package com.ironsource.sdk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.ironsource.sdk.data.SSABCParameters;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.BackButtonState;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.data.SSAObj;
import com.ironsource.sdk.data.SSASession;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceSharedPrefHelper {
    private static final String SUPERSONIC_SHARED_PREF = "supersonic_shared_preferen";
    private static final String VERSION = "version";
    private static final String SSA_SDK_DOWNLOAD_URL = "ssa_sdk_download_url";
    private static final String SSA_SDK_LOAD_URL = "ssa_sdk_load_url";
    private static final String UNIQUE_ID_RV = "unique_id_rv";
    private static final String UNIQUE_ID_OW = "unique_id_ow";
    private static final String UNIQUE_ID_IS = "unique_id_is";
    private static final String USER_ID_RV = "user_id_rv";
    private static final String USER_ID_OW = "user_id_ow";
    private static final String USER_ID_IS = "user_id_is";
    private static final String APPLICATION_KEY_RV = "application_key_rv";
    private static final String APPLICATION_KEY_OW = "application_key_ow";
    private static final String APPLICATION_KEY_IS = "application_key_is";
    private static final String SSA_RV_PARAMETER_CONNECTION_RETRIES = "ssa_rv_parameter_connection_retries";
    private static final String BACK_BUTTON_STATE = "back_button_state";
    private static final String SEARCH_KEYS = "search_keys";
    private static final String REGISTER_SESSIONS = "register_sessions";
    private static final String SESSIONS = "sessions";
    private static final String IS_REPORTED = "is_reported";
    private SharedPreferences mSharedPreferences;
    private static IronSourceSharedPrefHelper mInstance;

    private IronSourceSharedPrefHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("supersonic_shared_preferen", 0);
    }

    public static synchronized IronSourceSharedPrefHelper getSupersonicPrefHelper(Context context) {
        if (mInstance == null) {
            mInstance = new IronSourceSharedPrefHelper(context);
        }
        return mInstance;
    }

    public static synchronized IronSourceSharedPrefHelper getSupersonicPrefHelper() {
        return mInstance;
    }


    public String getConnectionRetries() {
        return this.mSharedPreferences.getString("ssa_rv_parameter_connection_retries", "3");
    }


    public void setSSABCParameters(SSABCParameters object) {
        Editor editor = this.mSharedPreferences.edit();


        editor.putString("ssa_rv_parameter_connection_retries", object.getConnectionRetries());
        editor.commit();
    }


    public void setBackButtonState(String value) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString("back_button_state", value);

        editor.commit();
    }


    public SSAEnums.BackButtonState getBackButtonState() {
        String stateStr = this.mSharedPreferences.getString("back_button_state", "2");

        int state = Integer.parseInt(stateStr);

        if (state == 0)
            return SSAEnums.BackButtonState.None;
        if (state == 1)
            return SSAEnums.BackButtonState.Device;
        if (state == 2) {
            return SSAEnums.BackButtonState.Controller;
        }

        return SSAEnums.BackButtonState.Controller;
    }


    public void setSearchKeys(String value) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString("search_keys", value);

        editor.commit();
    }


    public List<String> getSearchKeys() {
        String value = this.mSharedPreferences.getString("search_keys", null);

        ArrayList<String> keys = new ArrayList();

        if (value != null) {
            SSAObj ssaObj = new SSAObj(value);

            if (ssaObj.containsKey("searchKeys")) {
                JSONArray jsonArr = (JSONArray) ssaObj.get("searchKeys");
                try {
                    keys.addAll(ssaObj.toList(jsonArr));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return keys;
    }


    public JSONArray getSessions() {
        JSONArray jsArr = null;

        String value = this.mSharedPreferences.getString("sessions", null);

        if (value == null) {
            return new JSONArray();
        }
        try {
            jsArr = new JSONArray(value);
        } catch (JSONException e) {
            jsArr = new JSONArray();
        }

        return jsArr;
    }


    public void deleteSessions() {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString("sessions", null);

        editor.commit();
    }


    public void addSession(SSASession session) {
        if (getShouldRegisterSessions()) {
            JSONObject jsObj = new JSONObject();
            try {
                jsObj.put("sessionStartTime", session.getSessionStartTime());
                jsObj.put("sessionEndTime", session.getSessionEndTime());
                jsObj.put("sessionType", session.getSessionType());
                jsObj.put("connectivity", session.getConnectivity());
            } catch (JSONException localJSONException) {
            }

            JSONArray jsArr = getSessions();

            if (jsArr == null) {
                jsArr = new JSONArray();
            }

            jsArr.put(jsObj);

            Editor editor = this.mSharedPreferences.edit();

            editor.putString("sessions", jsArr.toString());

            editor.commit();
        }
    }

    private boolean getShouldRegisterSessions() {
        return this.mSharedPreferences.getBoolean("register_sessions", true);
    }


    public void setShouldRegisterSessions(boolean value) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putBoolean("register_sessions", value);

        editor.commit();
    }


    public boolean setUserData(String key, String value) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString(key, value);

        return editor.commit();
    }


    public String getUserData(String key) {
        String value = this.mSharedPreferences.getString(key, null);

        if (value != null) {
            return value;
        }
        return "{}";
    }


    public String getApplicationKey(SSAEnums.ProductType type) {
        String applicationKey = "EMPTY_APPLICATION_KEY";

        switch (type) {
            case RewardedVideo:
                applicationKey = this.mSharedPreferences.getString("application_key_rv", null);
                break;

            case OfferWall:
                applicationKey = this.mSharedPreferences.getString("application_key_ow", null);
                break;

            case Interstitial:
                applicationKey = this.mSharedPreferences.getString("application_key_is", null);
                break;
        }


        return applicationKey;
    }


    public void setApplicationKey(String value, SSAEnums.ProductType type) {
        Editor editor = this.mSharedPreferences.edit();

        switch (type) {
            case RewardedVideo:
                editor.putString("application_key_rv", value);
                break;

            case OfferWall:
                editor.putString("application_key_ow", value);
                break;

            case Interstitial:
                editor.putString("application_key_is", value);
                break;
        }


        editor.commit();
    }


    public String getUniqueId(String type) {
        String userUniqueId = "EMPTY_UNIQUE_ID";

        if (type.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
            userUniqueId = this.mSharedPreferences.getString("unique_id_rv", null);
        } else if (type.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {
            userUniqueId = this.mSharedPreferences.getString("unique_id_ow", null);
        } else if (type.equalsIgnoreCase(SSAEnums.ProductType.Interstitial.toString())) {
            userUniqueId = this.mSharedPreferences.getString("unique_id_is", null);
        }

        return userUniqueId;
    }


    public boolean setUniqueId(String value, String type) {
        Editor editor = this.mSharedPreferences.edit();

        if (type.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
            editor.putString("unique_id_rv", value);
        } else if (type.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {
            editor.putString("unique_id_ow", value);
        } else if (type.equalsIgnoreCase(SSAEnums.ProductType.Interstitial.toString())) {
            editor.putString("unique_id_is", value);
        }

        return editor.commit();
    }


    public String getCurrentSDKVersion() {
        return this.mSharedPreferences.getString("version", "UN_VERSIONED");
    }


    public void setCurrentSDKVersion(String sdkVersion) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString("version", sdkVersion);
        editor.commit();
    }


    public String getSDKDownloadUrl() {
        return this.mSharedPreferences.getString("ssa_sdk_download_url", null);
    }


    public String getCampaignLastUpdate(String campaign) {
        return this.mSharedPreferences.getString(campaign, null);
    }


    public void setCampaignLastUpdate(String campaign, String lastUpdate) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putString(campaign, lastUpdate);
        editor.commit();
    }


    public void setUserID(String value, SSAEnums.ProductType type) {
        Editor editor = this.mSharedPreferences.edit();

        switch (type) {
            case RewardedVideo:
                editor.putString("user_id_rv", value);
                break;

            case OfferWall:
                editor.putString("user_id_ow", value);
                break;

            case Interstitial:
                editor.putString("user_id_is", value);
                break;
        }


        editor.commit();
    }


    public String getUniqueId(SSAEnums.ProductType type) {
        String userUniqueId = "EMPTY_UNIQUE_ID";

        switch (type) {
            case RewardedVideo:
                userUniqueId = this.mSharedPreferences.getString("unique_id_rv", null);
                break;

            case OfferWall:
                userUniqueId = this.mSharedPreferences.getString("unique_id_ow", null);
                break;

            case Interstitial:
                userUniqueId = this.mSharedPreferences.getString("unique_id_is", null);
                break;
        }


        return userUniqueId;
    }


    public boolean setLatestCompeltionsTime(String timestamp, String applicationKey, String userId) {
        String value = this.mSharedPreferences.getString("ssaUserData", null);

        if (!TextUtils.isEmpty(value)) {
            try {
                JSONObject ssaUserDataJson = new JSONObject(value);

                if (!ssaUserDataJson.isNull(applicationKey)) {
                    JSONObject applicationKeyJson = ssaUserDataJson.getJSONObject(applicationKey);

                    if (!applicationKeyJson.isNull(userId)) {
                        JSONObject userIdJson = applicationKeyJson.getJSONObject(userId);

                        userIdJson.put("timestamp", timestamp);

                        Editor editor = this.mSharedPreferences.edit();
                        editor.putString("ssaUserData", ssaUserDataJson.toString());
                        return editor.commit();
                    }
                }
            } catch (JSONException e) {
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
            }
        }

        return false;
    }


    public void setReportAppStarted(boolean value) {
        Editor editor = this.mSharedPreferences.edit();

        editor.putBoolean("is_reported", value);
        editor.apply();
    }


    public boolean getReportAppStarted() {
        return this.mSharedPreferences.getBoolean("is_reported", false);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/IronSourceSharedPrefHelper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */