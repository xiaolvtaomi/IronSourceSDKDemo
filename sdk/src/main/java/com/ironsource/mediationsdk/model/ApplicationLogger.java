 package com.ironsource.mediationsdk.model;


 public class ApplicationLogger
 {
   private int mServer;

   private int mPublisher;

   private int mConsole;


   public ApplicationLogger() {}


   public ApplicationLogger(int serverLoggerLevel, int publisherLoggerLevel, int consoleLoggerLevel)
   {
     this.mServer = serverLoggerLevel;
     this.mPublisher = publisherLoggerLevel;
     this.mConsole = consoleLoggerLevel;
   }

   public int getServerLoggerLevel() {
     return this.mServer;
   }

   public int getPublisherLoggerLevel() {
     return this.mPublisher;
   }

   public int getConsoleLoggerLevel() {
     return this.mConsole;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ApplicationLogger.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */