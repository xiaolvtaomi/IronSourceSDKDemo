package com.ironsource.mediationsdk.logger;


public abstract class IronSourceLogger {
    int mDebugLevel;


    private String mLoggerName;


    IronSourceLogger(String loggerName) {
        this.mLoggerName = loggerName;
        this.mDebugLevel = 0;
    }

    IronSourceLogger(String loggerName, int debugLevel) {
        this.mLoggerName = loggerName;
        this.mDebugLevel = debugLevel;
    }


    String getLoggerName() {
        return this.mLoggerName;
    }


    int getDebugLevel() {
        return this.mDebugLevel;
    }


    public void setDebugLevel(int debugLevel) {
        this.mDebugLevel = debugLevel;
    }


    public boolean equals(Object other) {
        if ((other != null) && ((other instanceof IronSourceLogger))) {
            IronSourceLogger otherLogger = (IronSourceLogger) other;


            return (this.mLoggerName != null) && (this.mLoggerName.equals(otherLogger.mLoggerName));
        }

        return false;
    }


    public abstract void log(IronSourceTag paramIronSourceTag, String paramString, int paramInt);


    public abstract void logException(IronSourceTag paramIronSourceTag, String paramString, Throwable paramThrowable);


    public static enum IronSourceTag {
        API,
        ADAPTER_API,
        CALLBACK,
        ADAPTER_CALLBACK,
        NETWORK,
        INTERNAL,
        NATIVE,
        EVENT;

        private IronSourceTag() {
        }
    }

    public class IronSourceLogLevel {
        public static final int VERBOSE = 0;
        public static final int INFO = 1;
        public static final int WARNING = 2;
        public static final int ERROR = 3;

        public IronSourceLogLevel() {
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/IronSourceLogger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */