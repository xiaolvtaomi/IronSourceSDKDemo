 package com.ironsource.mediationsdk.model;


 public class Configurations
 {
   private RewardedVideoConfigurations mRewardedVideoConfig;

   private InterstitialConfigurations mInterstitialConfig;

   private OfferwallConfigurations mOfferwallConfig;

   private BannerConfigurations mBannerConfig;

   private ApplicationConfigurations mApplicationConfig;


   public Configurations() {}


   public Configurations(RewardedVideoConfigurations rewardedVideoConfigurations, InterstitialConfigurations interstitialConfigurations, OfferwallConfigurations offerwallConfigurations, BannerConfigurations bannerConfigurations, ApplicationConfigurations applicationConfigurations)
   {
     if (rewardedVideoConfigurations != null) {
       this.mRewardedVideoConfig = rewardedVideoConfigurations;
     }
     if (interstitialConfigurations != null) {
       this.mInterstitialConfig = interstitialConfigurations;
     }
     if (offerwallConfigurations != null) {
       this.mOfferwallConfig = offerwallConfigurations;
     }

     if (bannerConfigurations != null) {
       this.mBannerConfig = bannerConfigurations;
     }

     this.mApplicationConfig = applicationConfigurations;
   }

   public ApplicationConfigurations getApplicationConfigurations() {
     return this.mApplicationConfig;
   }

   public RewardedVideoConfigurations getRewardedVideoConfigurations() {
     return this.mRewardedVideoConfig;
   }

   public InterstitialConfigurations getInterstitialConfigurations() {
     return this.mInterstitialConfig;
   }

   public OfferwallConfigurations getOfferwallConfigurations() {
     return this.mOfferwallConfig;
   }

   public BannerConfigurations getBannerConfigurations() {
     return this.mBannerConfig;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/model/Configurations.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */