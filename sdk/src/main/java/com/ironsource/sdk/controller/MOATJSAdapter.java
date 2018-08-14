package com.ironsource.sdk.controller;

import android.app.Application;
import android.webkit.WebView;

import com.ironsource.sdk.analytics.moat.MOATManager;
import com.ironsource.sdk.analytics.moat.MOATManager.EventsListener;

import org.json.JSONException;
import org.json.JSONObject;


public class MOATJSAdapter {
    private Application mApplication;
    private static final String initWithOptions = "initWithOptions";
    private static final String createAdTracker = "createAdTracker";
    private static final String startTracking = "startTracking";
    private static final String stopTracking = "stopTracking";
    private static final String moatFunction = "moatFunction";
    private static final String moatParams = "moatParams";
    private static final String success = "success";
    private static final String fail = "fail";

    public MOATJSAdapter(Application application) {
        this.mApplication = application;
    }


    void call(String params, IronSourceWebView.JSInterface.JSCallbackTask callback, WebView webView)
            throws Exception {
        FunctionCall fCall = fetchFunctionCall(params);

        if ("initWithOptions".equals(fCall.name)) {
            MOATManager.initWithOptions(fCall.params, this.mApplication);
        } else if (("createAdTracker".equals(fCall.name)) && (webView != null)) {
            MOATManager.createAdTracker(webView);
        } else if ("startTracking".equals(fCall.name)) {
            MOATManager.setEventListener(createEventListener(callback, fCall.successCallback, fCall.failCallback));
            MOATManager.startTracking();
        } else if ("stopTracking".equals(fCall.name)) {
            MOATManager.setEventListener(createEventListener(callback, fCall.successCallback, fCall.failCallback));
            MOATManager.stopTracking();
        }
    }

    private EventsListener createEventListener(final IronSourceWebView.JSInterface.JSCallbackTask callback, final String success, final String fail) {
        return new EventsListener() {
            public void onTrackingStarted(String s) {
                if (callback != null) {
                    callback.sendMessage(true, success, s);
                }
            }


            public void onTrackingFailedToStart(String s) {
                if (callback != null) {
                    callback.sendMessage(false, fail, s);
                }
            }

            public void onTrackingStopped(String s) {
                if (callback != null) {
                    callback.sendMessage(true, success, s);
                }
            }
        } ;
    }

    private FunctionCall fetchFunctionCall(String params) throws JSONException {
        JSONObject functionData = new JSONObject(params);

        FunctionCall res = new FunctionCall();
        res.name = functionData.optString("moatFunction");
        res.params = functionData.optJSONObject("moatParams");
        res.successCallback = functionData.optString("success");
        res.failCallback = functionData.optString("fail");

        return res;
    }

    private static class FunctionCall {
        String name;
        JSONObject params;
        String successCallback;
        String failCallback;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/MOATJSAdapter.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */