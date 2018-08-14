package com.ironsource.mediationsdk.logger;

import java.util.ArrayList;


public class IronSourceLoggerManager
        extends IronSourceLogger
        implements LogListener {
    private static IronSourceLoggerManager mInstance;
    private ArrayList<IronSourceLogger> mLoggers;
    private boolean mIsDebugEnabled = false;

    private IronSourceLoggerManager(String loggerName) {
        super(loggerName);
        this.mLoggers = new ArrayList();
        initSubLoggers();
    }

    private IronSourceLoggerManager(String loggerName, int debugLevel) {
        super(loggerName, debugLevel);
        this.mLoggers = new ArrayList();
        initSubLoggers();
    }


    private void initSubLoggers() {
        this.mLoggers.add(new ConsoleLogger(1));
    }


    public static synchronized IronSourceLoggerManager getLogger() {
        if (mInstance == null) {
            mInstance = new IronSourceLoggerManager(IronSourceLoggerManager.class.getSimpleName());
        }

        return mInstance;
    }


    public static synchronized IronSourceLoggerManager getLogger(int debugLevel) {
        if (mInstance == null) {
            mInstance = new IronSourceLoggerManager(IronSourceLoggerManager.class.getSimpleName());
        } else {
            mInstance.mDebugLevel = debugLevel;
        }

        return mInstance;
    }


    public void addLogger(IronSourceLogger toAdd) {
        this.mLoggers.add(toAdd);
    }


    public synchronized void log(IronSourceTag tag, String message, int logLevel) {
        if (logLevel < this.mDebugLevel) {
            return;
        }
        for (IronSourceLogger logger : this.mLoggers) {
            if (logger.getDebugLevel() <= logLevel) {
                logger.log(tag, message, logLevel);
            }
        }
    }


    public synchronized void onLog(IronSourceTag tag, String message, int logLevel) {
        log(tag, message, logLevel);
    }


    public synchronized void logException(IronSourceTag tag, String message, Throwable e) {
        if (e == null) {
            for (IronSourceLogger logger : this.mLoggers)
                logger.log(tag, message, 3);
        } else {
            for (IronSourceLogger logger : this.mLoggers) {
                logger.logException(tag, message, e);
            }
        }
    }


    private IronSourceLogger findLoggerByName(String loggerName) {
        IronSourceLogger result = null;

        for (IronSourceLogger logger : this.mLoggers) {
            if (logger.getLoggerName().equals(loggerName)) {
                result = logger;
                break;
            }
        }
        return result;
    }


    public void setLoggerDebugLevel(String loggerName, int debugLevel) {
        if (loggerName == null) {
            return;
        }
        IronSourceLogger logger = findLoggerByName(loggerName);


        if (logger != null) {

            if ((debugLevel >= 0) && (debugLevel <= 3)) {
                log(IronSourceTag.NATIVE, "setLoggerDebugLevel(loggerName:" + loggerName + " ,debugLevel:" + debugLevel + ")", 0);
                logger.setDebugLevel(debugLevel);
            } else {
                this.mLoggers.remove(logger);
            }

        } else {
            log(IronSourceTag.NATIVE, "Failed to find logger:setLoggerDebugLevel(loggerName:" + loggerName + " ,debugLevel:" + debugLevel + ")", 0);
        }
    }

    public boolean isDebugEnabled() {
        return this.mIsDebugEnabled;
    }

    public void setAdaptersDebug(boolean enabled) {
        this.mIsDebugEnabled = enabled;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/IronSourceLoggerManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */