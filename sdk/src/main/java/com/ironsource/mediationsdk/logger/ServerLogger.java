package com.ironsource.mediationsdk.logger;

import android.util.Log;

import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ServerLogger
        extends IronSourceLogger {
    public static final String NAME = "server";
    private final int SERVER_LOGS_SIZE_LIMIT = 1000;

    private ArrayList<ServerLogEntry> mLogs;

    public ServerLogger() {
        super("server");
        this.mLogs = new ArrayList();
    }

    public ServerLogger(int debugLevel) {
        super("server", debugLevel);
        this.mLogs = new ArrayList();
    }


    private synchronized void addLogEntry(ServerLogEntry entry) {
        this.mLogs.add(entry);
        boolean shouldSendLogs = shouldSendLogs();

        if (shouldSendLogs) {
            send();
        } else if (this.mLogs.size() > 1000) {
            try {
                ArrayList<ServerLogEntry> newerLog = new ArrayList();

                for (int i = 500; i < this.mLogs.size(); i++) {
                    newerLog.add(this.mLogs.get(i));
                }

                this.mLogs = newerLog;
            } catch (Exception e) {
                this.mLogs = new ArrayList();
            }
        }
    }


    private boolean shouldSendLogs() {
        return ((ServerLogEntry) this.mLogs.get(this.mLogs.size() - 1)).getLogLevel() == 3;
    }


    private void send() {
        IronSourceUtils.createAndStartWorker(new LogsSender(this.mLogs), "LogsSender");
        this.mLogs = new ArrayList();
    }


    private String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


    public synchronized void log(IronSourceLogger.IronSourceTag tag, String message, int logLevel) {
        addLogEntry(new ServerLogEntry(tag, getTimestamp(), message, logLevel));
    }


    public synchronized void logException(IronSourceLogger.IronSourceTag tag, String message, Throwable e) {
        StringBuilder logMessage = new StringBuilder(message);

        if (e != null) {
            logMessage.append(":stacktrace[");
            logMessage.append(Log.getStackTraceString(e)).append("]");
        }

        addLogEntry(new ServerLogEntry(tag, getTimestamp(), logMessage.toString(), 3));
    }


    private class SendingCalc {
        private int DEFAULT_SIZE = 1;
        private int DEFAULT_TIME = 1;
        private int DEFAULT_DEBUG_LEVEL = 3;

        public SendingCalc() {
            initDefaults();
        }


        private void initDefaults() {
        }


        public void notifyEvent(int event) {
            if (calc(event))
                ServerLogger.this.send();
        }

        private boolean calc(int event) {
            if (error(event))
                return true;
            if (size())
                return true;
            if (time()) {
                return true;
            }
            return false;
        }


        private boolean time() {
            return false;
        }

        private boolean error(int event) {
            return event == 3;
        }


        private boolean size() {
            return false;
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/logger/ServerLogger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */