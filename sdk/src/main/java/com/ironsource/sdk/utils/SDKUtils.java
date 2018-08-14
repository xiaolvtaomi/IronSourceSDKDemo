package com.ironsource.sdk.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.ironsource.environment.DeviceStatus;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ProductType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SDKUtils {
    private static final String TAG = SDKUtils.class.getSimpleName();

    private static String mAdvertisingId;
    private static boolean mIsLimitedTrackingEnabled = true;

    private static String mControllerUrl;
    private static int mDebugMode = 0;
    private static String mControllerConfig;
    private static String mUserGroup = "";


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static String getFileName(String url) {
        String[] assetSplit = url.split(File.separator);
        String fileName = assetSplit[(assetSplit.length - 1)];

        String[] fileNameArr = fileName.split("\\?");

        String name = fileNameArr[0];

        String encodedlFileName = null;
        try {
            encodedlFileName = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodedlFileName;
    }

    public static int pxToDp(long px) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int dp = (int) ((float) px / displayMetrics.density + 0.5F);
        return dp;
    }

    public static int dpToPx(long dp) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int px = (int) ((float) dp * displayMetrics.density + 0.5F);
        return px;
    }

    public static int convertPxToDp(int pixels) {
        int dp = (int) TypedValue.applyDimension(1, pixels,


                Resources.getSystem().getDisplayMetrics());

        return dp;
    }

    public static int convertDpToPx(int dp) {
        int pixels = (int) TypedValue.applyDimension(0, dp,


                Resources.getSystem().getDisplayMetrics());

        return pixels;
    }

    public static String translateRequestedOrientation(int orientation) {
        String result = "none";
        switch (orientation) {
            case 0:
            case 6:
            case 8:
            case 11:
                result = "landscape";
                break;
            case 1:
            case 7:
            case 9:
            case 12:
                result = "portrait";
                break;
        }


        return result;
    }


    public static String translateOrientation(int orientation) {
        String strOrientation = "none";
        switch (orientation) {
            case 2:
                strOrientation = "landscape";
                break;
            case 1:
                strOrientation = "portrait";
        }

        return strOrientation;
    }

    public static JSONObject getOrientation(Context context) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("orientation", translateOrientation(DeviceStatus.getDeviceOrientation(context)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static String createErrorMessage(String action, String stage) {
        String message = String.format("%s Failure occurred during initiation at: %s", new Object[]{action, stage});

        return message;
    }

    public static Long getCurrentTimeMillis() {
        return Long.valueOf(System.currentTimeMillis());
    }

    public static boolean isApplicationVisible(Context context) {
        String packageName = context.getPackageName();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        for (RunningAppProcessInfo appProcess : appProcesses) {
            if ((appProcess.processName.equalsIgnoreCase(packageName)) && (appProcess.importance == 100)) {
                return true;
            }
        }
        return false;
    }


    public static void showNoInternetDialog(Context context) {
        new AlertDialog.Builder(context).setMessage("No Internet Connection").setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public static byte[] encrypt(String x) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");

            digest.reset();

            digest.update(x.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (digest != null) {
            return digest.digest();
        }
        return null;
    }


    public static String convertStreamToString(InputStream is, boolean isGzipEnabled, String dir, String fileName)
            throws IOException {
        InputStream cleanedIs = is;

        if (isGzipEnabled) {
            cleanedIs = new GZIPInputStream(is);
        }

        BufferedReader reader = null;


        File outputFile = new File(dir, fileName);
        Writer writer = new BufferedWriter(new FileWriter(outputFile));
        try {
            reader = new BufferedReader(new InputStreamReader(cleanedIs, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            writer.write(sb.toString());

            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }

            cleanedIs.close();

            if (isGzipEnabled) {
                is.close();
            }

            writer.close();
        }
    }


    public static String encodeString(String value) {
        String encodedString = "";
        try {
            encodedString = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
        }

        return encodedString;
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
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadGoogleAdvertiserInfo(Context context) {
        Exception exception = null;
        try {
            String[] deviceInfo = DeviceStatus.getAdvertisingIdInfo(context);
            mAdvertisingId = deviceInfo[0];
            mIsLimitedTrackingEnabled = Boolean.valueOf(deviceInfo[1]).booleanValue();
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception != null) {
                if (exception.getMessage() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getMessage());
                }
                if (exception.getCause() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getCause());
                }
            }
        }
    }


    public static String getAdvertiserId() {
        return mAdvertisingId;
    }


    public static boolean isLimitAdTrackingEnabled() {
        return mIsLimitedTrackingEnabled;
    }

    public static Object getIInAppBillingServiceClass(IBinder binder) {
        Exception exception = null;


        try {
            Class<?> mStubClass = Class.forName("com.android.vending.billing.IInAppBillingService$Stub");


            Method asInterfaceMethod = mStubClass.getMethod("asInterface", new Class[]{IBinder.class});
            Object object = asInterfaceMethod.invoke(null, new Object[]{binder});

            return object;
        } catch (ClassNotFoundException e) {
            exception = e;
        } catch (NoSuchMethodException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        } finally {
            if (exception != null) {
                if (exception.getMessage() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getMessage());
                }
                if (exception.getCause() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getCause());
                }
            }
        }

        return null;
    }

    public static String queryingPurchasedItems(Object object, String pckName) {
        JSONArray purchases = new JSONArray();

        Exception exception = null;


        try {
            Method getPurchasesMethod = object.getClass().getMethod("getPurchases", new Class[]{Integer.TYPE, String.class, String.class, String.class});
            Object mBundleClass = getPurchasesMethod.invoke(object, new Object[]{Integer.valueOf(3), pckName, "inapp", null});

            Method mGetIntmethod = mBundleClass.getClass().getMethod("getInt", new Class[]{String.class});
            Method mGetStringArrayListMethod = mBundleClass.getClass().getMethod("getStringArrayList", new Class[]{String.class});
            Method mGetStringMethod = mBundleClass.getClass().getMethod("getString", new Class[]{String.class});

            int response = -1;
            response = ((Integer) mGetIntmethod.invoke(mBundleClass, new Object[]{"RESPONSE_CODE"})).intValue();

            if (response == 0) {

                ArrayList<String> ownedSkus = (ArrayList) mGetStringArrayListMethod.invoke(mBundleClass, new Object[]{"INAPP_PURCHASE_ITEM_LIST"});


                ArrayList<String> purchaseDataList = (ArrayList) mGetStringArrayListMethod.invoke(mBundleClass, new Object[]{"INAPP_PURCHASE_DATA_LIST"});


                ArrayList<String> signatureList = (ArrayList) mGetStringArrayListMethod.invoke(mBundleClass, new Object[]{"INAPP_DATA_SIGNATURE_LIST"});


                String continuationToken = (String) mGetStringMethod.invoke(mBundleClass, new Object[]{"INAPP_CONTINUATION_TOKEN"});

                for (int i = 0; i < purchaseDataList.size(); i++) {
                    String purchaseData = (String) purchaseDataList.get(i);
                    String signature = (String) signatureList.get(i);
                    String sku = (String) ownedSkus.get(i);

                    Logger.i(TAG, purchaseData);
                    Logger.i(TAG, signature);
                    Logger.i(TAG, sku);

                    JSONObject item = new JSONObject();
                    try {
                        item.put("purchaseData", purchaseData);
                        item.put("signature", purchaseData);
                        item.put("sku", purchaseData);

                        purchases.put(item);


                    } catch (JSONException localJSONException) {
                    }

                }

            }


        } catch (NoSuchMethodException e) {

            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        } catch (IllegalArgumentException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        } finally {
            if (exception != null) {
                if (exception.getMessage() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getMessage());
                }
                if (exception.getCause() != null) {
                    Logger.i(TAG, exception.getClass().getSimpleName() + ": " + exception.getCause());
                }
            }
        }

        return purchases.toString();
    }

    public static String getControllerUrl() {
        if (!TextUtils.isEmpty(mControllerUrl)) {
            return mControllerUrl;
        }
        return "";
    }


    public static String getSDKVersion() {
        return "5.53";
    }

    public static void setControllerUrl(String url) {
        mControllerUrl = url;
    }

    public static String getControllerConfig() {
        return mControllerConfig;
    }

    public static void setControllerConfig(String config) {
        mControllerConfig = config;
    }

    public static int getDebugMode() {
        return mDebugMode;
    }

    public static void setDebugMode(int debugMode) {
        mDebugMode = debugMode;
    }

    public static String getValueFromJsonObject(String json, String key) {
        String placementId = "";
        try {
            JSONObject jsonObj = new JSONObject(json);
            placementId = jsonObj.getString(key);
        } catch (Exception e) {
        }
        return placementId;
    }


    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return ("mounted".equals(state)) || ("mounted_ro".equals(state));
    }


    public static int getActivityUIFlags(boolean isImmersive) {
        int uiFlags = 0;
        if (Build.VERSION.SDK_INT >= 14) {
            uiFlags = 2;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            uiFlags |= 0x704;
        }
        if ((Build.VERSION.SDK_INT >= 19) && (isImmersive)) {
            uiFlags |= 0x1000;
        }

        return uiFlags;
    }


    private static int generateViewIdForOldOS() {
        for (; ; ) {
            int result = sNextGeneratedId.get();

            int newValue = result + 1;
            if (newValue > 16777215) newValue = 1;
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    public static int generateViewId() {
        if (Build.VERSION.SDK_INT < 17) {
            return generateViewIdForOldOS();
        }
        return View.generateViewId();
    }

    public static JSONObject getControllerConfigAsJSONObject() {
        String controllerConfig = getControllerConfig();
        try {
            return new JSONObject(controllerConfig);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static SSAEnums.ProductType getProductType(String product) {
        SSAEnums.ProductType productType = null;
        if (product.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
            productType = SSAEnums.ProductType.RewardedVideo;
        } else if (product.equalsIgnoreCase(SSAEnums.ProductType.Interstitial.toString())) {
            productType = SSAEnums.ProductType.Interstitial;
        } else if (product.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {
            productType = SSAEnums.ProductType.OfferWall;
        }
        return productType;
    }

    public static void setTesterParameters(String testerParameters) {
        mUserGroup = testerParameters;
    }

    public static String getTesterParameters() {
        return mUserGroup;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/SDKUtils.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */