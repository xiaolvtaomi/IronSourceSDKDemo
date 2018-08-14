package com.ironsource.sdk.analytics.moat;

import android.app.Application;
import android.webkit.WebView;

import org.json.JSONObject;


public class MOATManager {
//    private static WebAdTracker webAdTracker;
    private static EventsListener mEventsListener;
    private static final String loggingEnabled = "loggingEnabled";
    private static final String autoTrackGMAInterstitials = "autoTrackGMAInterstitials";
    private static final String disableAdIdCollection = "disableAdIdCollection";
    private static final String disableLocationService = "disableLocationServices";


    public static void setEventListener(EventsListener eventsListener) {
        mEventsListener = eventsListener;
    }

    public static void init(Application application) throws Exception {
        initWithOptions(null, application);
    }

    public static void initWithOptions(JSONObject options, Application application) throws Exception {
//        MoatOptions o = null;
//
//        if ((options != null) && (options.length() > 0)) {
//            o = createMoatOptions(options);
//        }
//
//        MoatAnalytics.getInstance().start(o, application);
    }

//    private static MoatOptions createMoatOptions(JSONObject options) {
//        MoatOptions mo = new MoatOptions();
//
//        mo.loggingEnabled = options.optBoolean("loggingEnabled");
//        mo.autoTrackGMAInterstitials = options.optBoolean("autoTrackGMAInterstitials");
//        mo.disableAdIdCollection = options.optBoolean("disableAdIdCollection");
//        mo.disableLocationServices = options.optBoolean("disableLocationServices");
//
//        return mo;
//    }

    public static void createAdTracker(WebView webView) throws Exception {
    }

    public static void startTracking() throws Exception {
    }

    public static void stopTracking() throws Exception {
    }

    public static abstract interface EventsListener {

        public void onTrackingStarted(String s) ;

        public void onTrackingFailedToStart(String s);

        public void onTrackingStopped(String s) ;

    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/analytics/moat/MOATManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */