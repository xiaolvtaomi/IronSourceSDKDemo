package com.ironsource.environment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import org.json.JSONObject;


public class DeviceStatus {
    private static final String DEVICE_OS = "android";
    private static final String GOOGLE_PLAY_SERVICES_CLASS_NAME = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    private static final String GOOGLE_PLAY_SERVICES_GET_AID_INFO_METHOD_NAME = "getAdvertisingIdInfo";
    private static final String GOOGLE_PLAY_SERVICES_GET_AID_METHOD_NAME = "getId";
    private static final String GOOGLE_PLAY_SERVICES_IS_LIMITED_AD_TRACKING_METHOD_NAME = "isLimitAdTrackingEnabled";
    private static String uniqueId = null;

    private static final String MEDIATION_SHARED_PREFS = "Mediation_Shared_Preferences";

    public static final String UUID_ENABLED = "uuidEnabled";
    private static final String CACHED_UUID_KEY = "cachedUUID";

    public static long getDeviceLocalTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        Date currentLocalTime = calendar.getTime();

        return currentLocalTime.getTime();
    }


    public static int getDeviceTimeZoneOffsetInMinutes() {
        return -(TimeZone.getDefault().getOffset(getDeviceLocalTime()) / 60000);
    }


    public static String[] getAdvertisingIdInfo(Context c)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> mAdvertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");

        Method getAdvertisingIdInfoMethod = mAdvertisingIdClientClass.getMethod("getAdvertisingIdInfo", new Class[]{Context.class});
        Object mInfoClass = getAdvertisingIdInfoMethod.invoke(mAdvertisingIdClientClass, new Object[]{c});

        Method getIdMethod = mInfoClass.getClass().getMethod("getId", new Class[0]);
        Method isLimitAdTrackingEnabledMethod = mInfoClass.getClass().getMethod("isLimitAdTrackingEnabled", new Class[0]);

        String advertisingId = getIdMethod.invoke(mInfoClass, new Object[0]).toString();
        boolean isLimitedTrackingEnabled = ((Boolean) isLimitAdTrackingEnabledMethod.invoke(mInfoClass, new Object[0])).booleanValue();

        return new String[]{advertisingId, "" + isLimitedTrackingEnabled};
    }


    public static String getDeviceLanguage(Context c)
            throws Exception {
        return c.getResources().getConfiguration().locale.getLanguage();
    }


    private static long getFreeStorageInBytes(File f) {
        long SIZE_KB = 1024L;
        long SIZE_MB = SIZE_KB*1024;

        StatFs stat = new StatFs(f.getPath());
        long res;
        if (VERSION.SDK_INT < 19) {
            res = stat.getAvailableBlocks() * stat.getBlockSize();
        } else {
            res = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        }

        return res / SIZE_MB;
    }


    public static boolean isExternalMemoryAvailableWritable() {
        return ("mounted".equals(Environment.getExternalStorageState())) && (Environment.isExternalStorageRemovable());
    }


    public static String getMobileCarrier(Context c) {
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperatorName();
    }


    public static String getAndroidOsVersion() {
        return VERSION.RELEASE;
    }


    public static int getAndroidAPIVersion() {
        return VERSION.SDK_INT;
    }


    public static String getDeviceModel() {
        return Build.MODEL;
    }


    public static String getDeviceOEM() {
        return Build.MANUFACTURER;
    }


    public static String getDeviceOs() {
        return "android";
    }


    public static boolean isRootedDevice() {
        return findBinary("su");
    }

    private static boolean findBinary(String binaryName) {
        boolean found = false;
        try {
            String[] paths = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};

            for (String path : paths) {
                File file = new File(path + binaryName);
                if (file.exists()) {
                    found = true;
                    break;
                }
            }
        } catch (Exception localException) {
        }

        return found;
    }


    public static boolean isRTL(Context context) {
        Configuration config = context.getResources().getConfiguration();
        if ((VERSION.SDK_INT >= 17) &&
                (config.getLayoutDirection() == 1)) {
            return true;
        }

        return false;
    }


    public static int getApplicationRotation(Context context) {
        Display defaultDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        return defaultDisplay.getRotation();
    }


    public static float getSystemVolumePercent(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audio.getStreamVolume(3) / audio.getStreamMaxVolume(3);
    }


    public static int getDeviceWidth() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }


    public static int getDeviceHeight() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }


    public static int getActivityRequestedOrientation(Context activity) {
        return (activity instanceof Activity) ? ((Activity) activity).getRequestedOrientation() : -1;
    }


    public static int getDeviceDefaultOrientation(Context context) {
        int rotation = getApplicationRotation(context);
        int orientation = getDeviceOrientation(context);

        if (((rotation != 0) && (rotation != 2)) || ((orientation == 2) || (((rotation == 1) || (rotation == 3)) && (orientation == 1)))) {


            return 2;
        }
        return 1;
    }


    public static int getDeviceOrientation(Context context) {
        return context.getResources().getConfiguration().orientation;
    }


    public static float getDeviceDensity() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return displayMetrics.density;
    }


    public static List<ApplicationInfo> getInstalledApplications(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.getInstalledApplications(0);
    }


    public static boolean isDeviceOrientationLocked(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "accelerometer_rotation", 0) != 1;
    }


    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }


    public static String getInternalCacheDirPath(Context context) {
        String path = null;
        File internalFile = context.getCacheDir();
        if (internalFile != null) {
            path = internalFile.getPath();
        }

        return path;
    }


    public static long getAvailableInternalMemorySizeInMegaBytes() {
        File path = Environment.getDataDirectory();
        long res = getFreeStorageInBytes(path);
        return res;
    }


    public static long getAvailableMemorySizeInMegaBytes(String path) {
        return getFreeStorageInBytes(new File(path));
    }


    public static long getAvailableExternalMemorySizeInMegaBytes() {
        long res = 0L;
        if (isExternalMemoryAvailableWritable()) {
            File path = Environment.getExternalStorageDirectory();
            res = getFreeStorageInBytes(path);
        }
        return res;
    }


    @TargetApi(19)
    public static boolean isImmersiveSupported(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        return ((uiOptions | 0x1000) == uiOptions) || ((uiOptions | 0x800) == uiOptions);
    }


    public static int getBatteryLevel(Context context) {
        int batteryLevel = -1;
        try {
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            int level = batteryIntent != null ? batteryIntent.getIntExtra("level", -1) : 0;
            int scale = batteryIntent != null ? batteryIntent.getIntExtra("scale", -1) : 0;


            if ((level != -1) && (scale != -1)) {
                batteryLevel = (int) (level / scale * 100.0F);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return batteryLevel;
    }


    public static synchronized String getOrGenerateOnceUniqueIdentifier(Context context) {
        if (!TextUtils.isEmpty(uniqueId)) {
            return uniqueId;
        }
        try {
            SharedPreferences preferences = context.getSharedPreferences("Mediation_Shared_Preferences", 0);
            boolean isEnabled = preferences.getBoolean("uuidEnabled", true);
            if (isEnabled) {
                String id = preferences.getString("cachedUUID", "");
                if (TextUtils.isEmpty(id)) {
                    uniqueId = UUID.randomUUID().toString();
                    Editor editor = preferences.edit();
                    editor.putString("cachedUUID", uniqueId);
                    editor.apply();
                } else {
                    uniqueId = id;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueId;
    }

    private static boolean isSystemPackage(ResolveInfo resolveInfo) {
        return (resolveInfo.activityInfo.applicationInfo.flags & 0x1) != 0;
    }

    public static JSONObject getAppsInstallTime(Context ctx, boolean includeSystemPackages) {
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");

        List<ResolveInfo> pkgAppsList = ctx.getPackageManager().queryIntentActivities(intent, 0);
        JSONObject packagesInstalledPerDate = new JSONObject();

        PackageManager manager = ctx.getPackageManager();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (int i = 0; i < pkgAppsList.size(); i++) {
            ResolveInfo resolveInfo = (ResolveInfo) pkgAppsList.get(i);
            try {
                if ((includeSystemPackages) || (!isSystemPackage(resolveInfo))) {


                    PackageInfo packageInfo = manager.getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);

                    String installTime = sdf.format(new Date(packageInfo.firstInstallTime));
                    int numberOfInstalledApps = packagesInstalledPerDate.optInt(installTime, 0);
                    numberOfInstalledApps++;
                    packagesInstalledPerDate.put(installTime, numberOfInstalledApps);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return packagesInstalledPerDate;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/environment/DeviceStatus.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */