package com.ironsource.mediationsdk.logger;


public class PublisherLogger
        extends IronSourceLogger {
    private static final String NAME = "publisher";


    private LogListener mLogListener;


    private PublisherLogger() {
        super("publisher");
    }

    public PublisherLogger(LogListener logListener, int debugLevel) {
        super("publisher", debugLevel);
        this.mLogListener = logListener;
    }

    public synchronized void log(IronSourceLogger.IronSourceTag tag, String message, int logLevel) {
        if ((this.mLogListener != null) && (message != null)) {
            this.mLogListener.onLog(tag, message, logLevel);
        }
    }

    public void logException(IronSourceLogger.IronSourceTag tag, String message, Throwable e) {
        if (e != null) {
            log(tag, e.getMessage(), 3);
        }
    }

    public void setLogListener(LogListener listener) {
        this.mLogListener = listener;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/PublisherLogger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */