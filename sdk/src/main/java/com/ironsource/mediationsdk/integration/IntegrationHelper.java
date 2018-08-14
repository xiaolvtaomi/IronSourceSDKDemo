package com.ironsource.mediationsdk.integration;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.ironsource.mediationsdk.IntegrationData;
import com.ironsource.mediationsdk.IronSourceObject;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;


public class IntegrationHelper {
    private static final String TAG = "IntegrationHelper";
    private static final String SDK_COMPATIBILITY_VERSION = "4.1";
    private static final String BANNER_COMPATIBILITY_VERSION = "4.2";

    public static void validateIntegration(Activity activity) {
        Log.i("IntegrationHelper", "Verifying Integration:");
        validatePermissions(activity);

        String[] adapters = {"SupersonicAds", "AdColony", "AppLovin", "Chartboost", "HyprMX", "UnityAds", "Vungle", "InMobi", "Facebook", "Fyber", "MediaBrix", "Tapjoy", "AdMob", "MoPub", "Maio"};
        for (String adapter : adapters) {
            if (isAdapterValid(activity, adapter)) {
                if (adapter.equalsIgnoreCase("SupersonicAds")) {
                    Log.i("IntegrationHelper", ">>>> IronSource - VERIFIED");
                } else {
                    Log.i("IntegrationHelper", ">>>> " + adapter + " - VERIFIED");
                }
            } else if (adapter.equalsIgnoreCase("SupersonicAds")) {
                Log.e("IntegrationHelper", ">>>> IronSource - NOT VERIFIED");
            } else {
                Log.e("IntegrationHelper", ">>>> " + adapter + " - NOT VERIFIED");
            }
        }


        validateGooglePlayServices(activity);
    }

    private static boolean isAdapterValid(Activity activity, String adapterName) {
        try {
            if (adapterName.equalsIgnoreCase("SupersonicAds")) {
                Log.i("IntegrationHelper", "--------------- IronSource  --------------");
            } else {
                Log.i("IntegrationHelper", "--------------- " + adapterName + " --------------");
            }

            String className = "com.ironsource.adapters." + adapterName.toLowerCase() + "." + adapterName + "Adapter";

            IntegrationData data = getIntegrationData(activity, className);
            if (data == null) {
                return false;
            }

            if ((!adapterName.equalsIgnoreCase("SupersonicAds")) && (!isAdapterVersionValid(data))) {
                return false;
            }

            verifyBannerAdapterVersion(data);

            boolean ret = true;
            if (!isAdapterSdkValid(data.sdkName)) {
                ret = false;
            }

            if (!isActivitiesValid(activity, data.activities)) {
                ret = false;
            }

            if (!isExternalLibsValid(data.externalLibs)) {
                ret = false;
            }

            if (!isBroadcastReceiversValid(activity, data.broadcastReceivers)) {
                ret = false;
            }

            if (!isServicesValid(activity, data.services)) {
                ret = false;
            }

            if ((data.validateWriteExternalStorage) && (Build.VERSION.SDK_INT <= 18)) {
                PackageManager pm = activity.getPackageManager();
                if (pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", activity.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("IntegrationHelper", "android.permission.WRITE_EXTERNAL_STORAGE - VERIFIED");
                } else
                    Log.e("IntegrationHelper", "android.permission.WRITE_EXTERNAL_STORAGE - MISSING");
            }
            return false;


        } catch (Exception e) {

            Log.e("IntegrationHelper", "isAdapterValid " + adapterName, e);
        }
        return false;
    }

    private static boolean isBroadcastReceiversValid(Activity activity, String[] broadcastReceivers) {
        if (broadcastReceivers == null) {
            return true;
        }

        PackageManager packageManager = activity.getPackageManager();
        Log.i("IntegrationHelper", "*** Broadcast Receivers ***");

        boolean ret = true;
        for (String broadcastReceiver : broadcastReceivers) {
            try {
                Class localClass = Class.forName(broadcastReceiver);
                Intent intent = new Intent(activity, localClass);

                List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);

                if (list.size() > 0) {
                    Log.i("IntegrationHelper", broadcastReceiver + " - VERIFIED");
                } else {
                    ret = false;
                    Log.e("IntegrationHelper", broadcastReceiver + " - MISSING");
                }
            } catch (ClassNotFoundException e) {
                ret = false;
                Log.e("IntegrationHelper", broadcastReceiver + " - MISSING");
            }
        }

        return ret;
    }

    private static boolean isServicesValid(Activity activity, String[] services) {
        if (services == null) {
            return true;
        }

        PackageManager packageManager = activity.getPackageManager();
        Log.i("IntegrationHelper", "*** Services ***");

        boolean ret = true;
        for (String service : services) {
            try {
                Class localClass = Class.forName(service);
                Intent intent = new Intent(activity, localClass);

                List<ResolveInfo> list = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);

                if (list.size() > 0) {
                    Log.i("IntegrationHelper", service + " - VERIFIED");
                } else {
                    ret = false;
                    Log.e("IntegrationHelper", service + " - MISSING");
                }
            } catch (ClassNotFoundException e) {
                ret = false;
                Log.e("IntegrationHelper", service + " - MISSING");
            }
        }

        return ret;
    }

    private static boolean isExternalLibsValid(ArrayList<Pair<String, String>> externalLibs) {
        if (externalLibs == null) {
            return true;
        }

        Log.i("IntegrationHelper", "*** External Libraries ***");

        boolean ret = true;
        for (Pair<String, String> externalLibrary : externalLibs) {
            try {
                Class c = Class.forName((String) externalLibrary.first);
                Log.i("IntegrationHelper", (String) externalLibrary.second + " - VERIFIED");
            } catch (ClassNotFoundException e) {
                ret = false;
                Log.e("IntegrationHelper", (String) externalLibrary.second + " - MISSING");
            }
        }
        return ret;
    }

    private static boolean isActivitiesValid(Activity activity, String[] activities) {
        if (activities == null) {
            return true;
        }

        Log.i("IntegrationHelper", "*** Activities ***");

        boolean ret = true;
        for (String act : activities) {
            try {
                Class localClass = Class.forName(act);
                Intent intent = new Intent(activity, localClass);

                List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                if (list.size() > 0) {
                    Log.i("IntegrationHelper", act + " - VERIFIED");
                } else {
                    ret = false;
                    Log.e("IntegrationHelper", act + " - MISSING");
                }
            } catch (ClassNotFoundException e) {
                ret = false;
                Log.e("IntegrationHelper", act + " - MISSING");
            }
        }
        return ret;
    }

    private static void validatePermissions(Activity activity) {
        Log.i("IntegrationHelper", "*** Permissions ***");

        PackageManager pm = activity.getPackageManager();
        if (pm.checkPermission("android.permission.INTERNET", activity.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            Log.i("IntegrationHelper", "android.permission.INTERNET - VERIFIED");
        } else {
            Log.e("IntegrationHelper", "android.permission.INTERNET - MISSING");
        }

        if (pm.checkPermission("android.permission.ACCESS_NETWORK_STATE", activity.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
            Log.i("IntegrationHelper", "android.permission.ACCESS_NETWORK_STATE - VERIFIED");
        } else {
            Log.e("IntegrationHelper", "android.permission.ACCESS_NETWORK_STATE - MISSING");
        }
    }

    private static boolean isAdapterSdkValid(String sdkName) {
        if (sdkName == null) {
            return true;
        }
        try {
            Class c = Class.forName(sdkName);
            Log.i("IntegrationHelper", "SDK - VERIFIED");
            return true;
        } catch (Exception e) {
            Log.e("IntegrationHelper", "SDK - MISSING");
        }
        return false;
    }

    private static void verifyBannerAdapterVersion(IntegrationData data) {
        if ((!data.name.equalsIgnoreCase("AppLovin")) && (!data.name.equalsIgnoreCase("AdMob")) && (!data.name.equalsIgnoreCase("Facebook")) &&
                (!data.name.equalsIgnoreCase("InMobi")) && (!data.name.equalsIgnoreCase("Fyber"))) {
            return;
        }

        if (!data.version.startsWith("4.2")) {
            Log.e("IntegrationHelper", data.name + " adapter " + data.version + " is incompatible for showing banners with SDK version " + IronSourceUtils.getSDKVersion() + ", please update your adapter to version " + "4.2" + ".*");
        }
    }

    private static boolean isAdapterVersionValid(IntegrationData data) {
        if ((data.version.startsWith("4.1")) || (data.version.startsWith("4.2"))) {
            Log.i("IntegrationHelper", "Adapter version - VERIFIED");
            return true;
        }
        Log.e("IntegrationHelper", data.name + " adapter " + data.version + " is incompatible with SDK version " + IronSourceUtils.getSDKVersion() + ", please update your adapter to version " + "4.1" + ".*");

        return false;
    }

    private static IntegrationData getIntegrationData(Activity activity, String className) {
        try {
            Class c = Class.forName(className);
            Method method = c.getMethod("getIntegrationData", new Class[]{Activity.class});
            IntegrationData ret = (IntegrationData) method.invoke(null, new Object[]{activity});
            Log.i("IntegrationHelper", "Adapter - VERIFIED");
            return ret;
        } catch (ClassNotFoundException cnfe) {
            Log.e("IntegrationHelper", "Adapter - MISSING");
        } catch (Exception e) {
            Log.e("IntegrationHelper", "Adapter version - NOT VERIFIED");
        }
        return null;
    }

    private static void validateGooglePlayServices(final Activity activity) {
        final String mGooglePlayServicesMetaData = "com.google.android.gms.version";
        final String mGooglePlayServices = "Google Play Services";

        Thread thread = new Thread() {
            public void run() {
                try {
                    Log.w("IntegrationHelper", "--------------- Google Play Services --------------");

                    PackageManager packageManager = activity.getPackageManager();

                    ApplicationInfo ai = packageManager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
                    Bundle bundle = ai.metaData;

                    boolean exists = bundle.containsKey(mGooglePlayServicesMetaData);

                    if (exists) {
                        Log.i("IntegrationHelper", "Google Play Services - VERIFIED");
                        String gaid = IronSourceObject.getInstance().getAdvertiserId(activity);
                        if (!TextUtils.isEmpty(gaid))
                            Log.i("IntegrationHelper", "GAID is: " + gaid + " (use this for test devices)");
                    } else {
                        Log.e("IntegrationHelper", "Google Play Services - MISSING");
                    }
                } catch (Exception e) {
                    Log.e("IntegrationHelper", "Google Play Services - MISSING");
                }
            }
        };
        thread.start();
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/integration/IntegrationHelper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */