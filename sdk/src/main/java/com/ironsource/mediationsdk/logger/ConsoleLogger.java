package com.ironsource.mediationsdk.logger;

import android.util.Log;


public class ConsoleLogger
        extends IronSourceLogger {
    public static final String NAME = "console";

    private ConsoleLogger() {
        super("console");
    }

    public ConsoleLogger(int debugLevel) {
        super("console", debugLevel);
    }


    public void log(IronSourceLogger.IronSourceTag tag, String message, int logLevel) {
        switch (logLevel) {
            case 0:
                Log.v("" + tag, message);
                break;
            case 1:
                Log.i("" + tag, message);
                break;
            case 2:
                Log.w("" + tag, message);
                break;
            case 3:
                Log.e("" + tag, message);
        }

    }

    public void logException(IronSourceLogger.IronSourceTag tag, String message, Throwable e) {
        log(tag, message + ":stacktrace[" + Log.getStackTraceString(e) + "]", 3);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/ConsoleLogger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */