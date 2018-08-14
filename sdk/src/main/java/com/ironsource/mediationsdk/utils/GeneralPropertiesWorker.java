package com.ironsource.mediationsdk.utils;

import android.annotation.SuppressLint;
import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStats;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ironsource.environment.ApplicationContext;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
import com.ironsource.mediationsdk.sdk.GeneralProperties;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class GeneralPropertiesWorker
        implements Runnable {
    private final String TAG = getClass().getSimpleName();

    private final String BUNDLE_ID = "bundleId";
    private final String ADVERTISING_ID = "advertisingId";
    private final String ADVERTISING_ID_IS_LIMIT_TRACKING = "isLimitAdTrackingEnabled";
    private final String APPLICATION_KEY = "appKey";
    private final String DEVICE_OS = "deviceOS";
    private final String ANDROID_OS_VERSION = "osVersion";
    private final String CONNECTION_TYPE = "connectionType";
    public static final String SDK_VERSION = "sdkVersion";
    private final String LANGUAGE = "language";
    private final String DEVICE_OEM = "deviceOEM";
    private final String DEVICE_MODEL = "deviceModel";
    private final String MOBILE_CARRIER = "mobileCarrier";
    private final String EXTERNAL_FREE_MEMORY = "externalFreeMemory";
    private final String INTERNAL_FREE_MEMORY = "internalFreeMemory";
    private final String BATTERY_LEVEL = "battery";
    private final String LOCATION_LAT = "lat";
    private final String LOCATION_LON = "lon";
    private final String GMT_MINUTES_OFFSET = "gmtMinutesOffset";
    private final String PUBLISHER_APP_VERSION = "appVersion";
    private final String KEY_SESSION_ID = "sessionId";
    private final String KEY_PLUGIN_TYPE = "pluginType";
    private final String KEY_PLUGIN_VERSION = "pluginVersion";
    private final String KEY_PLUGIN_FW_VERSION = "plugin_fw_v";
    private final String KEY_IS_ROOT = "jb";
    private final String ADVERTISING_ID_TYPE = "advertisingIdType";
    private final String MEDIATION_TYPE = "mt";

    private static final int MINUTES_OFFSET_STEP = 15;

    private static final int MAX_MINUTES_OFFSET = 840;
    private static final int MIN_MINUTES_OFFSET = -720;
    private Context mContext;

    private GeneralPropertiesWorker() {
    }

    public GeneralPropertiesWorker(Context ctx) {
        this.mContext = ctx.getApplicationContext();
    }

    public void run() {
        try {
            Map<String, Object> params = collectInformation();
            GeneralProperties.getProperties().putKeys(params);


            IronSourceUtils.saveGeneralProperties(this.mContext, GeneralProperties.getProperties().toJSON());
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "Thread name = " + getClass().getSimpleName(), e);
        }
    }


    private Map<String, Object> collectInformation() {
        Map<String, Object> result = new HashMap();


        String strVal = IronSourceObject.getInstance().getSessionId();
        result.put("sessionId", strVal);

        strVal = getBundleId();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("bundleId", strVal);


            String publAppVersion = ApplicationContext.getPublisherApplicationVersion(this.mContext, strVal);
            if (!TextUtils.isEmpty(publAppVersion)) {
                result.put("appVersion", publAppVersion);
            }
        }
        result.put("appKey", getApplicationKey());


        String advertisingId = "";
        String advertisingIdType = "";
        boolean isLimitAdTrackingEnabled = false;


        try {
            String[] advertisingIdInfo = DeviceStatus.getAdvertisingIdInfo(this.mContext);
            if ((advertisingIdInfo != null) && (advertisingIdInfo.length == 2)) {
                if (!TextUtils.isEmpty(advertisingIdInfo[0])) {
                    advertisingId = advertisingIdInfo[0];
                }
                isLimitAdTrackingEnabled = Boolean.valueOf(advertisingIdInfo[1]).booleanValue();
            }
        } catch (Exception localException) {
        }


        if (!TextUtils.isEmpty(advertisingId)) {
            advertisingIdType = "GAID";
        } else {
            advertisingId = DeviceStatus.getOrGenerateOnceUniqueIdentifier(this.mContext);
            if (!TextUtils.isEmpty(advertisingId)) {
                advertisingIdType = "UUID";
            }
        }


        if (!TextUtils.isEmpty(advertisingId)) {
            result.put("advertisingId", advertisingId);
            result.put("advertisingIdType", advertisingIdType);
            result.put("isLimitAdTrackingEnabled", Boolean.valueOf(isLimitAdTrackingEnabled));
        }

        result.put("deviceOS", getDeviceOS());

        strVal = getAndroidVersion();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("osVersion", getAndroidVersion());
        }
        strVal = IronSourceUtils.getConnectionType(this.mContext);
        if (!TextUtils.isEmpty(strVal)) {
            result.put("connectionType", strVal);
        }
        result.put("sdkVersion", getSDKVersion());

        strVal = getLanguage();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("language", strVal);
        }
        strVal = getDeviceOEM();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("deviceOEM", strVal);
        }
        strVal = getDeviceModel();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("deviceModel", strVal);
        }
        strVal = getMobileCarrier();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("mobileCarrier", strVal);
        }
        long longVal = getInternalStorageFreeSize();
        result.put("internalFreeMemory", Long.valueOf(longVal));

        longVal = getExternalStorageFreeSize();
        result.put("externalFreeMemory", Long.valueOf(longVal));


        int intVal = getBatteryLevel();
        result.put("battery", Integer.valueOf(intVal));

        boolean allowLocation = IronSourceUtils.getBooleanFromSharedPrefs(this.mContext, "GeneralProperties.ALLOW_LOCATION_SHARED_PREFS_KEY", false);

        if (allowLocation) {
            double[] lastKnownLocation = getLastKnownLocation();
            if ((lastKnownLocation != null) && (lastKnownLocation.length == 2)) {
                result.put("lat", Double.valueOf(lastKnownLocation[0]));
                result.put("lon", Double.valueOf(lastKnownLocation[1]));
            }
        }

        int gmtMinutesOffset = getGmtMinutesOffset();
        if (validateGmtMinutesOffset(gmtMinutesOffset)) {
            result.put("gmtMinutesOffset", Integer.valueOf(gmtMinutesOffset));
        }

        strVal = getPluginType();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("pluginType", strVal);
        }
        strVal = getPluginVersion();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("pluginVersion", strVal);
        }
        strVal = getPluginFrameworkVersion();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("plugin_fw_v", strVal);
        }
        strVal = String.valueOf(DeviceStatus.isRootedDevice());
        if (!TextUtils.isEmpty(strVal)) {
            result.put("jb", strVal);
        }
        strVal = getMediationType();
        if (!TextUtils.isEmpty(strVal)) {
            result.put("mt", strVal);
        }
        return result;
    }

    private String getPluginType() {
        String result = "";
        try {
            result = ConfigFile.getConfigFile().getPluginType();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getPluginType()", e);
        }
        return result;
    }

    private String getPluginVersion() {
        String result = "";
        try {
            result = ConfigFile.getConfigFile().getPluginVersion();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getPluginVersion()", e);
        }
        return result;
    }

    private String getPluginFrameworkVersion() {
        String result = "";
        try {
            result = ConfigFile.getConfigFile().getPluginFrameworkVersion();
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "getPluginFrameworkVersion()", e);
        }
        return result;
    }


    private String getBundleId() {
        try {
            return this.mContext.getPackageName();
        } catch (Exception e) {
        }
        return "";
    }


    private String getApplicationKey() {
        return IronSourceObject.getInstance().getIronSourceAppKey();
    }


    private String getDeviceOS() {
        return "Android";
    }


    private String getAndroidVersion() {
        try {
            String release = VERSION.RELEASE;
            int sdkVersion = VERSION.SDK_INT;
            return "" + sdkVersion + "(" + release + ")";
        } catch (Exception e) {
        }
        return "";
    }


    private String getSDKVersion() {
        return IronSourceUtils.getSDKVersion();
    }


    private String getLanguage() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Exception e) {
        }
        return "";
    }


    private String getDeviceOEM() {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
        }
        return "";
    }


    private String getDeviceModel() {
        try {
            return Build.MODEL;
        } catch (Exception e) {
        }
        return "";
    }


    private String getMobileCarrier() {
        String ret = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String operatorName = telephonyManager.getNetworkOperatorName();
                if (!operatorName.equals(""))
                    ret = operatorName;
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":getMobileCarrier()", e);
        }

        return ret;
    }


    private boolean isExternalStorageAbvailable() {
        try {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
        }
        return false;
    }


    private long getInternalStorageFreeSize() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());


            long blockSize = stat.getBlockSize();

            long availableBlocks = stat.getAvailableBlocks();

            return availableBlocks * blockSize / 1048576L;
        } catch (Exception e) {
        }
        return -1L;
    }


    private long getExternalStorageFreeSize() {
        if (isExternalStorageAbvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());


            long blockSize = stat.getBlockSize();

            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize / 1048576L;
        }
        return -1L;
    }


    private int getBatteryLevel() {
        int result = -1;
        try {
            Intent batteryIntent = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            int level = batteryIntent != null ? batteryIntent.getIntExtra("level", -1) : 0;
            int scale = batteryIntent != null ? batteryIntent.getIntExtra("scale", -1) : 0;


            if ((level != -1) && (scale != -1)) {
                result = (int) (level / scale * 100.0F);
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":getBatteryLevel()", e);
        }

        return result;
    }


    @SuppressLint({"MissingPermission"})
    private double[] getLastKnownLocation() {
        double[] result = new double[0];
        long bestLocationTime = Long.MIN_VALUE;


        try {
            if (locationPermissionGranted()) {
                LocationManager locationManager = (LocationManager) this.mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


                Location bestLocation = null;

                List<String> providers = locationManager.getAllProviders();
                for (String provider : providers) {
                    Location location = locationManager.getLastKnownLocation(provider);

                    if (location != null) {
                        long currentTime = location.getTime();
                        if (currentTime > bestLocationTime) {
                            bestLocation = location;
                            bestLocationTime = bestLocation.getTime();
                        }
                    }
                }


                if (bestLocation != null) {
                    double lat = bestLocation.getLatitude();
                    double lon = bestLocation.getLongitude();

                    result = new double[]{lat, lon};
                }
            }
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":getLastLocation()", e);
            result = new double[0];
        }

        return result;
    }


    private boolean locationPermissionGranted() {
        try {
            String permission = "android.permission.ACCESS_FINE_LOCATION";
            int res = this.mContext.checkCallingOrSelfPermission(permission);
            return res == 0;
        } catch (Exception e) {
        }
        return false;
    }


    private int getGmtMinutesOffset() {
        int result = 0;
        try {
            TimeZone tz = TimeZone.getDefault();
            Calendar cal = GregorianCalendar.getInstance(tz);
            int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

            result = offsetInMillis / 1000 / 60;


            result = Math.round(result / 15) * 15;
        } catch (Exception e) {
            IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, this.TAG + ":getGmtMinutesOffset()", e);
        }

        return result;
    }

    private boolean validateGmtMinutesOffset(int offset) {
        boolean isValid = (offset <= 840) && (offset >= 64816) && (offset % 15 == 0);
        return isValid;
    }

    private String getMediationType() {
        return IronSourceObject.getInstance().getMediationType();
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/GeneralPropertiesWorker.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */