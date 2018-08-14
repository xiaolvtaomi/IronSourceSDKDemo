package com.ironsource.mediationsdk.utils;

public class IronSourceConstants {
    public static final String IRONSOURCE_CONFIG_NAME = "SupersonicAds";
    public static final String RIS_CONFIG_NAME = "RIS";
    public static final String REQUEST_URL = "requestUrl";
    public static final String START_ADAPTER = "startAdapter";
    public static final String MEDIATION_PROVIDER_NAME = "Mediation";
    public static final String REWARDED_VIDEO_AD_UNIT = "Rewarded Video";
    public static final String INTERSTITIAL_AD_UNIT = "Interstitial";
    public static final String OFFERWALL_AD_UNIT = "Offerwall";
    public static final String BANNER_AD_UNIT = "Banner";
    public static final String GENERAL_AD_UNIT = "Mediation";
    public static final String REWARDED_VIDEO_EVENT_TYPE = "RV";
    public static final String INTERSTITIAL_EVENT_TYPE = "IS";
    public static final String ERROR_CODE_KEY = "errorCode";
    public static final String ERROR_CODE_MESSAGE_KEY = "errorMessage";
    public static final String TYPE_USER_GENERATED = "userGenerated";
    public static final String TYPE_GAID = "GAID";
    public static final String TYPE_UUID = "UUID";
    public static final int GET_INSTANCE_CODE = 14;
    public static final int REVIVED_EVENT = 114;
    public static final int CONSENT_TRUE_CODE = 40;
    public static final int CONSENT_FALSE_CODE = 41;
    public static final int SHOW_REWARDED_VIDEO_CODE = 2;
    public static final int IS_REWARDED_VIDEO_AVAILABLE_CODE = 3;
    public static final int REWARDED_VIDEO_AD_OPENED = 5;
    public static final int REWARDED_VIDEO_AD_CLOSED = 6;
    public static final int AVAILABILITY_CHANGED = 7;
    public static final int VIDEO_START = 8;
    public static final int VIDEO_END = 9;
    public static final int VIDEO_AD_REWARDED = 10;
    public static final int VIDEO_AD_VISIBLE = 11;
    public static final int SHOW_REWARDED_VIDEO_RESULT = 17;
    public static final int REWARDED_VIDEO_PUBLISHER_CHECK_AVAILABILITY = 18;
    public static final int REWARDED_VIDEO_SHOW_CHECK_AVAILABILITY = 19;
    public static final int RV_PLACEMENT_CAPPED = 20;
    public static final int REWARDED_VIDEO_SHOW_CHANCE = 119;
    public static final int REWARDED_VIDEO_AD_CLICKED = 128;
    public static final int OFFERWALL_AVAILABLE = 302;
    public static final int OFFERWALL_OPENED = 305;
    public static final int INTERSTITIAL_AD_REWARDED = 290;
    public static final int REWARDED_VIDEO_DAILY_CAPPED = 150;
    public static final int INTERSTITIAL_DAILY_CAPPED = 250;
    public static final int LOAD_INTERSTITIAL_CODE = 22;
    public static final int SHOW_INTERSTITIAL_CODE = 23;
    public static final int INTERSTITIAL_AD_OPENED = 25;
    public static final int INTERSTITIAL_AD_CLOSED = 26;
    public static final int INTERSTITIAL_AD_READY = 27;
    public static final int INTERSTITIAL_AD_LOAD_FAILED = 227;
    public static final int INTERSTITIAL_AD_CLICKED = 28;
    public static final int SHOW_INTERSTITIAL_FAILED = 29;
    public static final int INTERSTITIAL_PUBLISHER_CHECK_AVAILABILITY = 30;
    public static final int INTERSTITIAL_AD_VISIBLE = 31;
    public static final int IS_PLACEMENT_CAPPED = 34;
    public static final int INTERSTITIAL_SHOW_CHANCE = 219;
    public static final int BN_LOAD = 3001;
    public static final int BN_INSTANCE_LOAD = 3002;
    public static final int BN_INSTANCE_LOAD_SUCCESS = 3005;
    public static final int BN_INSTANCE_CLICK = 3008;
    public static final int BN_RELOAD = 3011;
    public static final int BN_INSTANCE_RELOAD = 3012;
    public static final int BN_INSTANCE_RELOAD_SUCCESS = 3015;
    public static final int BN_DESTROY = 3100;
    public static final int BN_CALLBACK_LOAD_SUCCESS = 3110;
    public static final int BN_CALLBACK_LOAD_ERROR = 3111;
    public static final int BN_CALLBACK_CLICK = 3112;
    public static final int BN_CALLBACK_PRESENT_SCREEN = 3113;
    public static final int BN_CALLBACK_DISMISS_SCREEN = 3114;
    public static final int BN_CALLBACK_LEAVE_APP = 3115;
    public static final int BN_SKIP_RELOAD = 3200;
    public static final int BN_RELOAD_FAILED = 3201;
    public static final int BN_INSTANCE_LOAD_ERROR = 3300;
    public static final int BN_INSTANCE_RELOAD_ERROR = 3301;
    public static final int BN_INSTANCE_PRESENT_SCREEN = 3302;
    public static final int BN_INSTANCE_DISMISS_SCREEN = 3303;
    public static final int BN_INSTANCE_LEAVE_APP = 3304;
    public static final int BN_INSTANCE_DESTROY = 3305;
    public static final int BN_PLACEMENT_CAPPED = 3400;
    public static final String FALSE_AVAILABILITY_REASON_NO_INTERNET = "noInternetConnection";
    public static final String FALSE_AVAILABILITY_REASON_NO_SERVER_RESPONSE = "noServerResponse";
    public static final String FALSE_AVAILABILITY_REASON_SERVER_RESPONSE_IS_NOT_VALID = "serverResponseIsNotValid";
    public static final String FALSE_INVALID_APPKEY = "invalidAppKey";
    public static final int RETRY_DELAY = 1;
    public static final int RETRY_COUNTER = 0;
    public static final int RETRY_LIMIT = 62;
    public static final int RETRY_GROW_LIMIT = 12;
    public static final int RETRY_AVAILABILITY_LIMIT = 5;
    public static final int RELOAD_IMPRESSION_REASON = 100;

    public class Gender {
        public static final String MALE = "male";
        public static final String FEMALE = "female";
        public static final String UNKNOWN = "unknown";

        public Gender() {
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/IronSourceConstants.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */