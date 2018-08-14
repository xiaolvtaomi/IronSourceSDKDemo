package com.ironsource.sdk;

import android.app.Activity;

import com.ironsource.sdk.agent.IronSourceAdsAdvertiserAgent;
import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.DebugMode;

public class SSAFactory {
    public static SSAPublisher getPublisherInstance(Activity activity) throws Exception {
        return IronSourceAdsPublisherAgent.getInstance(activity);
    }

    public static SSAPublisher getPublisherTestInstance(Activity activity) throws Exception {
        return IronSourceAdsPublisherAgent.getInstance(activity, SSAEnums.DebugMode.MODE_2.getValue());
    }


    public static SSAPublisher getPublisherTestInstance(Activity activity, int debugMode)
            throws Exception {
        return IronSourceAdsPublisherAgent.getInstance(activity, debugMode);
    }


    public static SSAAdvertiser getAdvertiserInstance() {
        return IronSourceAdsAdvertiserAgent.getInstance();
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/SSAFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */