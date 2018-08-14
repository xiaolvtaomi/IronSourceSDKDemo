package com.ironsource.sdk.controller;

import android.os.Bundle;

import com.ironsource.sdk.utils.Logger;

public class InterstitialActivity
        extends ControllerActivity {
    private static final String TAG = ControllerActivity.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "onCreate");
    }

    protected void onResume() {
        super.onResume();
        Logger.i(TAG, "onResume");
    }

    protected void onPause() {
        super.onPause();
        Logger.i(TAG, "onPause");
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/InterstitialActivity.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */