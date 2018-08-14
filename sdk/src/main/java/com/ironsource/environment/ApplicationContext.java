package com.ironsource.environment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


public class ApplicationContext {
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }


    public static int getAppOrientation(Activity a) {
        return a.getRequestedOrientation();
    }


    public static String getDiskCacheDirPath(Context context) {
        String path = null;
        File internalFile = context.getCacheDir();
        if (internalFile != null) {
            path = internalFile.getPath();
        }
        return path;
    }


    public static boolean isValidPermission(Context context, String permissionToCheck) {
        boolean isPermissionValid = false;
        String permission = "";

        if (!TextUtils.isEmpty(permissionToCheck)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),  PackageManager.GET_PERMISSIONS);
                if (packageInfo.requestedPermissions != null) {
                    for (int i = 0; (i < packageInfo.requestedPermissions.length) && (!isPermissionValid); i++) {
                        permission = packageInfo.requestedPermissions[i];
                        isPermissionValid = permissionToCheck.equals(permission);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return isPermissionValid;
    }


    public static boolean isPermissionGranted(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return res == 0;
    }


    public static JSONObject getPermissions(Context context, JSONArray permissionsFromController) {
        String GRANTED = "Granted";
        String REJECTED = "Rejected";
        String NOT_FOUND = "notFoundInManifest";

        JSONObject allPermissions = new JSONObject();
        if (Build.VERSION.SDK_INT >= 16) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),  PackageManager.GET_PERMISSIONS);


                if (permissionsFromController.length() == 0) {
                    for (int index = 0; index < packageInfo.requestedPermissions.length; index++) {
                        String permissionStatus = (packageInfo.requestedPermissionsFlags[index] & 0x2) != 0 ? GRANTED : REJECTED;

                        allPermissions.put(packageInfo.requestedPermissions[index], permissionStatus);
                    }
                } else {
                    List<String> permissionsList = Arrays.asList(packageInfo.requestedPermissions);
                    for (int index = 0; index < permissionsFromController.length(); index++) {
                        String permission = permissionsFromController.getString(index);
                        int indexOfPermission = permissionsList.indexOf(permission);
                        if (indexOfPermission != -1) {
                            String permissionStatus = (packageInfo.requestedPermissionsFlags[indexOfPermission] & 0x2) != 0 ? GRANTED : REJECTED;
                            allPermissions.put(permission, permissionStatus);
                        } else {
                            allPermissions.put(permission, NOT_FOUND);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allPermissions;
    }


    static PackageInfo getAppPackageInfo(Context context)
            throws NameNotFoundException {
        return context.getPackageManager().getPackageInfo(getPackageName(context), 0);
    }


    public static long getFirstInstallTime(Context context) {
        try {
            PackageInfo packageInfo = getAppPackageInfo(context);
            return packageInfo.firstInstallTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1L;
    }


    public static long getLastUpdateTime(Context context) {
        try {
            PackageInfo packageInfo = getAppPackageInfo(context);
            return packageInfo.lastUpdateTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1L;
    }


    public static String getApplicationVersionName(Context context) {
        try {
            PackageInfo packageInfo = getAppPackageInfo(context);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getPublisherApplicationVersion(Context context, String packageName) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception localException) {
        }

        return result;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/environment/ApplicationContext.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */