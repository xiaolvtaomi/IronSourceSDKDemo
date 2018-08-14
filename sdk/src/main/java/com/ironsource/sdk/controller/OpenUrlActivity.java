package com.ironsource.sdk.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ironsource.sdk.agent.IronSourceAdsPublisherAgent;
import com.ironsource.sdk.utils.IronSourceAsyncHttpRequestTask;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;

import java.util.List;


public class OpenUrlActivity
        extends Activity {
    private static final String TAG = "OpenUrlActivity";
    private static final int WEB_VIEW_VIEW_ID = 123456789;
    private static final int PROGRESS_BAR_VIEW_ID = SDKUtils.generateViewId();

    private WebView webView = null;


    private IronSourceWebView mWebViewController;


    private ProgressBar mProgressBar;

    boolean isSecondaryWebview;

    private RelativeLayout mainLayout;

    private String mUrl;

    private Handler mUiThreadHandler = new Handler();


    private boolean mIsImmersive = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.i("OpenUrlActivity", "onCreate()");
        try {
            IronSourceAdsPublisherAgent ssaPubAgt = IronSourceAdsPublisherAgent.getInstance(this);
            this.mWebViewController = ssaPubAgt.getWebViewController();


            hideActivityTitle();


            hideActivtiyStatusBar();


            Bundle bundle = getIntent().getExtras();


            this.mUrl = bundle.getString(IronSourceWebView.EXTERNAL_URL);
            this.isSecondaryWebview = bundle.getBoolean(IronSourceWebView.SECONDARY_WEB_VIEW);

            Intent intent = getIntent();
            this.mIsImmersive = intent.getBooleanExtra("immersive", false);

            if (this.mIsImmersive) {
                getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & 0x1002) == 0) {
                            OpenUrlActivity.this.mUiThreadHandler.removeCallbacks(OpenUrlActivity.this.decorViewSettings);
                            OpenUrlActivity.this.mUiThreadHandler.postDelayed(OpenUrlActivity.this.decorViewSettings, 500L);
                        }
                    }
                });
                runOnUiThread(this.decorViewSettings);
            }


            this.mainLayout = new RelativeLayout(this);
            setContentView(this.mainLayout, new ViewGroup.LayoutParams(-1, -1));
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    protected void onResume() {
        super.onResume();
        createWebView();
    }


    private void createWebView() {
        if (this.webView == null) {
            this.webView = new WebView(getApplicationContext());
            this.webView.setId(WEB_VIEW_VIEW_ID);

            this.webView.getSettings().setJavaScriptEnabled(true);
            this.webView.setWebViewClient(new Client());

            loadUrl(this.mUrl);
        }


        if (findViewById(WEB_VIEW_VIEW_ID) == null) {
            RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(-1, -1);


            this.mainLayout.addView(this.webView, webViewLayoutParams);
        }


        createProgressBarForWebView();


        if (this.mWebViewController != null) {
            this.mWebViewController.viewableChange(true, "secondary");
        }
    }

    private void createProgressBarForWebView() {
        if (this.mProgressBar == null) {
            if (Build.VERSION.SDK_INT >= 11) {
                this.mProgressBar = new ProgressBar(new ContextThemeWrapper(this, 16973939));
            } else {
                this.mProgressBar = new ProgressBar(this);
            }
            this.mProgressBar.setId(PROGRESS_BAR_VIEW_ID);
        }


        if (findViewById(PROGRESS_BAR_VIEW_ID) == null) {
            RelativeLayout.LayoutParams progressBarLayoutParams = new RelativeLayout.LayoutParams(-2, -2);


            progressBarLayoutParams.addRule(13);

            this.mProgressBar.setLayoutParams(progressBarLayoutParams);
            this.mProgressBar.setVisibility(View.INVISIBLE);


            this.mainLayout.addView(this.mProgressBar);
        }
    }

    private void removeWebViewFromLayout() {
        if (this.mWebViewController != null) {
            this.mWebViewController.viewableChange(false, "secondary");

            if (this.mainLayout != null) {
                ViewGroup parent = (ViewGroup) this.webView.getParent();

                if (parent != null) {
                    if (parent.findViewById(WEB_VIEW_VIEW_ID) != null) {
                        parent.removeView(this.webView);
                    }

                    if (parent.findViewById(PROGRESS_BAR_VIEW_ID) != null) {
                        parent.removeView(this.mProgressBar);
                    }
                }
            }
        }
    }

    private void destroyWebView() {
        if (this.webView != null) {
            this.webView.destroy();
        }
    }

    protected void onPause() {
        super.onPause();
        removeWebViewFromLayout();
    }

    public void loadUrl(String url) {
        this.webView.stopLoading();
        this.webView.clearHistory();
        try {
            this.webView.loadUrl(url);
        } catch (Throwable e) {
            Logger.e("OpenUrlActivity", "OpenUrlActivity:: loadUrl: " + e.toString());
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
        }
    }

    private class Client extends WebViewClient {
        private Client() {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            OpenUrlActivity.this.mProgressBar.setVisibility(View.VISIBLE);
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            OpenUrlActivity.this.mProgressBar.setVisibility(View.INVISIBLE);
        }


        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            List<String> searchKeys = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getSearchKeys();

            if ((searchKeys != null) && (!searchKeys.isEmpty())) {
                for (String key : searchKeys) {
                    if (url.contains(key)) {
                        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        OpenUrlActivity.this.startActivity(intent);

                        OpenUrlActivity.this.mWebViewController.interceptedUrlToStore();

                        OpenUrlActivity.this.finish();
                        return true;
                    }
                }
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private void hideActivityTitle() {
        requestWindowFeature(1);
    }

    private void hideActivtiyStatusBar() {
        getWindow().setFlags(1024, 1024);
    }

    private void disableTouch() {
        getWindow().addFlags(16);
    }


    public void onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        destroyWebView();
    }


    public void finish() {
        if (this.isSecondaryWebview) {
            this.mWebViewController.engageEnd("secondaryClose");
        }

        super.finish();
    }


    private final Runnable decorViewSettings = new Runnable() {
        public void run() {
            OpenUrlActivity.this.getWindow().getDecorView().setSystemUiVisibility(SDKUtils.getActivityUIFlags(OpenUrlActivity.this.mIsImmersive));
        }
    };

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if ((this.mIsImmersive) && (hasFocus)) {
            runOnUiThread(this.decorViewSettings);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((this.mIsImmersive) && ((keyCode == 25) || (keyCode == 24))) {
            this.mUiThreadHandler.postDelayed(this.decorViewSettings, 500L);
        }
        return super.onKeyDown(keyCode, event);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/OpenUrlActivity.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */