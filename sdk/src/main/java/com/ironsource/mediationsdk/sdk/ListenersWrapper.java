 package com.ironsource.mediationsdk.sdk;

 import android.os.Handler;
 import android.os.Looper;
 import com.ironsource.eventsmodule.EventData;
 import com.ironsource.mediationsdk.events.InterstitialEventsManager;
 import com.ironsource.mediationsdk.events.RewardedVideoEventsManager;
 import com.ironsource.mediationsdk.logger.IronSourceError;
 import com.ironsource.mediationsdk.logger.IronSourceLogger;
 import com.ironsource.mediationsdk.logger.IronSourceLogger.IronSourceTag;
 import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;
 import com.ironsource.mediationsdk.model.Placement;
 import com.ironsource.mediationsdk.utils.IronSourceUtils;
 import org.json.JSONException;
 import org.json.JSONObject;






 public class ListenersWrapper
   implements RewardedVideoListener, InterstitialListener, InternalOfferwallListener, RewardedInterstitialListener, SegmentListener, ISDemandOnlyRewardedVideoListener, ISDemandOnlyInterstitialListener
 {
   private RewardedVideoListener mRewardedVideoListener;
   private ISDemandOnlyRewardedVideoListener mISDemandOnlyRewardedVideoListener;
   private InterstitialListener mInterstitialListener;
   private ISDemandOnlyInterstitialListener mISDemandOnlyInterstitialListener;
   private OfferwallListener mOfferwallListener;
   private RewardedInterstitialListener mRewardedInterstitialListener;
   private SegmentListener mSegementListener;
   private CallbackHandlerThread mCallbackHandlerThread;

   public ListenersWrapper()
   {
     this.mCallbackHandlerThread = new CallbackHandlerThread();
     this.mCallbackHandlerThread.start();
   }

   private boolean canSendCallback(Object productListener) {
     return (productListener != null) && (this.mCallbackHandlerThread != null);
   }

   private void sendCallback(Runnable callbackRunnable) {
     if (this.mCallbackHandlerThread == null) {
       return;
     }
     Handler callbackHandler = this.mCallbackHandlerThread.getCallbackHandler();
     if (callbackHandler != null) {
       callbackHandler.post(callbackRunnable);
     }
   }

   public void setRewardedVideoListener(RewardedVideoListener rewardedVideoListener) {
     this.mRewardedVideoListener = rewardedVideoListener;
   }

   public void setISDemandOnlyRewardedVideoListener(ISDemandOnlyRewardedVideoListener demandOnlyRewardedVideoListener) {
     this.mISDemandOnlyRewardedVideoListener = demandOnlyRewardedVideoListener;
   }

   public void setInterstitialListener(InterstitialListener interstitialListener) {
     this.mInterstitialListener = interstitialListener;
   }

   public void setISDemandOnlyInterstitialListener(ISDemandOnlyInterstitialListener demandOnlyInterstitialListener) {
     this.mISDemandOnlyInterstitialListener = demandOnlyInterstitialListener;
   }

   public void setOfferwallListener(OfferwallListener offerwallListener) {
     this.mOfferwallListener = offerwallListener;
   }

   public void setRewardedInterstitialListener(RewardedInterstitialListener rewardedInterstitialListener) {
     this.mRewardedInterstitialListener = rewardedInterstitialListener;
   }

   public void setSegmentListener(SegmentListener segmentListener) {
     this.mSegementListener = segmentListener;
   }

   public void onSegmentReceived(final String segment)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onSegmentReceived(" + segment + ")", 1);

     if (canSendCallback(this.mSegementListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           if (segment != null) {
             ListenersWrapper.this.mSegementListener.onSegmentReceived(segment);
           } else {
             ListenersWrapper.this.mSegementListener.onSegmentReceived("");
           }
         }
       });
     }
   }

   public void onRewardedVideoAdOpened()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdOpened()", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdOpened();
         }
       });
     }
   }

   public void onRewardedVideoAdClosed()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdClosed()", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdClosed();
         }
       });
     }
   }

   public void onRewardedVideoAvailabilityChanged(final boolean available)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAvailabilityChanged(available:" + available + ")", 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
     try {
       data.put("status", String.valueOf(available));
     } catch (JSONException e) {
       e.printStackTrace();
     }

     EventData event = new EventData(7, data);
     RewardedVideoEventsManager.getInstance().log(event);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAvailabilityChanged(available);
         }
       });
     }
   }

   public void onRewardedVideoAdStarted()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdStarted()", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdStarted();
         }
       });
     }
   }

   public void onRewardedVideoAdEnded()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdEnded()", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdEnded();
         }
       });
     }
   }

   public void onRewardedVideoAdRewarded(final Placement placement)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdRewarded(" + placement.toString() + ")", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdRewarded(placement);
         }
       });
     }
   }

   public void onRewardedVideoAdClicked(final Placement placement)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdClicked(" + placement.getPlacementName() + ")", 1);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdClicked(placement);
         }
       });
     }
   }

   public void onRewardedVideoAdShowFailed(final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdShowFailed(" + error.toString() + ")", 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
     try {
       data.put("status", "false");
       if (error.getErrorCode() == 524) {
         data.put("reason", 1);
       }
       data.put("errorCode", error.getErrorCode());
     } catch (JSONException e) {
       e.printStackTrace();
     }
     EventData event = new EventData(17, data);
     RewardedVideoEventsManager.getInstance().log(event);

     if (canSendCallback(this.mRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedVideoListener.onRewardedVideoAdShowFailed(error);
         }
       });
     }
   }


   public void onInterstitialAdReady()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdReady()", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdReady();
         }
       });
     }
   }

   public void onInterstitialAdLoadFailed(final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdLoadFailed(" + error + ")", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdLoadFailed(error);
         }
       });
     }
   }

   public void onInterstitialAdOpened()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdOpened()", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdOpened();
         }
       });
     }
   }

   public void onInterstitialAdShowSucceeded()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdShowSucceeded()", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdShowSucceeded();
         }
       });
     }
   }

   public void onInterstitialAdShowFailed(final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdShowFailed(" + error + ")", 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
     try {
       if (error.getErrorCode() == 524) {
         data.put("reason", 1);
       }
       data.put("errorCode", error.getErrorCode());
     } catch (JSONException e) {
       e.printStackTrace();
     }
     EventData event = new EventData(29, data);
     InterstitialEventsManager.getInstance().log(event);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdShowFailed(error);
         }
       });
     }
   }

   public void onInterstitialAdClicked()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdClicked()", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdClicked();
         }
       });
     }
   }

   public void onInterstitialAdClosed()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdClosed()", 1);

     if (canSendCallback(this.mInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mInterstitialListener.onInterstitialAdClosed();
         }
       });
     }
   }

   public void onInterstitialAdRewarded()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdRewarded()", 1);

     if (canSendCallback(this.mRewardedInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mRewardedInterstitialListener.onInterstitialAdRewarded();
         }
       });
     }
   }



   public void onOfferwallOpened()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onOfferwallOpened()", 1);

     if (canSendCallback(this.mOfferwallListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mOfferwallListener.onOfferwallOpened();
         }
       });
     }
   }

   public void onOfferwallShowFailed(final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onOfferwallShowFailed(" + error + ")", 1);

     if (canSendCallback(this.mOfferwallListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mOfferwallListener.onOfferwallShowFailed(error);
         }
       });
     }
   }

   public boolean onOfferwallAdCredited(int credits, int totalCredits, boolean totalCreditsFlag)
   {
     boolean result = false;

     if (this.mOfferwallListener != null) {
       result = this.mOfferwallListener.onOfferwallAdCredited(credits, totalCredits, totalCreditsFlag);
     }
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onOfferwallAdCredited(credits:" + credits + ", " + "totalCredits:" + totalCredits + ", " + "totalCreditsFlag:" + totalCreditsFlag + "):" + result, 1);



     return result;
   }

   public void onGetOfferwallCreditsFailed(final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onGetOfferwallCreditsFailed(" + error + ")", 1);

     if (canSendCallback(this.mOfferwallListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mOfferwallListener.onGetOfferwallCreditsFailed(error);
         }
       });
     }
   }

   public void onOfferwallClosed()
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onOfferwallClosed()", 1);

     if (canSendCallback(this.mOfferwallListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mOfferwallListener.onOfferwallClosed();
         }
       });
     }
   }

   public void onOfferwallAvailable(boolean isAvailable)
   {
     onOfferwallAvailable(isAvailable, null);
   }

   public void onOfferwallAvailable(final boolean isAvailable, IronSourceError error)
   {
     String logString = "onOfferwallAvailable(isAvailable: " + isAvailable + ")";
     if (error != null) {
       logString = logString + ", error: " + error.getErrorMessage();
     }
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, logString, 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
     try {
       data.put("status", String.valueOf(isAvailable));
       if (error != null) {
         data.put("errorCode", error.getErrorCode());
       }
     } catch (JSONException e) {
       e.printStackTrace();
     }
     EventData event = new EventData(302, data);
     RewardedVideoEventsManager.getInstance().log(event);

     if (canSendCallback(this.mOfferwallListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mOfferwallListener.onOfferwallAvailable(isAvailable);
         }
       });
     }
   }


   public void onRewardedVideoAdOpened(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdOpened(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAdOpened(instanceId);
         }
       });
     }
   }

   public void onRewardedVideoAdClosed(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdClosed(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAdClosed(instanceId);
         }
       });
     }
   }

   public void onRewardedVideoAvailabilityChanged(final String instanceId, final boolean available)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAvailabilityChanged(" + instanceId + ", " + available + ")", 1);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAvailabilityChanged(instanceId, available);
         }
       });
     }
   }

   public void onRewardedVideoAdRewarded(final String instanceId, final Placement placement)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdRewarded(" + instanceId + ", " + placement.toString() + ")", 1);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAdRewarded(instanceId, placement);
         }
       });
     }
   }

   public void onRewardedVideoAdShowFailed(final String instanceId, final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdShowFailed(" + instanceId + ", " + error.toString() + ")", 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
     try {
       data.put("status", "false");
       if (error.getErrorCode() == 524) {
         data.put("reason", 1);
       }
       data.put("errorCode", error.getErrorCode());
     } catch (JSONException e) {
       e.printStackTrace();
     }
     EventData event = new EventData(17, data);
     RewardedVideoEventsManager.getInstance().log(event);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAdShowFailed(instanceId, error);
         }
       });
     }
   }

   public void onRewardedVideoAdClicked(final String instanceId, final Placement placement)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onRewardedVideoAdClicked(" + instanceId + ", " + placement.getPlacementName() + ")", 1);

     if (canSendCallback(this.mISDemandOnlyRewardedVideoListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyRewardedVideoListener.onRewardedVideoAdClicked(instanceId, placement);
         }
       });
     }
   }



   public void onInterstitialAdReady(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdReady(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdReady(instanceId);
         }
       });
     }
   }

   public void onInterstitialAdLoadFailed(final String instanceId, final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdLoadFailed(" + instanceId + ", " + error + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdLoadFailed(instanceId, error);
         }
       });
     }
   }

   public void onInterstitialAdOpened(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdOpened(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdOpened(instanceId);
         }
       });
     }
   }

   public void onInterstitialAdClosed(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdClosed(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdClosed(instanceId);
         }
       });
     }
   }

   public void onInterstitialAdShowSucceeded(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdShowSucceeded(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdShowSucceeded(instanceId);
         }
       });
     }
   }

   public void onInterstitialAdShowFailed(final String instanceId, final IronSourceError error)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdShowFailed(" + instanceId + ", " + error + ")", 1);

     JSONObject data = IronSourceUtils.getMediationAdditionalData(true);
     try {
       if (error.getErrorCode() == 524) {
         data.put("reason", 1);
       }
       data.put("errorCode", error.getErrorCode());
     } catch (JSONException e) {
       e.printStackTrace();
     }
     EventData event = new EventData(29, data);
     InterstitialEventsManager.getInstance().log(event);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdShowFailed(instanceId, error);
         }
       });
     }
   }

   public void onInterstitialAdClicked(final String instanceId)
   {
     IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.CALLBACK, "onInterstitialAdClicked(" + instanceId + ")", 1);

     if (canSendCallback(this.mISDemandOnlyInterstitialListener)) {
       sendCallback(new Runnable()
       {
         public void run() {
           ListenersWrapper.this.mISDemandOnlyInterstitialListener.onInterstitialAdClicked(instanceId);
         }
       });
     }
   }

   private class CallbackHandlerThread extends Thread
   {
     private Handler mCallbackHandler;

     private CallbackHandlerThread() {}

     public void run()
     {
       Looper.prepare();




       this.mCallbackHandler = new Handler();





       Looper.loop();
     }

     public Handler getCallbackHandler() {
       return this.mCallbackHandler;
     }
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/ListenersWrapper.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */