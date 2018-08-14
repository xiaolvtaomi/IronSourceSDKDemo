package com.ironsource.mediationsdk.logger;


public class ThreadExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread thread, Throwable ex) {
        IronSourceLoggerManager.getLogger().logException(IronSourceLogger.IronSourceTag.NATIVE, "Thread name =" + thread.getName(), ex);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/ThreadExceptionHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */