 package com.ironsource.mediationsdk;

 import com.ironsource.mediationsdk.logger.IronSourceLogger;
 import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
 import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
 import com.ironsource.mediationsdk.model.ProviderSettings;
 import java.util.TimerTask;

 public abstract class AbstractSmash implements com.ironsource.mediationsdk.sdk.BaseApi
 {
   MEDIATION_STATE mMediationState;
   AbstractAdapter mAdapter;
   ProviderSettings mAdapterConfigs;
   String mName;
   String mNameForReflection;
   String mInstanceName;
   boolean mIsMultipleInstances;
   boolean mIsInForeground;
   String mSpId;

   public static enum MEDIATION_STATE
   {
     NOT_INITIATED(0),
     INIT_FAILED(1),
     INITIATED(2),
     AVAILABLE(3),
     NOT_AVAILABLE(4),
     EXHAUSTED(5),
     CAPPED_PER_SESSION(6),
     INIT_PENDING(7),
     LOAD_PENDING(8),
     CAPPED_PER_DAY(9);

     private int mValue;

     private MEDIATION_STATE(int value) {
       this.mValue = value;
     }

     public int getValue() {
       return this.mValue;
     }
   }



   int mSessionShowCounter;


   int mIterationShowCounter;


   TimerTask mInitTimerTask;


   TimerTask mLoadTimerTask;


   int mMaxAdsPerIteration;

   int mMaxAdsPerSession;

   int mMaxAdsPerDay;

   int mProviderPriority;

   IronSourceLoggerManager mLoggerManager;

   final String MAX_ADS_PER_SESSION_KEY = "maxAdsPerSession";
   final String MAX_ADS_PER_ITERATION_KEY = "maxAdsPerIteration";
   final String MAX_ADS_PER_DAY_KEY = "maxAdsPerDay";
   public static final int MAX_ADS_PER_DAY_DEFAULT_VALUE = 99;

   AbstractSmash(ProviderSettings adapterConfigs)
   {
     this.mNameForReflection = adapterConfigs.getProviderTypeForReflection();
     this.mInstanceName = adapterConfigs.getProviderInstanceName();
     this.mIsMultipleInstances = adapterConfigs.isMultipleInstances();
     this.mAdapterConfigs = adapterConfigs;
     this.mSpId = adapterConfigs.getSubProviderId();
     this.mIterationShowCounter = 0;
     this.mSessionShowCounter = 0;
     this.mMediationState = MEDIATION_STATE.NOT_INITIATED;
     this.mLoggerManager = IronSourceLoggerManager.getLogger();
     this.mIsInForeground = true;



     if (this.mIsMultipleInstances) {
       this.mName = this.mNameForReflection;
     } else
       this.mName = adapterConfigs.getProviderName();
   }

   void setAdapterForSmash(AbstractAdapter adapter) {
     this.mAdapter = adapter;
   }

   boolean isExhausted() {
     return this.mIterationShowCounter >= this.mMaxAdsPerIteration;
   }

   boolean isCappedPerSession() {
     return this.mSessionShowCounter >= this.mMaxAdsPerSession;
   }

   boolean isCappedPerDay() {
     return this.mMediationState == MEDIATION_STATE.CAPPED_PER_DAY;
   }

   boolean isMediationAvailable() {
     return (!isExhausted()) && (!isCappedPerSession()) && (!isCappedPerDay());
   }

   void preShow() {
     this.mIterationShowCounter += 1;
     this.mSessionShowCounter += 1;

     if (isCappedPerSession()) {
       setMediationState(MEDIATION_STATE.CAPPED_PER_SESSION);
     } else if (isExhausted())
       setMediationState(MEDIATION_STATE.EXHAUSTED);
   }

   void stopInitTimer() {
     try {
       if (this.mInitTimerTask != null) {
         this.mInitTimerTask.cancel();
         this.mInitTimerTask = null;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
   }

   void stopLoadTimer() {
     try {
       if (this.mLoadTimerTask != null) {
         this.mLoadTimerTask.cancel();
         this.mLoadTimerTask = null;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
   }

   void setPluginData(String pluginType, String pluginFrameworkVersion) {
     if (this.mAdapter != null) {
       this.mAdapter.setPluginData(pluginType, pluginFrameworkVersion);
     }
   }


   abstract void completeIteration();


   abstract void startInitTimer();

   abstract void startLoadTimer();

   MEDIATION_STATE getMediationState()
   {
     return this.mMediationState;
   }

   String getNameForReflection() {
     return this.mNameForReflection;
   }

   String getInstanceName() {
     return this.mInstanceName;
   }

   public String getName() {
     return this.mName;
   }

   public String getSubProviderId() {
     return this.mSpId;
   }

   int getMaxAdsPerSession() {
     return this.mMaxAdsPerSession;
   }

   int getMaxAdsPerIteration() {
     return this.mMaxAdsPerIteration;
   }

   public int getMaxAdsPerDay() {
     return this.mMaxAdsPerDay;
   }

   public AbstractAdapter getAdapter() {
     return this.mAdapter;
   }

   public int getProviderPriority() {
     return this.mProviderPriority;
   }


   synchronized void setMediationState(MEDIATION_STATE state)
   {
     if (this.mMediationState == state) {
       return;
     }

     this.mMediationState = state;
     this.mLoggerManager.log(IronSourceLogger.IronSourceTag.INTERNAL, "Smart Loading - " + getInstanceName() + " state changed to " + state.toString(), 0);

     if ((this.mAdapter != null) && ((state == MEDIATION_STATE.CAPPED_PER_SESSION) || (state == MEDIATION_STATE.CAPPED_PER_DAY))) {
       this.mAdapter.setMediationState(state, getAdUnitString());
     }
   }


   public void onResume(android.app.Activity activity)
   {
     if (this.mAdapter != null) {
       this.mAdapter.onResume(activity);
     }
     this.mIsInForeground = true;
   }

   public void onPause(android.app.Activity activity)
   {
     if (this.mAdapter != null) {
       this.mAdapter.onPause(activity);
     }
     this.mIsInForeground = false;
   }


   public void setAge(int age)
   {
     if (this.mAdapter != null) {
       this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getName() + ":setAge(age:" + age + ")", 1);
       this.mAdapter.setAge(age);
     }
   }

   public void setGender(String gender)
   {
     if (this.mAdapter != null) {
       this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getName() + ":setGender(gender:" + gender + ")", 1);
       this.mAdapter.setGender(gender);
     }
   }

   public void setMediationSegment(String segment)
   {
     if (this.mAdapter != null) {
       this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getName() + ":setMediationSegment(segment:" + segment + ")", 1);
       this.mAdapter.setMediationSegment(segment);
     }
   }

   public java.util.HashSet<String> getAllSettingsForProvider(String fieldName) {
     return IronSourceObject.getInstance().getAllSettingsForProvider(this.mNameForReflection, fieldName);
   }



   protected abstract String getAdUnitString();



   void setProviderPriority(int providerPriority)
   {
     this.mProviderPriority = providerPriority;
   }

   void setConsent(boolean consent) {
     if (this.mAdapter != null) {
       this.mLoggerManager.log(IronSourceLogger.IronSourceTag.ADAPTER_API, getName() + " | " + getAdUnitString() + "| setConsent(consent:" + consent + ")", 1);
       this.mAdapter.setConsent(consent);
     }
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/AbstractSmash.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */