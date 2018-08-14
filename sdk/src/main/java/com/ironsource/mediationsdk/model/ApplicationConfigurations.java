 package com.ironsource.mediationsdk.model;


 public class ApplicationConfigurations
 {
   private ApplicationLogger mLogger;

   private ServerSegmetData mSegmetData;

   private boolean mIsIntegration;


   public ApplicationConfigurations()
   {
     this.mLogger = new ApplicationLogger();
   }

   public ApplicationConfigurations(ApplicationLogger logger, ServerSegmetData data, boolean isIntegration) {
     this.mLogger = logger;
     this.mSegmetData = data;
     this.mIsIntegration = isIntegration;
   }

   public ApplicationLogger getLoggerConfigurations() {
     return this.mLogger;
   }

   public ServerSegmetData getSegmetData() {
     return this.mSegmetData;
   }

   public boolean getIntegration()
   {
     return this.mIsIntegration;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/ApplicationConfigurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */