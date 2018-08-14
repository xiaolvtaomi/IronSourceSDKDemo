package com.ironsource.mediationsdk.utils;

import android.text.TextUtils;

import com.ironsource.mediationsdk.logger.IronSourceError;


public class ErrorBuilder {
    public static IronSourceError buildNoConfigurationAvailableError(String adUnit) {
        return new IronSourceError(501, "" + adUnit + " Init Fail - Unable to retrieve configurations from the server");
    }

    public static IronSourceError buildInvalidConfigurationError(String adUnit) {
        return new IronSourceError(501, "" + adUnit + " Init Fail - Configurations from the server are not valid");
    }

    public static IronSourceError buildUsingCachedConfigurationError(String appKey, String userId) {
        return new IronSourceError(502, "Mediation - Unable to retrieve configurations from IronSource server, using cached configurations with appKey:" + appKey + " and userId:" + userId);
    }

    public static IronSourceError buildKeyNotSetError(String key, String provider, String adUnit) {
        if ((TextUtils.isEmpty(key)) || (TextUtils.isEmpty(provider))) {
            return getGenericErrorForMissingParams();
        }
        return new IronSourceError(505, adUnit + " Mediation - " + key + " is not set for " + provider);
    }

    public static IronSourceError buildInvalidKeyValueError(String key, String provider, String optionalReason) {
        if ((TextUtils.isEmpty(key)) || (TextUtils.isEmpty(provider))) {
            return getGenericErrorForMissingParams();
        }
        return new IronSourceError(506, "Mediation - " + key + " value is not valid for " + provider + (!TextUtils.isEmpty(optionalReason) ? " - " + optionalReason : ""));
    }

    public static IronSourceError buildInvalidCredentialsError(String credentialName, String credentialValue, String errorMessage) {
        String resultingMessage = "Init Fail - " + credentialName + " value " + credentialValue + " is not valid" + (!TextUtils.isEmpty(errorMessage) ? " - " + errorMessage : "");
        return new IronSourceError(506, resultingMessage);
    }

    public static IronSourceError buildInitFailedError(String errorMsg, String adUnit) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = adUnit + " init failed due to an unknown error";
        } else {
            errorMsg = adUnit + " - " + errorMsg;
        }
        return new IronSourceError(508, errorMsg);
    }

    public static IronSourceError buildNoAdsToShowError(String adUnit) {
        return new IronSourceError(509, adUnit + " Show Fail - No ads to show");
    }

    public static IronSourceError buildShowFailedError(String adUnit, String error) {
        return new IronSourceError(509, adUnit + " Show Fail - " + error);
    }

    public static IronSourceError buildLoadFailedError(String adUnit, String adapterName, String errorMsg) {
        String resultingMessage = "" + adUnit + " Load Fail" + (!TextUtils.isEmpty(adapterName) ? " " + adapterName : "") + " - ";
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "unknown error";
        }

        resultingMessage = resultingMessage + errorMsg;
        return new IronSourceError(510, resultingMessage);
    }

    public static IronSourceError buildGenericError(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "An error occurred";
        }
        return new IronSourceError(510, errorMsg);
    }

    public static IronSourceError buildNoInternetConnectionInitFailError(String adUnit) {
        return new IronSourceError(520, "" + adUnit + " Init Fail - No Internet connection");
    }

    public static IronSourceError buildNoInternetConnectionLoadFailError(String adUnit) {
        return new IronSourceError(520, "" + adUnit + " Load Fail - No Internet connection");
    }

    public static IronSourceError buildNoInternetConnectionShowFailError(String adUnit) {
        return new IronSourceError(520, "" + adUnit + " Show Fail - No Internet connection");
    }

    public static IronSourceError buildCappedPerPlacementError(String adUnit, String error) {
        return new IronSourceError(524, adUnit + " Show Fail - " + error);
    }

    public static IronSourceError buildCappedPerSessionError(String adUnit) {
        return new IronSourceError(526, adUnit + " Show Fail - Networks have reached their cap per session");
    }

    public static IronSourceError buildNonExistentInstanceError(String adUnit) {
        return new IronSourceError(527, adUnit + " The requested instance does not exist");
    }

    private static IronSourceError getGenericErrorForMissingParams() {
        return buildGenericError("Mediation - wrong configuration");
    }

    public static IronSourceError buildLoadFailedError(String errorMsg) {
        if (TextUtils.isEmpty(errorMsg)) {
            errorMsg = "Load failed due to an unknown error";
        } else
            errorMsg = "Load failed - " + errorMsg;
        return new IronSourceError(510, errorMsg);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/ErrorBuilder.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */