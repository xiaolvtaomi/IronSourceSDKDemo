package com.ironsource.sdk.handlers;

import android.app.Activity;

import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.controller.IronSourceWebView;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.BackButtonState;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;


public class BackButtonHandler {
    public static BackButtonHandler mInstance;

    public static BackButtonHandler getInstance() {
        if (mInstance == null) {
            return new BackButtonHandler();
        }
        return mInstance;
    }


    public boolean handleBackButton(Activity activity) {
        SSAEnums.BackButtonState state = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getBackButtonState();

        switch (state) {
            case None:
                return false;

            case Device:
                return false;
            case Controller:
                try {
                    IronSourceAdsPublisherAgent ssaPubAgt = IronSourceAdsPublisherAgent.getInstance(activity);
                    IronSourceWebView webViewController = ssaPubAgt.getWebViewController();
                    if (webViewController != null) {
                        webViewController.nativeNavigationPressed("back");
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
        }

        return false;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/handlers/BackButtonHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */