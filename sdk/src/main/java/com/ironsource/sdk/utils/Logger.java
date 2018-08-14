package com.ironsource.sdk.utils;

import android.text.TextUtils;
import android.util.Log;

import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.DebugMode;


public class Logger {
    private static boolean enableLogging;

    public static void enableLogging(int mode) {
        if (SSAEnums.DebugMode.MODE_0.getValue() == mode) {
            enableLogging = false;
        } else {
            enableLogging = true;
        }
    }


    public static void i(String tag, String message) {
        if (enableLogging) {
            Log.i(tag, message);
        }
    }


    public static void i(String tag, String message, Throwable tr) {
        if ((enableLogging) && (!TextUtils.isEmpty(message))) {
            Log.i(tag, message, tr);
        }
    }


    public static void e(String tag, String message) {
        if (enableLogging) {
            Log.e(tag, message);
        }
    }


    public static void e(String tag, String message, Throwable tr) {
        if (enableLogging) {
            Log.e(tag, message, tr);
        }
    }


    public static void w(String tag, String message) {
        if (enableLogging) {
            Log.w(tag, message);
        }
    }


    public static void w(String tag, String message, Throwable tr) {
        if (enableLogging) {
            Log.w(tag, message, tr);
        }
    }


    public static void d(String tag, String message) {
        if (enableLogging) {
            Log.d(tag, message);
        }
    }


    public static void d(String tag, String message, Throwable tr) {
        if (enableLogging) {
            Log.d(tag, message, tr);
        }
    }


    public static void v(String tag, String message) {
        if (enableLogging) {
            Log.v(tag, message);
        }
    }


    public static void v(String tag, String message, Throwable tr) {
        if (enableLogging) {
            Log.v(tag, message, tr);
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/Logger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */