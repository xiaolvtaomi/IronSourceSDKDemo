package com.ironsource.sdk.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.MutableContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.WebViewTransport;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.ironsource.environment.ApplicationContext;
import com.ironsource.environment.ConnectivityService;
import com.ironsource.environment.DeviceStatus;
import com.ironsource.environment.LocationService;
import com.ironsource.environment.LocationService.ISLocationListener;
import com.ironsource.environment.UrlHandler;
import com.ironsource.sdk.constants.Constants;
import com.ironsource.sdk.constants.Constants.JSMethods;
import com.ironsource.sdk.data.AdUnitsReady;
import com.ironsource.sdk.data.AdUnitsState;
import com.ironsource.sdk.data.DemandSource;
import com.ironsource.sdk.data.ProductParameters;
import com.ironsource.sdk.data.SSABCParameters;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ControllerState;
import com.ironsource.sdk.data.SSAEnums.DebugMode;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.data.SSAFile;
import com.ironsource.sdk.data.SSAObj;
import com.ironsource.sdk.listeners.OnGenericFunctionListener;
import com.ironsource.sdk.listeners.OnOfferWallListener;
import com.ironsource.sdk.listeners.OnWebViewChangeListener;
import com.ironsource.sdk.listeners.internals.DSAdProductListener;
import com.ironsource.sdk.listeners.internals.DSInterstitialListener;
import com.ironsource.sdk.listeners.internals.DSRewardedVideoListener;
import com.ironsource.sdk.precache.DownloadManager;
import com.ironsource.sdk.precache.DownloadManager.OnPreCacheCompletion;
import com.ironsource.sdk.utils.DeviceProperties;
import com.ironsource.sdk.utils.IronSourceAsyncHttpRequestTask;
import com.ironsource.sdk.utils.IronSourceSharedPrefHelper;
import com.ironsource.sdk.utils.IronSourceStorageUtils;
import com.ironsource.sdk.utils.Logger;
import com.ironsource.sdk.utils.SDKUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceWebView
        extends WebView
        implements OnPreCacheCompletion, DownloadListener {
    private String TAG = IronSourceWebView.class.getSimpleName();
    private String PUB_TAG = "IronSource";

    public static int mDebugMode = 0;

    private final String GENERIC_MESSAGE = "We're sorry, some error occurred. we will investigate it";

    private String mRVAppKey;

    private String mRVUserId;

    private String mOWAppKey;

    private String mOWUserId;

    private Map<String, String> mOWExtraParameters;

    private String mISAppKey;

    private String mISUserId;

    private String mOWCreditsAppKey;

    private String mOWCreditsUserId;
    public static String IS_STORE = "is_store";
    public static String IS_STORE_CLOSE = "is_store_close";
    public static String WEBVIEW_TYPE = "webview_type";
    public static String EXTERNAL_URL = "external_url";
    public static String SECONDARY_WEB_VIEW = "secondary_web_view";
    public static int DISPLAY_WEB_VIEW_INTENT = 0;
    public static int OPEN_URL_INTENT = 1;
    public static String APP_IDS = "appIds";
    public static String REQUEST_ID = "requestId";
    public static String IS_INSTALLED = "isInstalled";
    public static String RESULT = "result";

    private DownloadManager downloadManager;

    private boolean mOWmiss;

    private boolean mOWCreditsMiss;

    private boolean mGlobalControllerTimeFinish;

    private boolean isRemoveCloseEventHandler;

    private Uri mUri;
    private String mRequestParameters;
    private String mControllerKeyPressed = "interrupt";

    private CountDownTimer mCloseEventTimer;

    private CountDownTimer mLoadControllerTimer;

    private CountDownTimer mGlobalControllerTimer;
    private int mHiddenForceCloseWidth = 50;
    private int mHiddenForceCloseHeight = 50;
    private String mHiddenForceCloseLocation = "top-right";

    private ChromeClient mWebChromeClient;

    private View mCustomView;

    private FrameLayout mCustomViewContainer;

    private CustomViewCallback mCustomViewCallback;

    private FrameLayout mControllerLayout;

    private State mState;
    private String mOrientationState;
    private DSRewardedVideoListener mDSRewardedVideoListener;
    private OnGenericFunctionListener mOnGenericFunctionListener;
    private DSInterstitialListener mDSInterstitialListener;
    private OnOfferWallListener mOnOfferWallListener;
    private SSAEnums.ControllerState mControllerState = SSAEnums.ControllerState.None;

    private Boolean isKitkatAndAbove = null;


    private String mCacheDirectory;

    private VideoEventsListener mVideoEventsListener;

    private AdUnitsState mSavedState;

    private Object mSavedStateLocker = new Object();

    Context mCurrentActivityContext;

    Handler mUiHandler;
    private boolean mIsImmersive = false;
    private boolean mIsActivityThemeTranslucent = false;

    private DemandSourceManager mDemandSourceManager;

    private MOATJSAdapter mMoatJsAdapter;

    private PermissionsJSAdapter mPermissionsJsAdapter;
    private ArrayList<String> mControllerCommandsQueue;
    private ProductParametersCollection mProductParametersCollection = new ProductParametersCollection();

    private Map<String, String> getExtraParamsByProduct(SSAEnums.ProductType type) {
        if (type == SSAEnums.ProductType.OfferWall) {
            return this.mOWExtraParameters;
        }
        return null;
    }

    public IronSourceWebView(Context context, DemandSourceManager demandSourceManager) {
        super(context.getApplicationContext());
        Logger.i(this.TAG, "C'tor");
        this.mControllerCommandsQueue = new ArrayList();
        this.mCacheDirectory = initializeCacheDirectory(context.getApplicationContext());
        this.mCurrentActivityContext = context;
        this.mDemandSourceManager = demandSourceManager;
        initLayout(this.mCurrentActivityContext);

        this.mSavedState = new AdUnitsState();

        this.downloadManager = getDownloadManager();
        this.downloadManager.setOnPreCacheCompletion(this);

        this.mWebChromeClient = new ChromeClient();

        setWebViewClient(new ViewClient());
        setWebChromeClient(this.mWebChromeClient);

        setWebViewSettings();

        addJavascriptInterface(createJSInterface(context), "Android");

        setDownloadListener(this);

        setOnTouchListener(new SupersonicWebViewTouchListener());
        this.mUiHandler = createMainThreadHandler();
    }


    JSInterface createJSInterface(Context context) {
        return new JSInterface(context);
    }

    Handler createMainThreadHandler() {
        return new Handler(Looper.getMainLooper());
    }

    DownloadManager getDownloadManager() {
        return DownloadManager.getInstance(this.mCacheDirectory);
    }

    String initializeCacheDirectory(Context context) {
        return IronSourceStorageUtils.initializeCacheDirectory(context.getApplicationContext());
    }

    public void addMoatJSInterface(MOATJSAdapter moatjsAdapter) {
        this.mMoatJsAdapter = moatjsAdapter;
    }

    public void addPermissionsJSInterface(PermissionsJSAdapter permissionsJSAdapter) {
        this.mPermissionsJsAdapter = permissionsJSAdapter;
    }

    public void notifyLifeCycle(String productType, String event) {
        String params = parseToJson("lifeCycleEvent", event, "productType", productType, null, null, null, null, null, false);


        String script = generateJSToInject("onNativeLifeCycleEvent", params);
        injectJavascript(script);
    }

    private class SupersonicWebViewTouchListener implements OnTouchListener {
        private SupersonicWebViewTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == 1) {
                float xTouch = event.getX();
                float yTouch = event.getY();

                Logger.i(IronSourceWebView.this.TAG, "X:" + (int) xTouch + " Y:" + (int) yTouch);

                int width = DeviceStatus.getDeviceWidth();
                int height = DeviceStatus.getDeviceHeight();

                Logger.i(IronSourceWebView.this.TAG, "Width:" + width + " Height:" + height);


                int boundsTouchAreaX = SDKUtils.dpToPx(IronSourceWebView.this.mHiddenForceCloseWidth);
                int boundsTouchAreaY = SDKUtils.dpToPx(IronSourceWebView.this.mHiddenForceCloseHeight);
                int actualTouchX = 0;
                int actualTouchY = 0;

                if ("top-right".equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = width - (int) xTouch;
                    actualTouchY = (int) yTouch;
                } else if ("top-left".equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = (int) xTouch;
                    actualTouchY = (int) yTouch;
                } else if ("bottom-right".equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = width - (int) xTouch;
                    actualTouchY = height - (int) yTouch;
                } else if ("bottom-left".equalsIgnoreCase(IronSourceWebView.this.mHiddenForceCloseLocation)) {
                    actualTouchX = (int) xTouch;
                    actualTouchY = height - (int) yTouch;
                }

                if ((actualTouchX <= boundsTouchAreaX) && (actualTouchY <= boundsTouchAreaY)) {

                    IronSourceWebView.this.isRemoveCloseEventHandler = false;


                    if (IronSourceWebView.this.mCloseEventTimer != null) {
                        IronSourceWebView.this.mCloseEventTimer.cancel();
                    }

                    IronSourceWebView.this.mCloseEventTimer = new CountDownTimer(2000L, 500L) {
                        public void onTick(long millisUntilFinished) {
                            Logger.i(IronSourceWebView.this.TAG, "Close Event Timer Tick " + millisUntilFinished);
                        }

                        public void onFinish() {
                            Logger.i(IronSourceWebView.this.TAG, "Close Event Timer Finish");
                            if (IronSourceWebView.this.isRemoveCloseEventHandler) {
                                IronSourceWebView.this.isRemoveCloseEventHandler = false;
                            } else {
                                IronSourceWebView.this.engageEnd("forceClose");
                            }
                        }
                    }.start();
                }
            }

            return false;
        }
    }

    private void initLayout(Context context) {
        FrameLayout.LayoutParams coverScreenParams = new FrameLayout.LayoutParams(-1, -1);


        this.mControllerLayout = new FrameLayout(context);


        this.mCustomViewContainer = new FrameLayout(context);
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(-1, -1);
        this.mCustomViewContainer.setLayoutParams(fp);
        this.mCustomViewContainer.setVisibility(View.GONE);


        FrameLayout mContentView = new FrameLayout(context);
        FrameLayout.LayoutParams lpChild2 = new FrameLayout.LayoutParams(-1, -1);
        mContentView.setLayoutParams(lpChild2);
        mContentView.addView(this);

        this.mControllerLayout.addView(this.mCustomViewContainer, coverScreenParams);
        this.mControllerLayout.addView(mContentView);
    }

    private void setWebViewSettings() {
        WebSettings s = getSettings();

        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        if (Build.VERSION.SDK_INT >= 16) {
            try {
                getSettings().setAllowFileAccessFromFileURLs(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        s.setBuiltInZoomControls(false);

        s.setJavaScriptEnabled(true);

        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);


        s.setGeolocationEnabled(true);
        s.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");


        s.setDomStorageEnabled(true);

        try {
            setDisplayZoomControls(s);
            setMediaPlaybackJellyBean(s);
        } catch (Throwable e) {
            Logger.e(this.TAG, "setWebSettings - " + e.toString());
        }
    }

    private void setDisplayZoomControls(WebSettings s) {
        if (Build.VERSION.SDK_INT > 11) {
            s.setDisplayZoomControls(false);
        }
    }

    public WebBackForwardList saveState(Bundle outState) {
        return super.saveState(outState);
    }

    @SuppressLint({"NewApi"})
    private void setMediaPlaybackJellyBean(WebSettings s) {
        if (Build.VERSION.SDK_INT >= 17) {
            s.setMediaPlaybackRequiresUserGesture(false);
        }
    }

    @SuppressLint({"NewApi"})
    private void setWebDebuggingEnabled() {
        if (Build.VERSION.SDK_INT >= 19) {
            setWebContentsDebuggingEnabled(true);
        }
    }


    public void downloadController() {
        IronSourceStorageUtils.deleteFile(this.mCacheDirectory, "", "mobileController.html");
        String controllerPath = "";

        String controllerUrl = SDKUtils.getControllerUrl();
        SSAFile indexHtml = new SSAFile(controllerUrl, controllerPath);


        this.mGlobalControllerTimer = new CountDownTimer(200000L, 1000L) {

            public void onTick(long millisUntilFinished) {

                Logger.i(IronSourceWebView.this.TAG, "Global Controller Timer Tick " + millisUntilFinished);
            }

            public void onFinish() {
                Logger.i(IronSourceWebView.this.TAG, "Global Controller Timer Finish");
                IronSourceWebView.this.mGlobalControllerTimeFinish = true;
            }
        }.start();


        if (!this.downloadManager.isMobileControllerThreadLive()) {
            Logger.i(this.TAG, "Download Mobile Controller: " + controllerUrl);
            this.downloadManager.downloadMobileControllerFile(indexHtml);
        } else {
            Logger.i(this.TAG, "Download Mobile Controller: already alive");
        }
    }

    public void setDebugMode(int debugMode) {
        mDebugMode = debugMode;
    }


    public int getDebugMode() {
        return mDebugMode;
    }


    private boolean shouldNotifyDeveloper(String product) {
        boolean shouldNotify = false;


        if (TextUtils.isEmpty(product)) {
            Logger.d(this.TAG, "Trying to trigger a listener - no product was found");
            return false;
        }

        if (product.equalsIgnoreCase(SSAEnums.ProductType.Interstitial.toString())) {
            shouldNotify = this.mDSInterstitialListener != null;
        } else if (product.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
            shouldNotify = this.mDSRewardedVideoListener != null;
        } else if ((product.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) ||
                (product.equalsIgnoreCase(SSAEnums.ProductType.OfferWallCredits.toString()))) {
            shouldNotify = this.mOnOfferWallListener != null;
        }
        if (!shouldNotify) {
            Logger.d(this.TAG, "Trying to trigger a listener - no listener was found for product " + product);
        }
        return shouldNotify;
    }

    public void setOrientationState(String orientation) {
        this.mOrientationState = orientation;
    }

    public String getOrientationState() {
        return this.mOrientationState;
    }

    private class ViewClient extends WebViewClient {
        private ViewClient() {
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Logger.i("onPageStarted", url);
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            Logger.i("onPageFinished", url);

            if ((url.contains("adUnit")) || (url.contains("index.html"))) {
                IronSourceWebView.this.pageFinished();
            }
            super.onPageFinished(view, url);
        }


        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.i("onReceivedError", failingUrl + " " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }


        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.i("shouldOverrideUrlLoading", url);
            try {
                if (IronSourceWebView.this.handleSearchKeysURLs(url)) {
                    IronSourceWebView.this.interceptedUrlToStore();
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return super.shouldOverrideUrlLoading(view, url);
        }


        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Logger.i("shouldInterceptRequest", url);

            boolean mraidCall = false;
            try {
                URL mUrl = new URL(url);

                String file = mUrl.getFile();

                if (file.contains("mraid.js")) {
                    mraidCall = true;
                }
            } catch (MalformedURLException localMalformedURLException) {
            }


            if (mraidCall) {
                String filePath = "file://" + IronSourceWebView.this.mCacheDirectory + File.separator + "mraid.js";

                File mraidFile = new File(filePath);

                try {
                    FileInputStream fis = new FileInputStream(mraidFile);

                    return new WebResourceResponse("text/javascript", "UTF-8", getClass().getResourceAsStream(filePath));
                } catch (FileNotFoundException localFileNotFoundException) {
                }
            }

            return super.shouldInterceptRequest(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        private ChromeClient() {
        }

        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            WebView childView = new WebView(view.getContext());
            childView.setWebChromeClient(this);
            childView.setWebViewClient(new FrameBustWebViewClient());
            WebViewTransport transport = (WebViewTransport)resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            Logger.i("onCreateWindow", "onCreateWindow");
            return true;
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Logger.i("MyApplication", consoleMessage.message() + " -- From line " + consoleMessage
                    .lineNumber() + " of " + consoleMessage
                    .sourceId());

            return true;
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            Logger.i("Test", "onShowCustomView");

            IronSourceWebView.this.setVisibility(View.GONE);


            if (IronSourceWebView.this.mCustomView != null) {
                Logger.i("Test", "mCustomView != null");
                callback.onCustomViewHidden();
                return;
            }
            Logger.i("Test", "mCustomView == null");


            IronSourceWebView.this.mCustomViewContainer.addView(view);
            IronSourceWebView.this.mCustomView = view;
            IronSourceWebView.this.mCustomViewCallback = callback;
            IronSourceWebView.this.mCustomViewContainer.setVisibility(View.VISIBLE);
        }

        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(IronSourceWebView.this.getCurrentActivityContext());
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

            return frameLayout;
        }


        public void onHideCustomView() {
            Logger.i("Test", "onHideCustomView");

            if (IronSourceWebView.this.mCustomView == null) {
                return;
            }

            IronSourceWebView.this.mCustomView.setVisibility(View.GONE);


            IronSourceWebView.this.mCustomViewContainer.removeView(IronSourceWebView.this.mCustomView);
            IronSourceWebView.this.mCustomView = null;
            IronSourceWebView.this.mCustomViewContainer.setVisibility(View.GONE);
            IronSourceWebView.this.mCustomViewCallback.onCustomViewHidden();

            IronSourceWebView.this.setVisibility(View.VISIBLE);
        }
    }

    private class FrameBustWebViewClient extends WebViewClient {
        private FrameBustWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Context ctx = IronSourceWebView.this.getCurrentActivityContext();

            Intent intent = new Intent(ctx, OpenUrlActivity.class);
            intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
            intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, false);
            ctx.startActivity(intent);

            return true;
        }
    }


    public class JSInterface {
        volatile int udiaResults = 0;

        public JSInterface(Context context) {
        }

        @JavascriptInterface
        public void initController(String value) {
            Logger.i(IronSourceWebView.this.TAG, "initController(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            if (ssaObj.containsKey("stage")) {
                String stage = ssaObj.getString("stage");

                if ("ready".equalsIgnoreCase(stage)) {
                    handleControllerStageReady();
                } else if ("loaded".equalsIgnoreCase(stage)) {
                    handleControllerStageLoaded();
                } else if ("failed".equalsIgnoreCase(stage)) {
                    handleControllerStageFailed();
                } else {
                    Logger.i(IronSourceWebView.this.TAG, "No STAGE mentioned! Should not get here!");
                }


                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (Build.VERSION.SDK_INT >= 16) {
                            try {
                                IronSourceWebView.this.getSettings().setAllowFileAccessFromFileURLs(false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

        void handleControllerStageLoaded() {
            IronSourceWebView.this.mControllerState = SSAEnums.ControllerState.Loaded;
        }

        void handleControllerStageReady() {
            IronSourceWebView.this.mControllerState = SSAEnums.ControllerState.Ready;
            IronSourceWebView.this.mGlobalControllerTimer.cancel();
            IronSourceWebView.this.mLoadControllerTimer.cancel();
            IronSourceWebView.this.invokePendingCommands();
            Collection<DemandSource> demandSourcesRV = IronSourceWebView.this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.RewardedVideo);

            Iterator localIterator = demandSourcesRV.iterator();

            while(localIterator.hasNext()) {
                DemandSource demandSource = (DemandSource)localIterator.next();
                if(demandSource.getDemandSourceInitState() == 1) {
                    IronSourceWebView.this.initRewardedVideo(IronSourceWebView.this.mRVAppKey, IronSourceWebView.this.mRVUserId, demandSource, IronSourceWebView.this.mDSRewardedVideoListener);
                }
            }

            Collection<DemandSource> demandSourcesIS = IronSourceWebView.this.mDemandSourceManager.getDemandSources(ProductType.Interstitial);
            Iterator var6 = demandSourcesIS.iterator();

            while(var6.hasNext()) {
                DemandSource demandSourcex = (DemandSource)var6.next();
                if(demandSourcex.getDemandSourceInitState() == 1) {
                    IronSourceWebView.this.initInterstitial(IronSourceWebView.this.mISAppKey, IronSourceWebView.this.mISUserId, demandSourcex, IronSourceWebView.this.mDSInterstitialListener);
                }
            }

            if (IronSourceWebView.this.mOWmiss) {
                IronSourceWebView.this.initOfferWall(IronSourceWebView.this.mOWAppKey, IronSourceWebView.this.mOWUserId, IronSourceWebView.this.mOWExtraParameters, IronSourceWebView.this.mOnOfferWallListener);
            }

            if (IronSourceWebView.this.mOWCreditsMiss) {
                IronSourceWebView.this.getOfferWallCredits(IronSourceWebView.this.mOWCreditsAppKey, IronSourceWebView.this.mOWCreditsUserId, IronSourceWebView.this.mOnOfferWallListener);
            }

            IronSourceWebView.this.restoreState(IronSourceWebView.this.mSavedState);
        }

        void handleControllerStageFailed() {
            IronSourceWebView.this.mControllerState = SSAEnums.ControllerState.Failed;

            Collection<DemandSource> demandSources = IronSourceWebView.this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.RewardedVideo);
            for (DemandSource demandSource : demandSources) {
                if (demandSource.getDemandSourceInitState() == 1) {
                    IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.RewardedVideo, demandSource.getDemandSourceName());
                }
            }

            demandSources = IronSourceWebView.this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.Interstitial);
            for (DemandSource demandSource : demandSources) {
                if (demandSource.getDemandSourceInitState() == 1) {
                    IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.Interstitial, demandSource.getDemandSourceName());
                }
            }

            if (IronSourceWebView.this.mOWmiss) {
                IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.OfferWall, null);
            }

            if (IronSourceWebView.this.mOWCreditsMiss) {
                IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.OfferWallCredits, null);
            }
        }


        @JavascriptInterface
        public void alert(String message) {
        }

        @JavascriptInterface
        public void getDeviceStatus(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getDeviceStatus(" + value + ")");

            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);

            Object[] resultArr = new Object[2];
            resultArr = IronSourceWebView.this.getDeviceParams(IronSourceWebView.this.getContext());

            String params = (String) resultArr[0];
            boolean failed = ((Boolean) resultArr[1]).booleanValue();

            String funToCall = null;

            if (failed) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }


            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "onGetDeviceStatusSuccess", "onGetDeviceStatusFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void setMixedContentAlwaysAllow(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setMixedContentAlwaysAllow(" + value + ")");
            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (Build.VERSION.SDK_INT >= 21) {
                        IronSourceWebView.this.getSettings().setMixedContentMode(0);
                    }
                }
            });
        }

        @JavascriptInterface
        public void setAllowFileAccessFromFileURLs(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setAllowFileAccessFromFileURLs(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final boolean allow = ssaObj.getBoolean("allowFileAccess");

            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (Build.VERSION.SDK_INT >= 16) {
                        try {
                            IronSourceWebView.this.getSettings().setAllowFileAccessFromFileURLs(allow);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void getControllerConfig(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getControllerConfig(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String successFunToCall = ssaObj.getString(IronSourceWebView.JSON_KEY_SUCCESS);
            if (!TextUtils.isEmpty(successFunToCall)) {
                String params = SDKUtils.getControllerConfig();
                String testerParameters = SDKUtils.getTesterParameters();
                if (areTesterParametersValid(testerParameters)) {
                    try {
                        params = addTesterParametersToConfig(params, testerParameters);
                    } catch (JSONException jsonException) {
                        Logger.d(IronSourceWebView.this.TAG, "getControllerConfig Error while parsing Tester AB Group parameters");
                    }
                }
                String script = IronSourceWebView.this.generateJSToInject(successFunToCall, params);
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public String addTesterParametersToConfig(String originalConfiguration, String testerParameters) throws JSONException {
            JSONObject config = new JSONObject(originalConfiguration);
            JSONObject testerParams = new JSONObject(testerParameters);
            config.putOpt("testerABGroup", testerParams.get("testerABGroup"));
            config.putOpt("testFriendlyName", testerParams.get("testFriendlyName"));

            return config.toString();
        }

        @JavascriptInterface
        public boolean areTesterParametersValid(String testerParameters) {
            if ((!TextUtils.isEmpty(testerParameters)) && (!testerParameters.contains("-1"))) {
                try {
                    JSONObject testerParams = new JSONObject(testerParameters);
                    if ((!testerParams.getString("testerABGroup").isEmpty()) &&
                            (!testerParams.getString("testFriendlyName").isEmpty())) {
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @JavascriptInterface
        public void getApplicationInfo(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getApplicationInfo(" + value + ")");

            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);


            SSAObj ssaObj = new SSAObj(value);
            String product = ssaObj.getString("productType");
            String demandSourceName = ssaObj.getString("demandSourceName");

            String funToCall = null;

            Object[] resultArr = new Object[2];

            resultArr = IronSourceWebView.this.getApplicationParams(product, demandSourceName);

            String params = (String) resultArr[0];
            boolean failed = ((Boolean) resultArr[1]).booleanValue();

            if (failed) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }


            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "onGetApplicationInfoSuccess", "onGetApplicationInfoFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void checkInstalledApps(String value) {
            Logger.i(IronSourceWebView.this.TAG, "checkInstalledApps(" + value + ")");

            String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);

            String funToCall = null;


            SSAObj ssaObj = new SSAObj(value);
            String appIdsString = ssaObj.getString(IronSourceWebView.APP_IDS);
            String requestIdString = ssaObj.getString(IronSourceWebView.REQUEST_ID);


            Object[] resultArr = IronSourceWebView.this.getAppsStatus(appIdsString, requestIdString);

            String params = (String) resultArr[0];
            boolean failed = ((Boolean) resultArr[1]).booleanValue();

            if (failed) {
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else if (!TextUtils.isEmpty(successFunToCall)) {
                funToCall = successFunToCall;
            }


            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "onCheckInstalledAppsSuccess", "onCheckInstalledAppsFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void saveFile(String value) {
            Logger.i(IronSourceWebView.this.TAG, "saveFile(" + value + ")");

            SSAFile ssaFile = new SSAFile(value);

            if (DeviceStatus.getAvailableMemorySizeInMegaBytes(IronSourceWebView.this.mCacheDirectory) <= 0L) {
                IronSourceWebView.this.responseBack(value, false, "no_disk_space", null);
                return;
            }

            if (!SDKUtils.isExternalStorageAvailable()) {
                IronSourceWebView.this.responseBack(value, false, "sotrage_unavailable", null);
                return;
            }

            if (IronSourceStorageUtils.isFileCached(IronSourceWebView.this.mCacheDirectory, ssaFile)) {
                IronSourceWebView.this.responseBack(value, false, "file_already_exist", null);
                return;
            }

            if (!ConnectivityService.isConnected(IronSourceWebView.this.getContext())) {
                IronSourceWebView.this.responseBack(value, false, "no_network_connection", null);
                return;
            }

            IronSourceWebView.this.responseBack(value, true, null, null);

            Object lastUpdateTimeObj = ssaFile.getLastUpdateTime();

            if (lastUpdateTimeObj != null) {
                String lastUpdateTimeStr = String.valueOf(lastUpdateTimeObj);


                if (!TextUtils.isEmpty(lastUpdateTimeStr)) {

                    String path = ssaFile.getPath();
                    String folder;
                    if (path.contains("/")) {
                        String[] splitArr = ssaFile.getPath().split("/");
                        folder = splitArr[(splitArr.length - 1)];
                    } else {
                        folder = path;
                    }

                    IronSourceSharedPrefHelper.getSupersonicPrefHelper().setCampaignLastUpdate(folder, lastUpdateTimeStr);
                }
            }

            IronSourceWebView.this.downloadManager.downloadFile(ssaFile);
        }

        @JavascriptInterface
        public void adUnitsReady(String value) {
            Logger.i(IronSourceWebView.this.TAG, "adUnitsReady(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String demandSourceName = ssaObj.getString("demandSourceName");

            final AdUnitsReady adUnitsReady = new AdUnitsReady(value);

            if (!adUnitsReady.isNumOfAdUnitsExist()) {
                IronSourceWebView.this.responseBack(value, false, "Num Of Ad Units Do Not Exist", null);
                return;
            }

            IronSourceWebView.this.responseBack(value, true, null, null);

            final String product = adUnitsReady.getProductType();

            if (IronSourceWebView.this.shouldNotifyDeveloper(product)) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        int adUnits = Integer.parseInt(adUnitsReady.getNumOfAdUnits());

                        if (product.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
                            if (adUnits > 0) {
                                Log.d(IronSourceWebView.this.TAG, "onRVInitSuccess()");
                                IronSourceWebView.this.mDSRewardedVideoListener.onAdProductInitSuccess(SSAEnums.ProductType.RewardedVideo, demandSourceName, adUnitsReady);
                            } else {
                                IronSourceWebView.this.mDSRewardedVideoListener.onRVNoMoreOffers(demandSourceName);
                            }
                        }
                    }
                });
            }
        }


        @JavascriptInterface
        public void deleteFolder(String value) {
            Logger.i(IronSourceWebView.this.TAG, "deleteFolder(" + value + ")");

            SSAFile file = new SSAFile(value);

            if (!IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, file.getPath())) {
                IronSourceWebView.this.responseBack(value, false, "Folder not exist", "1");
            } else {
                boolean result = IronSourceStorageUtils.deleteFolder(IronSourceWebView.this.mCacheDirectory, file.getPath());
                IronSourceWebView.this.responseBack(value, result, null, null);
            }
        }

        @JavascriptInterface
        public void deleteFile(String value) {
            Logger.i(IronSourceWebView.this.TAG, "deleteFile(" + value + ")");

            SSAFile file = new SSAFile(value);

            if (!IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, file.getPath())) {
                IronSourceWebView.this.responseBack(value, false, "File not exist", "1");
            } else {
                boolean result = IronSourceStorageUtils.deleteFile(IronSourceWebView.this.mCacheDirectory, file.getPath(), file.getFile());
                IronSourceWebView.this.responseBack(value, result, null, null);
            }
        }

        @JavascriptInterface
        public void displayWebView(String value) {
            Logger.i(IronSourceWebView.this.TAG, "displayWebView(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            SSAObj ssaObj = new SSAObj(value);
            boolean display = ((Boolean) ssaObj.get("display")).booleanValue();
            String productType = ssaObj.getString("productType");
            boolean isStandaloneView = ssaObj.getBoolean("standaloneView");
            String demandSourceName = ssaObj.getString("demandSourceName");
            boolean isRewardedVideo = false;

            if (display) {
                IronSourceWebView.this.mIsImmersive = ssaObj.getBoolean("immersive");
                IronSourceWebView.this.mIsActivityThemeTranslucent = ssaObj.getBoolean("activityThemeTranslucent");

                if (IronSourceWebView.this.getState() != State.Display) {
                    IronSourceWebView.this.setState(State.Display);

                    Logger.i(IronSourceWebView.this.TAG, "State: " + IronSourceWebView.this.mState);

                    Context context = IronSourceWebView.this.getCurrentActivityContext();

                    String orientation = IronSourceWebView.this.getOrientationState();
                    int rotation = DeviceStatus.getApplicationRotation(context);


                    if (isStandaloneView) {
                        ControllerView controllerView = new ControllerView(context);
                        controllerView.addView(IronSourceWebView.this.mControllerLayout);
                        controllerView.showInterstitial(IronSourceWebView.this);
                    } else {
                        Intent intent;
                        if (IronSourceWebView.this.mIsActivityThemeTranslucent) {
                            intent = new Intent(context, InterstitialActivity.class);
                        } else {
                            intent = new Intent(context, ControllerActivity.class);
                        }

                        if (SSAEnums.ProductType.RewardedVideo.toString().equalsIgnoreCase(productType)) {
                            if ("application".equals(orientation)) {
                                orientation = SDKUtils.translateRequestedOrientation(DeviceStatus.getActivityRequestedOrientation(IronSourceWebView.this.getCurrentActivityContext()));
                            }

                            isRewardedVideo = true;

                            intent.putExtra("productType", SSAEnums.ProductType.RewardedVideo.toString());


                            IronSourceWebView.this.mSavedState.adOpened(SSAEnums.ProductType.RewardedVideo.ordinal());
                            IronSourceWebView.this.mSavedState.setDisplayedDemandSourceName(demandSourceName);
                        } else if (SSAEnums.ProductType.OfferWall.toString().equalsIgnoreCase(productType)) {
                            intent.putExtra("productType", SSAEnums.ProductType.OfferWall.toString());

                            IronSourceWebView.this.mSavedState.adOpened(SSAEnums.ProductType.OfferWall.ordinal());
                        } else if ((SSAEnums.ProductType.Interstitial.toString().equalsIgnoreCase(productType)) &&
                                ("application".equals(orientation))) {
                            orientation = SDKUtils.translateRequestedOrientation(DeviceStatus.getActivityRequestedOrientation(IronSourceWebView.this.getCurrentActivityContext()));
                        }


                        if ((isRewardedVideo) && (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.RewardedVideo.toString()))) {
                            IronSourceWebView.this.mDSRewardedVideoListener.onAdProductOpen(SSAEnums.ProductType.RewardedVideo, demandSourceName);
                        }

                        intent.setFlags(536870912);
                        intent.putExtra("immersive", IronSourceWebView.this.mIsImmersive);
                        intent.putExtra("orientation_set_flag", orientation);
                        intent.putExtra("rotation_set_flag", rotation);

                        context.startActivity(intent);
                    }
                } else {
                    Logger.i(IronSourceWebView.this.TAG, "State: " + IronSourceWebView.this.mState);
                }
            } else {
                IronSourceWebView.this.setState(State.Gone);
                IronSourceWebView.this.closeWebView();
            }
        }

        @JavascriptInterface
        public void getOrientation(String value) {
            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String params = SDKUtils.getOrientation(IronSourceWebView.this.getCurrentActivityContext()).toString();

            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "onGetOrientationSuccess", "onGetOrientationFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void setOrientation(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setOrientation(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String orientation = ssaObj.getString("orientation");

            IronSourceWebView.this.setOrientationState(orientation);
            int rotation = DeviceStatus.getApplicationRotation(IronSourceWebView.this.getCurrentActivityContext());

            if (IronSourceWebView.this.mChangeListener != null) {
                IronSourceWebView.this.mChangeListener.onOrientationChanged(orientation, rotation);
            }
        }

        @JavascriptInterface
        public void getCachedFilesMap(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getCachedFilesMap(" + value + ")");

            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);

            if (!TextUtils.isEmpty(funToCall)) {
                SSAObj ssaObj = new SSAObj(value);


                if (!ssaObj.containsKey("path")) {
                    IronSourceWebView.this.responseBack(value, false, "path key does not exist", null);
                    return;
                }

                String mapPath = (String) ssaObj.get("path");


                if (!IronSourceStorageUtils.isPathExist(IronSourceWebView.this.mCacheDirectory, mapPath)) {
                    IronSourceWebView.this.responseBack(value, false, "path file does not exist on disk", null);
                    return;
                }

                String fileSystmeMap = IronSourceStorageUtils.getCachedFilesMap(IronSourceWebView.this.mCacheDirectory, mapPath);
                String script = IronSourceWebView.this.generateJSToInject(funToCall, fileSystmeMap, "onGetCachedFilesMapSuccess", "onGetCachedFilesMapFail");

                IronSourceWebView.this.injectJavascript(script);
            }
        }

        private void callJavaScriptFunction(String funToCall, String params) {
            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, params);
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void getDemandSourceState(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getMediationState(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String demandSourceName = ssaObj.getString("demandSourceName");
            String product = ssaObj.getString("productType");
            try {
                if ((product != null) && (demandSourceName != null)) {
                    SSAEnums.ProductType productType = SDKUtils.getProductType(product);
                    if (productType != null) {
                        DemandSource demandSource = IronSourceWebView.this.mDemandSourceManager.getDemandSourceByName(productType, demandSourceName);
                        String funToCall = null;
                        JSONObject obj = new JSONObject();
                        obj.put("productType", product);
                        obj.put("demandSourceName", demandSourceName);
                        if ((demandSource != null) && (!demandSource.isMediationState(-1))) {
                            funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                            obj.put("state", demandSource.getMediationState());
                        } else {
                            funToCall = IronSourceWebView.this.extractFailFunctionToCall(value);
                        }
                        callJavaScriptFunction(funToCall, obj.toString());
                    }
                }
            } catch (Exception ex) {
                IronSourceWebView.this.responseBack(value, false, ex.getMessage(), null);
                ex.printStackTrace();
            }
        }

        @JavascriptInterface
        public void adCredited(final String value) {
            Log.d(IronSourceWebView.this.PUB_TAG, "adCredited(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String creditsStr = ssaObj.getString("credits");
            final int credits = creditsStr != null ? Integer.parseInt(creditsStr) : 0;

            String totalCreditsStr = ssaObj.getString("total");
            final int totalCredits = totalCreditsStr != null ? Integer.parseInt(totalCreditsStr) : 0;

            final String demandSourceName = ssaObj.getString("demandSourceName");

            final String product = ssaObj.getString("productType");
            boolean isExternalPoll = ssaObj.getBoolean("externalPoll");


            boolean totalCreditsFlag = false;
            String latestCompeltionsTime = null;
            boolean md5Signature = false;

            final String appKey;
            final String userId;
            if (isExternalPoll) {
                appKey = IronSourceWebView.this.mOWCreditsAppKey;
                userId = IronSourceWebView.this.mOWCreditsUserId;
            } else {
                appKey = IronSourceWebView.this.mOWAppKey;
                userId = IronSourceWebView.this.mOWUserId;
            }

            if (product.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {

                if ((ssaObj.isNull("signature")) || (ssaObj.isNull("timestamp")) || (ssaObj.isNull("totalCreditsFlag"))) {
                    IronSourceWebView.this.responseBack(value, false, "One of the keys are missing: signature/timestamp/totalCreditsFlag", null);
                    return;
                }


                String controllerMD5 = ssaObj.getString("signature");


                StringBuilder sig = new StringBuilder().append(totalCreditsStr).append(appKey).append(userId);

                String localMD5 = SDKUtils.getMD5(sig.toString());

                if (controllerMD5.equalsIgnoreCase(localMD5)) {
                    md5Signature = true;
                } else {
                    IronSourceWebView.this.responseBack(value, false, "Controller signature is not equal to SDK signature", null);
                }


                totalCreditsFlag = ssaObj.getBoolean("totalCreditsFlag");


                latestCompeltionsTime = ssaObj.getString("timestamp");
            }

            if (IronSourceWebView.this.shouldNotifyDeveloper(product)) {
                final boolean mTotalCreditsFlag = totalCreditsFlag;
                final String mlatestCompeltionsTime = latestCompeltionsTime;
                final boolean mMd5Signature = md5Signature;

                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        if (product.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString())) {
                            IronSourceWebView.this.mDSRewardedVideoListener.onRVAdCredited(demandSourceName, credits);
                        } else if (product.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {
                            if (mMd5Signature) {
                                if (IronSourceWebView.this.mOnOfferWallListener.onOWAdCredited(credits, totalCredits, mTotalCreditsFlag)) {
                                    if (!TextUtils.isEmpty(mlatestCompeltionsTime)) {
                                        boolean result = IronSourceSharedPrefHelper.getSupersonicPrefHelper().setLatestCompeltionsTime(mlatestCompeltionsTime, appKey, userId);

                                        if (result) {
                                            IronSourceWebView.this.responseBack(value, true, null, null);
                                        } else {
                                            IronSourceWebView.this.responseBack(value, false, "Time Stamp could not be stored", null);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        @JavascriptInterface
        public void removeCloseEventHandler(String value) {
            Logger.i(IronSourceWebView.this.TAG, "removeCloseEventHandler(" + value + ")");

            if (IronSourceWebView.this.mCloseEventTimer != null) {
                IronSourceWebView.this.mCloseEventTimer.cancel();
            }

            IronSourceWebView.this.isRemoveCloseEventHandler = true;
        }

        @JavascriptInterface
        public void onGetDeviceStatusSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetDeviceStatusSuccess(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            IronSourceWebView.this.toastingErrMsg("onGetDeviceStatusSuccess", value);
        }

        @JavascriptInterface
        public void onGetDeviceStatusFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetDeviceStatusFail(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            IronSourceWebView.this.toastingErrMsg("onGetDeviceStatusFail", value);
        }

        @JavascriptInterface
        public void onInitRewardedVideoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitRewardedVideoSuccess(" + value + ")");

            SSABCParameters ssaBCParameters = new SSABCParameters(value);
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setSSABCParameters(ssaBCParameters);

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onInitRewardedVideoSuccess", value);
        }

        @JavascriptInterface
        public void onInitRewardedVideoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitRewardedVideoFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");

            final String demandSourceName = ssaObj.getString("demandSourceName");

            DemandSource demandSource = IronSourceWebView.this.mDemandSourceManager.getDemandSourceByName(SSAEnums.ProductType.RewardedVideo, demandSourceName);
            if (demandSource != null) {
                demandSource.setDemandSourceInitState(3);
            }

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.RewardedVideo.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        Log.d(IronSourceWebView.this.TAG, "onRVInitFail(message:" + toSend + ")");
                        IronSourceWebView.this.mDSRewardedVideoListener.onAdProductInitFailed(SSAEnums.ProductType.RewardedVideo, demandSourceName, toSend);
                    }
                });
            }


            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onInitRewardedVideoFail", value);
        }

        @JavascriptInterface
        public void onGetApplicationInfoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetApplicationInfoSuccess(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onGetApplicationInfoSuccess", value);
        }

        @JavascriptInterface
        public void onGetApplicationInfoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetApplicationInfoFail(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onGetApplicationInfoFail", value);
        }

        @JavascriptInterface
        public void onShowRewardedVideoSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowRewardedVideoSuccess(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onShowRewardedVideoSuccess", value);
        }

        @JavascriptInterface
        public void onShowRewardedVideoFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowRewardedVideoFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");
            final String demandSourceName = ssaObj.getString("demandSourceName");

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.RewardedVideo.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        Log.d(IronSourceWebView.this.TAG, "onRVShowFail(message:" + message + ")");
                        IronSourceWebView.this.mDSRewardedVideoListener.onRVShowFail(demandSourceName, toSend);
                    }
                });
            }


            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onShowRewardedVideoFail", value);
        }

        @JavascriptInterface
        public void onGetCachedFilesMapSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetCachedFilesMapSuccess(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            IronSourceWebView.this.toastingErrMsg("onGetCachedFilesMapSuccess", value);
        }

        @JavascriptInterface
        public void onGetCachedFilesMapFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetCachedFilesMapFail(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            IronSourceWebView.this.toastingErrMsg("onGetCachedFilesMapFail", value);
        }


        @JavascriptInterface
        public void onShowOfferWallSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowOfferWallSuccess(" + value + ")");
            IronSourceWebView.this.mSavedState.adOpened(SSAEnums.ProductType.OfferWall.ordinal());

            final String placementId = SDKUtils.getValueFromJsonObject(value, "placementId");

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mOnOfferWallListener.onOWShowSuccess(placementId);
                    }
                });
            }

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onShowOfferWallSuccess", value);
        }

        @JavascriptInterface
        public void onShowOfferWallFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowOfferWallFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnOfferWallListener.onOWShowFail(toSend);
                    }
                });
            }

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onShowOfferWallFail", value);
        }


        @JavascriptInterface
        public void onInitInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialSuccess()");

            IronSourceWebView.this.toastingErrMsg("onInitInterstitialSuccess", "true");
            SSAObj ssaObj = new SSAObj(value);
            final String demandSourceName = ssaObj.getString("demandSourceName");

            if (TextUtils.isEmpty(demandSourceName)) {
                Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialSuccess failed with no demand source");
                return;
            }

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(IronSourceWebView.this.TAG, "onInterstitialInitSuccess()");
                        IronSourceWebView.this.mDSInterstitialListener.onAdProductInitSuccess(SSAEnums.ProductType.Interstitial, demandSourceName, null);
                    }
                });
            }
        }


        @JavascriptInterface
        public void onInitInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");
            final String demandSourceName = ssaObj.getString("demandSourceName");

            if (TextUtils.isEmpty(demandSourceName)) {
                Logger.i(IronSourceWebView.this.TAG, "onInitInterstitialSuccess failed with no demand source");
                return;
            }
            DemandSource demandSource = IronSourceWebView.this.mDemandSourceManager.getDemandSourceByName(SSAEnums.ProductType.Interstitial, demandSourceName);

            if (demandSource != null) {
                demandSource.setDemandSourceInitState(3);
            }

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        Log.d(IronSourceWebView.this.TAG, "onInterstitialInitFail(message:" + toSend + ")");
                        IronSourceWebView.this.mDSInterstitialListener.onAdProductInitFailed(SSAEnums.ProductType.Interstitial, demandSourceName, toSend);
                    }
                });
            }


            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onInitInterstitialFail", value);
        }

        private void setInterstitialAvailability(String demandSourceName, boolean isAvailable) {
            DemandSource demandSource = IronSourceWebView.this.mDemandSourceManager.getDemandSourceByName(SSAEnums.ProductType.Interstitial, demandSourceName);
            if (demandSource != null) {
                demandSource.setAvailabilityState(isAvailable);
            }


            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.toastingErrMsg("onInterstitialAvailability", String.valueOf(isAvailable + " with demand " + demandSourceName));
            }
        }

        @JavascriptInterface
        public void adClicked(String value) {
            Logger.i(IronSourceWebView.this.TAG, "adClicked(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String productType = ssaObj.getString("productType");
            final String demandSourceName = ssaObj.getString("demandSourceName");
            if (TextUtils.isEmpty(demandSourceName)) {
                return;
            }

            final SSAEnums.ProductType eProductType = IronSourceWebView.this.getStringProductTypeAsEnum(productType);
            final DSAdProductListener listener = IronSourceWebView.this.getAdProductListenerByProductType(eProductType);

            if ((eProductType != null) && (listener != null)) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        listener.onAdProductClick(eProductType, demandSourceName);
                    }
                });
            }
        }

        @JavascriptInterface
        public void onShowInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowInterstitialSuccess(" + value + ")");

            IronSourceWebView.this.responseBack(value, true, null, null);

            SSAObj ssaObj = new SSAObj(value);
            final String demandSourceName = ssaObj.getString("demandSourceName");

            if (TextUtils.isEmpty(demandSourceName)) {
                Logger.i(IronSourceWebView.this.TAG, "onShowInterstitialSuccess called with no demand");
                return;
            }

            IronSourceWebView.this.mSavedState.adOpened(SSAEnums.ProductType.Interstitial.ordinal());
            IronSourceWebView.this.mSavedState.setDisplayedDemandSourceName(demandSourceName);

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mDSInterstitialListener.onAdProductOpen(SSAEnums.ProductType.Interstitial, demandSourceName);
                        IronSourceWebView.this.mDSInterstitialListener.onInterstitialShowSuccess(demandSourceName);
                    }
                });
                IronSourceWebView.this.toastingErrMsg("onShowInterstitialSuccess", value);
            }
            setInterstitialAvailability(demandSourceName, false);
        }

        @JavascriptInterface
        public void onInitOfferWallSuccess(String value) {
            IronSourceWebView.this.toastingErrMsg("onInitOfferWallSuccess", "true");

            IronSourceWebView.this.mSavedState.setOfferwallInitSuccess(true);


            if (IronSourceWebView.this.mSavedState.reportInitOfferwall()) {
                IronSourceWebView.this.mSavedState.setOfferwallReportInit(false);
                if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d(IronSourceWebView.this.TAG, "onOfferWallInitSuccess()");
                            IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitSuccess();
                        }
                    });
                }
            }
        }

        @JavascriptInterface
        public void onInitOfferWallFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onInitOfferWallFail(" + value + ")");


            IronSourceWebView.this.mSavedState.setOfferwallInitSuccess(false);

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");


            if (IronSourceWebView.this.mSavedState.reportInitOfferwall()) {

                IronSourceWebView.this.mSavedState.setOfferwallReportInit(false);

                if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString())) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            String toSend = message;
                            if (toSend == null) {
                                toSend = "We're sorry, some error occurred. we will investigate it";
                            }
                            Log.d(IronSourceWebView.this.TAG, "onOfferWallInitFail(message:" + toSend + ")");
                            IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitFail(toSend);
                        }
                    });
                }
            }


            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onInitOfferWallFail", value);
        }

        @JavascriptInterface
        public void onLoadInterstitialSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onLoadInterstitialSuccess(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String demandSourceName = ssaObj.getString("demandSourceName");

            setInterstitialAvailability(demandSourceName, true);
            IronSourceWebView.this.responseBack(value, true, null, null);


            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        IronSourceWebView.this.mDSInterstitialListener.onInterstitialLoadSuccess(demandSourceName);
                    }
                });
            }


            IronSourceWebView.this.toastingErrMsg("onLoadInterstitialSuccess", "true");
        }

        @JavascriptInterface
        public void onLoadInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onLoadInterstitialFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");
            final String demandSourceName = ssaObj.getString("demandSourceName");

            IronSourceWebView.this.responseBack(value, true, null, null);

            if (TextUtils.isEmpty(demandSourceName)) {
                return;
            }


            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mDSInterstitialListener.onInterstitialLoadFailed(demandSourceName, toSend);
                    }
                });
            }

            IronSourceWebView.this.toastingErrMsg("onLoadInterstitialFail", "true");
        }

        @JavascriptInterface
        public void onShowInterstitialFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onShowInterstitialFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");
            final String demandSourceName = ssaObj.getString("demandSourceName");

            IronSourceWebView.this.responseBack(value, true, null, null);

            if (TextUtils.isEmpty(demandSourceName)) {
                return;
            }

            setInterstitialAvailability(demandSourceName, false);

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mDSInterstitialListener.onInterstitialShowFailed(demandSourceName, toSend);
                    }
                });
            }


            IronSourceWebView.this.toastingErrMsg("onShowInterstitialFail", value);
        }


        @JavascriptInterface
        public void onGenericFunctionSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGenericFunctionSuccess(" + value + ")");

            if (IronSourceWebView.this.mOnGenericFunctionListener == null) {
                Logger.d(IronSourceWebView.this.TAG, "genericFunctionListener was not found");
                return;
            }

            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mOnGenericFunctionListener.onGFSuccess();
                }

            });
            IronSourceWebView.this.responseBack(value, true, null, null);
        }

        @JavascriptInterface
        public void onGenericFunctionFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGenericFunctionFail(" + value + ")");

            if (IronSourceWebView.this.mOnGenericFunctionListener == null) {
                Logger.d(IronSourceWebView.this.TAG, "genericFunctionListener was not found");
                return;
            }

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");

            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mOnGenericFunctionListener.onGFFail(message);
                }

            });
            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onGenericFunctionFail", value);
        }


        @JavascriptInterface
        public void createCalendarEvent(String value) {
            Logger.i(IronSourceWebView.this.TAG, "createCalendarEvent(" + value + ")");
        }

        @JavascriptInterface
        public void openUrl(String value) {
            Logger.i(IronSourceWebView.this.TAG, "openUrl(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String url = ssaObj.getString("url");
            String method = ssaObj.getString("method");


            Context context = IronSourceWebView.this.getCurrentActivityContext();
            try {
                if (method.equalsIgnoreCase("external_browser")) {
                    UrlHandler.openUrl(context, url);
                } else if (method.equalsIgnoreCase("webview")) {
                    Intent intent = new Intent(context, OpenUrlActivity.class);
                    intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
                    intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, true);
                    intent.putExtra("immersive", IronSourceWebView.this.mIsImmersive);
                    context.startActivity(intent);
                } else if (method.equalsIgnoreCase("store")) {
                    Intent intent = new Intent(context, OpenUrlActivity.class);
                    intent.putExtra(IronSourceWebView.EXTERNAL_URL, url);
                    intent.putExtra(IronSourceWebView.IS_STORE, true);
                    intent.putExtra(IronSourceWebView.SECONDARY_WEB_VIEW, true);
                    context.startActivity(intent);
                }
            } catch (Exception ex) {
                IronSourceWebView.this.responseBack(value, false, ex.getMessage(), null);
                ex.printStackTrace();
            }
        }

        @JavascriptInterface
        public void setForceClose(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setForceClose(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            String width = ssaObj.getString("width");
            String hight = ssaObj.getString("height");

            IronSourceWebView.this.mHiddenForceCloseWidth = Integer.parseInt(width);
            IronSourceWebView.this.mHiddenForceCloseHeight = Integer.parseInt(hight);
            IronSourceWebView.this.mHiddenForceCloseLocation = ssaObj.getString("position");
        }

        @JavascriptInterface
        public void setBackButtonState(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setBackButtonState(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            String state = ssaObj.getString("state");

            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setBackButtonState(state);
        }

        @JavascriptInterface
        public void setStoreSearchKeys(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setStoreSearchKeys(" + value + ")");

            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setSearchKeys(value);
        }

        @JavascriptInterface
        public void setWebviewBackgroundColor(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setWebviewBackgroundColor(" + value + ")");

            IronSourceWebView.this.setWebviewBackground(value);
        }

        @JavascriptInterface
        public void toggleUDIA(String value) {
            Logger.i(IronSourceWebView.this.TAG, "toggleUDIA(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);


            if (!ssaObj.containsKey("toggle")) {
                IronSourceWebView.this.responseBack(value, false, "toggle key does not exist", null);
                return;
            }


            String toggleStr = ssaObj.getString("toggle");
            int toggle = Integer.parseInt(toggleStr);


            if (toggle == 0) {
                return;
            }


            String binaryToggle = Integer.toBinaryString(toggle);

            if (TextUtils.isEmpty(binaryToggle)) {
                IronSourceWebView.this.responseBack(value, false, "fialed to convert toggle", null);
                return;
            }


            char[] binaryToggleArr = binaryToggle.toCharArray();


            if (binaryToggleArr[3] == '0') {
                IronSourceSharedPrefHelper.getSupersonicPrefHelper().setShouldRegisterSessions(true);
            } else {
                IronSourceSharedPrefHelper.getSupersonicPrefHelper().setShouldRegisterSessions(false);
            }
        }

        @JavascriptInterface
        public void getUDIA(String value) {
            this.udiaResults = 0;

            Logger.i(IronSourceWebView.this.TAG, "getUDIA(" + value + ")");

            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);

            SSAObj ssaObj = new SSAObj(value);


            if (!ssaObj.containsKey("getByFlag")) {
                IronSourceWebView.this.responseBack(value, false, "getByFlag key does not exist", null);
                return;
            }


            String getByFlagStr = ssaObj.getString("getByFlag");
            int getByFlag = Integer.parseInt(getByFlagStr);


            if (getByFlag == 0) {
                return;
            }


            String binaryToggle = Integer.toBinaryString(getByFlag);

            if (TextUtils.isEmpty(binaryToggle)) {
                IronSourceWebView.this.responseBack(value, false, "fialed to convert getByFlag", null);
                return;
            }


            StringBuilder strBld = new StringBuilder(binaryToggle).reverse();
            binaryToggle = strBld.toString();


            char[] binaryToggleArr = binaryToggle.toCharArray();


            JSONArray jsArr = new JSONArray();


            if (binaryToggleArr[3] == '0') {
                JSONObject jsObj = new JSONObject();
                try {
                    jsObj.put("sessions", IronSourceSharedPrefHelper.getSupersonicPrefHelper().getSessions());
                    IronSourceSharedPrefHelper.getSupersonicPrefHelper().deleteSessions();
                    jsArr.put(jsObj);
                } catch (JSONException localJSONException) {
                }
            }


            if (binaryToggleArr[2] == '1') {
                this.udiaResults += 1;


                Location location = LocationService.getLastLocation(IronSourceWebView.this.getContext());

                if (location != null) {
                    JSONObject jsObj = new JSONObject();
                    try {
                        jsObj.put("latitude", location.getLatitude());
                        jsObj.put("longitude", location.getLongitude());
                        jsArr.put(jsObj);
                        this.udiaResults -= 1;
                        sendResults(funToCall, jsArr);
                        Logger.i(IronSourceWebView.this.TAG, "done location");
                    } catch (JSONException localJSONException1) {
                    }
                } else {
                    this.udiaResults -= 1;
                }
            }
        }


        private void sendResults(String funToCall, JSONArray jsArr) {
            Logger.i(IronSourceWebView.this.TAG, "sendResults: " + this.udiaResults);
            if (this.udiaResults <= 0) {
                injectGetUDIA(funToCall, jsArr);
            }
        }

        @JavascriptInterface
        public void onUDIASuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onUDIASuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onUDIAFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onUDIAFail(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUDIASuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUDIASuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUDIAFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUDIAFail(" + value + ")");
        }

        @JavascriptInterface
        public void setUserUniqueId(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setUserUniqueId(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            if ((!ssaObj.containsKey("userUniqueId")) || (!ssaObj.containsKey("productType"))) {
                IronSourceWebView.this.responseBack(value, false, "uniqueId or productType does not exist", null);
                return;
            }

            String uniqueId = ssaObj.getString("userUniqueId");
            String productType = ssaObj.getString("productType");

            boolean result = IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUniqueId(uniqueId, productType);

            if (result) {
                IronSourceWebView.this.responseBack(value, true, null, null);
            } else {
                IronSourceWebView.this.responseBack(value, false, "setUserUniqueId failed", null);
            }
        }

        @JavascriptInterface
        public void getUserUniqueId(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getUserUniqueId(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            if (!ssaObj.containsKey("productType")) {
                IronSourceWebView.this.responseBack(value, false, "productType does not exist", null);
                return;
            }

            String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);

            if (!TextUtils.isEmpty(funToCall)) {
                String productType = ssaObj.getString("productType");
                String id = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getUniqueId(productType);
                String params = IronSourceWebView.this.parseToJson("userUniqueId", id, "productType", productType, null, null, null, null, null, false);

                String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "onGetUserUniqueIdSuccess", "onGetUserUniqueIdFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void getAppsInstallTime(String value) {
            SSAObj ssaObj = new SSAObj(value);
            boolean failed = true;
            String funToCall = null;
            String dataToSend;
            try {
                String includeSystemApps = ssaObj.getString("systemApps");

                JSONObject appsInstallTime = DeviceStatus.getAppsInstallTime(IronSourceWebView.this.getContext(), Boolean.parseBoolean(includeSystemApps));
                dataToSend = appsInstallTime.toString();
                failed = false;
            } catch (Exception e) {
                Logger.i(IronSourceWebView.this.TAG, "getAppsInstallTime failed(" + e.getLocalizedMessage() + ")");
                dataToSend = e.getLocalizedMessage();
            }

            if (failed) {
                String failFunToCall = IronSourceWebView.this.extractFailFunctionToCall(value);
                if (!TextUtils.isEmpty(failFunToCall)) {
                    funToCall = failFunToCall;
                }
            } else {
                String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                if (!TextUtils.isEmpty(successFunToCall)) {
                    funToCall = successFunToCall;
                }
            }

            if (!TextUtils.isEmpty(funToCall)) {
                try {
                    dataToSend = URLDecoder.decode(dataToSend, Charset.defaultCharset().name());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String script = IronSourceWebView.this.generateJSToInject(funToCall, dataToSend);
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void onGetUserUniqueIdSuccess(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserUniqueIdSuccess(" + value + ")");
        }

        @JavascriptInterface
        public void onGetUserUniqueIdFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserUniqueIdFail(" + value + ")");
        }

        private void injectGetUDIA(String funToCall, JSONArray jsonArr) {
            if (!TextUtils.isEmpty(funToCall)) {
                String script = IronSourceWebView.this.generateJSToInject(funToCall, jsonArr.toString(), "onGetUDIASuccess", "onGetUDIAFail");
                IronSourceWebView.this.injectJavascript(script);
            }
        }

        @JavascriptInterface
        public void onOfferWallGeneric(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onOfferWallGeneric(" + value + ")");
            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString()))
                IronSourceWebView.this.mOnOfferWallListener.onOWGeneric("", "");
        }

        @JavascriptInterface
        public void setUserData(String value) {
            Logger.i(IronSourceWebView.this.TAG, "setUserData(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            if (!ssaObj.containsKey("key")) {
                IronSourceWebView.this.responseBack(value, false, "key does not exist", null);
                return;
            }

            if (!ssaObj.containsKey("value")) {
                IronSourceWebView.this.responseBack(value, false, "value does not exist", null);
                return;
            }

            String mKey = ssaObj.getString("key");
            String mValue = ssaObj.getString("value");

            boolean result = IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUserData(mKey, mValue);

            if (result) {
                String successFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                String params = IronSourceWebView.this.parseToJson(mKey, mValue, null, null, null, null, null, null, null, false);
                String script = IronSourceWebView.this.generateJSToInject(successFunToCall, params);
                IronSourceWebView.this.injectJavascript(script);
            } else {
                IronSourceWebView.this.responseBack(value, false, "SetUserData failed writing to shared preferences", null);
            }
        }

        @JavascriptInterface
        public void getUserData(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getUserData(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);

            if (!ssaObj.containsKey("key")) {
                IronSourceWebView.this.responseBack(value, false, "key does not exist", null);
                return;
            }

            String failFunToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
            String mKey = ssaObj.getString("key");

            String mValue = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getUserData(mKey);

            String params = IronSourceWebView.this.parseToJson(mKey, mValue, null, null, null, null, null, null, null, false);

            String script = IronSourceWebView.this.generateJSToInject(failFunToCall, params);
            IronSourceWebView.this.injectJavascript(script);
        }

        @JavascriptInterface
        public void onGetUserCreditsFail(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onGetUserCreditsFail(" + value + ")");

            SSAObj ssaObj = new SSAObj(value);
            final String message = ssaObj.getString("errMsg");

            if (IronSourceWebView.this.shouldNotifyDeveloper(SSAEnums.ProductType.OfferWall.toString())) {
                IronSourceWebView.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String toSend = message;
                        if (toSend == null) {
                            toSend = "We're sorry, some error occurred. we will investigate it";
                        }
                        IronSourceWebView.this.mOnOfferWallListener.onGetOWCreditsFailed(toSend);
                    }
                });
            }

            IronSourceWebView.this.responseBack(value, true, null, null);
            IronSourceWebView.this.toastingErrMsg("onGetUserCreditsFail", value);
        }

        @JavascriptInterface
        public void onAdWindowsClosed(String value) {
            Logger.i(IronSourceWebView.this.TAG, "onAdWindowsClosed(" + value + ")");


            IronSourceWebView.this.mSavedState.adClosed();
            IronSourceWebView.this.mSavedState.setDisplayedDemandSourceName(null);


            SSAObj ssaObj = new SSAObj(value);
            String product = ssaObj.getString("productType");
            final String demandSourceName = ssaObj.getString("demandSourceName");
            final SSAEnums.ProductType type = IronSourceWebView.this.getStringProductTypeAsEnum(product);

            Log.d(IronSourceWebView.this.PUB_TAG, "onAdClosed() with type " + type);

            if (IronSourceWebView.this.shouldNotifyDeveloper(product)) {
                if (product != null) {
                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if ((type == SSAEnums.ProductType.RewardedVideo) || (type == SSAEnums.ProductType.Interstitial)) {
                                DSAdProductListener listener = IronSourceWebView.this.getAdProductListenerByProductType(type);
                                if (listener != null) {
                                    listener.onAdProductClose(type, demandSourceName);
                                }
                            } else if (type == SSAEnums.ProductType.OfferWall) {
                                IronSourceWebView.this.mOnOfferWallListener.onOWAdClosed();
                            }
                        }
                    });
                }
            }
        }

        @JavascriptInterface
        public void onVideoStatusChanged(String value) {
            Log.d(IronSourceWebView.this.TAG, "onVideoStatusChanged(" + value + ")");
            SSAObj ssaObj = new SSAObj(value);
            String product = ssaObj.getString("productType");

            if ((IronSourceWebView.this.mVideoEventsListener != null) && (!TextUtils.isEmpty(product)) &&
                    (SSAEnums.ProductType.RewardedVideo.toString().equalsIgnoreCase(product))) {
                String status = ssaObj.getString("status");
                if ("started".equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoStarted();
                } else if ("paused".equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoPaused();
                } else if ("playing".equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoResumed();
                } else if ("ended".equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoEnded();
                } else if ("stopped".equalsIgnoreCase(status)) {
                    IronSourceWebView.this.mVideoEventsListener.onVideoStopped();
                } else {
                    Logger.i(IronSourceWebView.this.TAG, "onVideoStatusChanged: unknown status: " + status);
                }
            }
        }

        @JavascriptInterface
        public void postAdEventNotification(String value) {
            try {
                Logger.i(IronSourceWebView.this.TAG, "postAdEventNotification(" + value + ")");

                SSAObj ssaObj = new SSAObj(value);


                final String eventName = ssaObj.getString("eventName");
                if (TextUtils.isEmpty(eventName)) {
                    IronSourceWebView.this.responseBack(value, false, "eventName does not exist", null);
                    return;
                }


                final String demandSourceName = ssaObj.getString("dsName");


                final JSONObject extData = (JSONObject) ssaObj.get("extData");

                String productType = ssaObj.getString("productType");
                final SSAEnums.ProductType type = IronSourceWebView.this.getStringProductTypeAsEnum(productType);

                if (IronSourceWebView.this.shouldNotifyDeveloper(productType)) {
                    String funToCall = IronSourceWebView.this.extractSuccessFunctionToCall(value);
                    if (!TextUtils.isEmpty(funToCall)) {
                        String params = IronSourceWebView.this.parseToJson("productType", productType, "eventName", eventName, "demandSourceName", demandSourceName, null, null, null, false);
                        String script = IronSourceWebView.this.generateJSToInject(funToCall, params, "postAdEventNotificationSuccess", "postAdEventNotificationFail");
                        IronSourceWebView.this.injectJavascript(script);
                    }

                    IronSourceWebView.this.runOnUiThread(new Runnable() {
                        public void run() {
                            if ((type == SSAEnums.ProductType.Interstitial) || (type == SSAEnums.ProductType.RewardedVideo)) {
                                DSAdProductListener listener = IronSourceWebView.this.getAdProductListenerByProductType(type);
                                if (listener != null) {
                                    listener.onAdProductEventNotificationReceived(type, demandSourceName, eventName, extData);
                                }
                            } else if (type == SSAEnums.ProductType.OfferWall) {
                                IronSourceWebView.this.mOnOfferWallListener.onOfferwallEventNotificationReceived(eventName, extData);
                            }
                        }
                    });
                } else {
                    IronSourceWebView.this.responseBack(value, false, "productType does not exist", null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        class JSCallbackTask {
            JSCallbackTask() {
            }


            void sendMessage(boolean isSuccess, String callbackFunction, String messageData) {
                SSAObj res = new SSAObj();
                res.put(isSuccess ? IronSourceWebView.JSON_KEY_SUCCESS : IronSourceWebView.JSON_KEY_FAIL, callbackFunction);
                res.put("data", messageData);
                IronSourceWebView.this.responseBack(res.toString(), isSuccess, null, null);
            }

            void sendMessage(boolean isSuccess, String callbackFunction, SSAObj messageData) {
                messageData.put(isSuccess ? IronSourceWebView.JSON_KEY_SUCCESS : IronSourceWebView.JSON_KEY_FAIL, callbackFunction);
                IronSourceWebView.this.responseBack(messageData.toString(), isSuccess, null, null);
            }
        }

        @JavascriptInterface
        public void moatAPI(final String value) {
            IronSourceWebView.this.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Logger.i(IronSourceWebView.this.TAG, "moatAPI(" + value + ")");
                        SSAObj ssaObj = new SSAObj(value);
                        IronSourceWebView.this.mMoatJsAdapter.call(ssaObj.toString(), new JSCallbackTask(), IronSourceWebView.this.getWebview());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.i(IronSourceWebView.this.TAG, "moatAPI failed with exception " + e.getMessage());
                    }
                }
            });
        }

        @JavascriptInterface
        public void permissionsAPI(String value) {
            try {
                Logger.i(IronSourceWebView.this.TAG, "permissionsAPI(" + value + ")");
                SSAObj ssaObj = new SSAObj(value);
                IronSourceWebView.this.mPermissionsJsAdapter.call(ssaObj.toString(), new JSCallbackTask());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.i(IronSourceWebView.this.TAG, "permissionsAPI failed with exception " + e.getMessage());
            }
        }

        @JavascriptInterface
        public void getDeviceVolume(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getDeviceVolume(" + value + ")");
            try {
                float currVolume = DeviceProperties.getInstance(IronSourceWebView.this.getCurrentActivityContext()).getDeviceVolume(IronSourceWebView.this.getCurrentActivityContext());

                SSAObj ssaObj = new SSAObj(value);
                ssaObj.put("deviceVolume", String.valueOf(currVolume));
                IronSourceWebView.this.responseBack(ssaObj.toString(), true, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public void locationServicesEnabled(String value) {
            Logger.i(IronSourceWebView.this.TAG, "locationServicesEnabled(" + value + ")");
            try {
                boolean locationServicesEnabled = LocationService.locationServicesEnabled(IronSourceWebView.this.getContext());
                SSAObj ssaObj = new SSAObj(value);
                ssaObj.put("status", String.valueOf(locationServicesEnabled));
                IronSourceWebView.this.responseBack(ssaObj.toString(), true, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public void getDeviceLocation(String value) {
            Logger.i(IronSourceWebView.this.TAG, "getDeviceLocation(" + value + ")");
            try {
                SSAObj ret = IronSourceWebView.this.createLocationObject(value, LocationService.getLastLocation(IronSourceWebView.this.getContext()));
                IronSourceWebView.this.responseBack(ret.toString(), true, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @JavascriptInterface
        public void getDevicePreciseLocation(final String value) {
            Logger.i(IronSourceWebView.this.TAG, "getDevicePreciseLocation(" + value + ")");
            try {
                LocationService.getPreciseLocation(IronSourceWebView.this.getContext(), new ISLocationListener() {
                    public void onLocationChanged(Location location) {
                        SSAObj ret = IronSourceWebView.this.createLocationObject(value, location);
                        IronSourceWebView.this.responseBack(ret.toString(), true, null, null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void invokePendingCommands() {
        while (this.mControllerCommandsQueue.size() > 0) {
            String command = (String) this.mControllerCommandsQueue.get(0);
            injectJavascript(command);
            this.mControllerCommandsQueue.remove(0);
        }
    }

    private SSAObj createLocationObject(String value, Location location) {
        SSAObj ssaObj = new SSAObj(value);
        if (location != null) {
            ssaObj.put("provider", location.getProvider());
            ssaObj.put("latitude", Double.toString(location.getLatitude()));
            ssaObj.put("longitude", Double.toString(location.getLongitude()));
            ssaObj.put("altitude", Double.toString(location.getAltitude()));
            ssaObj.put("time", Long.toString(location.getTime()));
            ssaObj.put("accuracy", Float.toString(location.getAccuracy()));
            ssaObj.put("bearing", Float.toString(location.getBearing()));
            ssaObj.put("speed", Float.toString(location.getSpeed()));
        } else {
            ssaObj.put("error", "location data is not available");
        }
        return ssaObj;
    }

    private DSAdProductListener getAdProductListenerByProductType(SSAEnums.ProductType type) {
        if (type == SSAEnums.ProductType.Interstitial)
            return this.mDSInterstitialListener;
        if (type == SSAEnums.ProductType.RewardedVideo) {
            return this.mDSRewardedVideoListener;
        }

        return null;
    }

    private SSAEnums.ProductType getStringProductTypeAsEnum(String productType) {
        if (TextUtils.isEmpty(productType)) {
            return null;
        }

        if (productType.equalsIgnoreCase(SSAEnums.ProductType.Interstitial.toString()))
            return SSAEnums.ProductType.Interstitial;
        if (productType.equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.toString()))
            return SSAEnums.ProductType.RewardedVideo;
        if (productType.equalsIgnoreCase(SSAEnums.ProductType.OfferWall.toString())) {
            return SSAEnums.ProductType.OfferWall;
        }

        return null;
    }

    public static void setEXTERNAL_URL(String EXTERNAL_URL) {
        EXTERNAL_URL = EXTERNAL_URL;
    }

    public void setVideoEventsListener(VideoEventsListener listener) {
        this.mVideoEventsListener = listener;
    }

    public void removeVideoEventsListener() {
        this.mVideoEventsListener = null;
    }

    private void setWebviewBackground(String value) {
        SSAObj ssaObj = new SSAObj(value);
        String keyColor = ssaObj.getString("color");

        int bgColor = 0;

        if (!"transparent".equalsIgnoreCase(keyColor)) {
            bgColor = Color.parseColor(keyColor);
        }

        setBackgroundColor(bgColor);
    }


    public void load(final int loadAttemp) {
        try {
            loadUrl("about:blank");
        } catch (Throwable e) {
            Logger.e(this.TAG, "WebViewController:: load: " + e.toString());
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewLoadBlank"});
        }


        String controllerPath = "file://" + this.mCacheDirectory + File.separator + "mobileController.html";

        File file = new File(this.mCacheDirectory + File.separator + "mobileController.html");
        if (file.exists()) {

            JSONObject configObject = SDKUtils.getControllerConfigAsJSONObject();


            setWebDebuggingEnabled(configObject);


            this.mRequestParameters = getRequestParameters(configObject);


            String controllerPathWithParams = controllerPath + "?" + this.mRequestParameters;


            this.mLoadControllerTimer = new CountDownTimer(50000L, 1000L) {

                public void onTick(long millisUntilFinished) {

                    Logger.i(IronSourceWebView.this.TAG, "Loading Controller Timer Tick " + millisUntilFinished);
                }

                public void onFinish() {
                    Logger.i(IronSourceWebView.this.TAG, "Loading Controller Timer Finish");

                    if (loadAttemp == 3) {
                        IronSourceWebView.this.mGlobalControllerTimer.cancel();

                        Collection<DemandSource> demandSourcesRV = IronSourceWebView.this.mDemandSourceManager.getDemandSources(ProductType.RewardedVideo);
                        Iterator var2 = demandSourcesRV.iterator();

                        while(var2.hasNext()) {
                            DemandSource demandSource = (DemandSource)var2.next();
                            if(demandSource.getDemandSourceInitState() == 1) {
                                sendProductErrorMessage(ProductType.RewardedVideo, demandSource.getDemandSourceName());
                            }
                        }

                        Collection<DemandSource> demandSourcesIS = IronSourceWebView.this.mDemandSourceManager.getDemandSources(ProductType.Interstitial);
                        Iterator var6 = demandSourcesIS.iterator();

                        while(var6.hasNext()) {
                            DemandSource demandSourcex = (DemandSource)var6.next();
                            if(demandSourcex.getDemandSourceInitState() == 1) {
                                sendProductErrorMessage(ProductType.Interstitial, demandSourcex.getDemandSourceName());
                            }
                        }

                        if (IronSourceWebView.this.mOWmiss) {
                            IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.OfferWall, null);
                        }

                        if (IronSourceWebView.this.mOWCreditsMiss) {
                            IronSourceWebView.this.sendProductErrorMessage(SSAEnums.ProductType.OfferWallCredits, null);
                        }

                    } else {
                        IronSourceWebView.this.load(2);
                    }
                }
            }.start();


            try {
                loadUrl(controllerPathWithParams);
            } catch (Throwable e) {
                Logger.e(this.TAG, "WebViewController:: load: " + e.toString());
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewLoadWithPath"});
            }

            Logger.i(this.TAG, "load(): " + controllerPathWithParams);
        } else {
            Logger.i(this.TAG, "load(): Mobile Controller HTML Does not exist");
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=htmlControllerDoesNotExistOnFileSystem"});
        }
    }

    private void setWebDebuggingEnabled(JSONObject configObject) {
        if (configObject.optBoolean("inspectWebview")) {
            setWebDebuggingEnabled();
        }
    }


    private void initProduct(String applicationKey, String userId, SSAEnums.ProductType type, DemandSource demandSource, String action) {
        if ((TextUtils.isEmpty(userId)) || (TextUtils.isEmpty(applicationKey))) {
            triggerOnControllerInitProductFail("User id or Application key are missing", type, demandSource.getDemandSourceName());
            return;
        }

        if (this.mControllerState == SSAEnums.ControllerState.Ready) {

            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setApplicationKey(applicationKey, type);
            IronSourceSharedPrefHelper.getSupersonicPrefHelper().setUserID(userId, type);
            injectJavascript(createInitProductJSMethod(type, demandSource));
        } else {
            setMissProduct(type, demandSource);

            if (this.mControllerState == SSAEnums.ControllerState.Failed) {

                triggerOnControllerInitProductFail(
                        SDKUtils.createErrorMessage(action, "Initiating Controller"), type, demandSource
                                .getDemandSourceName());


            } else if (this.mGlobalControllerTimeFinish) {
                downloadController();
            }
        }
    }


    public void initRewardedVideo(String applicationKey, String userId, DemandSource demandSource, DSRewardedVideoListener listener) {
        this.mRVAppKey = applicationKey;
        this.mRVUserId = userId;
        this.mProductParametersCollection.setProductParameters(SSAEnums.ProductType.RewardedVideo, applicationKey, userId);
        this.mDSRewardedVideoListener = listener;
        this.mSavedState.setRVAppKey(applicationKey);
        this.mSavedState.setRVUserId(userId);

        initProduct(applicationKey, userId, SSAEnums.ProductType.RewardedVideo, demandSource, "Init RV");
    }


    public void initInterstitial(String applicationKey, String userId, DemandSource demandSource, DSInterstitialListener listener) {
        this.mISAppKey = applicationKey;
        this.mISUserId = userId;
        this.mProductParametersCollection.setProductParameters(SSAEnums.ProductType.Interstitial, applicationKey, userId);
        this.mDSInterstitialListener = listener;


        this.mSavedState.setInterstitialAppKey(this.mISAppKey);
        this.mSavedState.setInterstitialUserId(this.mISUserId);

        initProduct(this.mISAppKey, this.mISUserId, SSAEnums.ProductType.Interstitial, demandSource, "Init IS");
    }


    public void loadInterstitial(final String demandSourceName) {
        Map<String, String> productParamsMap = new HashMap();

        if (!TextUtils.isEmpty(demandSourceName)) {
            productParamsMap.put("demandSourceName", demandSourceName);
        }

        String params = flatMapToJsonAsString(productParamsMap);

        if (!isInterstitialAdAvailable(demandSourceName)) {
            this.mSavedState.setReportLoadInterstitial(demandSourceName, true);

            String script = generateJSToInject("loadInterstitial", params, "onLoadInterstitialSuccess", "onLoadInterstitialFail");
            injectJavascript(script);


        } else if (shouldNotifyDeveloper(SSAEnums.ProductType.Interstitial.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    IronSourceWebView.this.mDSInterstitialListener.onInterstitialLoadSuccess(demandSourceName);
                }
            });
        }
    }


    public boolean isInterstitialAdAvailable(String demandSourceName) {
        DemandSource demandSource = this.mDemandSourceManager.getDemandSourceByName(SSAEnums.ProductType.Interstitial, demandSourceName);
        return (demandSource != null) && (demandSource.getAvailabilityState());
    }

    public void showInterstitial(JSONObject showParams) {
        String script = createShowProductJSMethod(SSAEnums.ProductType.Interstitial, showParams);
        injectJavascript(script);
    }


    public void initOfferWall(String applicationKey, String userId, Map<String, String> extraParameters, OnOfferWallListener listener) {
        this.mOWAppKey = applicationKey;
        this.mOWUserId = userId;
        this.mProductParametersCollection.setProductParameters(SSAEnums.ProductType.OfferWall, applicationKey, userId);
        this.mOWExtraParameters = extraParameters;
        this.mOnOfferWallListener = listener;

        this.mSavedState.setOfferWallExtraParams(this.mOWExtraParameters);

        this.mSavedState.setOfferwallReportInit(true);

        initProduct(this.mOWAppKey, this.mOWUserId, SSAEnums.ProductType.OfferWall, null, "Init OW");
    }

    public void showOfferWall(Map<String, String> extraParameters) {
        this.mOWExtraParameters = extraParameters;
        String script = generateJSToInject("showOfferWall", "onShowOfferWallSuccess", "onShowOfferWallFail");
        injectJavascript(script);
    }


    public void getOfferWallCredits(String applicationKey, String userId, OnOfferWallListener listener) {
        this.mOWCreditsAppKey = applicationKey;
        this.mOWCreditsUserId = userId;
        this.mProductParametersCollection.setProductParameters(SSAEnums.ProductType.OfferWallCredits, applicationKey, userId);

        this.mOnOfferWallListener = listener;

        initProduct(this.mOWCreditsAppKey, this.mOWCreditsUserId, SSAEnums.ProductType.OfferWallCredits, null, "Show OW Credits");
    }


    public void updateConsentInfo(JSONObject consentParams) {
        String script = generateJSToInject("updateConsentInfo", consentParams != null ? consentParams.toString() : null);
        injectJavascript("updateConsentInfo", script);
    }


    private String createInitProductJSMethod(SSAEnums.ProductType type, DemandSource demandSource) {
        String script = "";

        if ((type == SSAEnums.ProductType.RewardedVideo) || (type == SSAEnums.ProductType.Interstitial) || (type == SSAEnums.ProductType.OfferWall)) {
            Map<String, String> productParamsMap = new HashMap();
            ProductParameters productParameters = this.mProductParametersCollection.getProductParameters(type);
            if (productParameters != null) {
                productParamsMap.put("applicationKey", productParameters.appKey);
                productParamsMap.put("applicationUserId", productParameters.userId);
            }

            if (demandSource != null) {
                if (demandSource.getExtraParams() != null) {
                    productParamsMap.putAll(demandSource.getExtraParams());
                }
                productParamsMap.put("demandSourceName", demandSource.getDemandSourceName());
            } else if (getExtraParamsByProduct(type) != null) {
                productParamsMap.putAll(getExtraParamsByProduct(type));
            }

            String params = flatMapToJsonAsString(productParamsMap);
            Constants.JSMethods method = Constants.JSMethods.getInitMethodByProduct(type);
            script = generateJSToInject(method.methodName, params, method.successCallbackName, method.failureCallbackName);
        } else if (type == SSAEnums.ProductType.OfferWallCredits) {
            String params = parseToJson("productType", "OfferWall", "applicationKey", this.mOWCreditsAppKey, "applicationUserId", this.mOWCreditsUserId, null, null, null, false);


            script = generateJSToInject("getUserCredits", params, "null", "onGetUserCreditsFail");
        }

        return script;
    }

    private String createShowProductJSMethod(SSAEnums.ProductType type, JSONObject showParams) {
        String script = "";

        Map<String, String> paramsMap = new HashMap();
        String sessionDepth = Integer.toString(showParams.optInt("sessionDepth"));
        paramsMap.put("sessionDepth", sessionDepth);

        String demandSourceName = showParams.optString("demandSourceName");
        DemandSource demandSource = this.mDemandSourceManager.getDemandSourceByName(type, demandSourceName);

        if (demandSource != null) {
            if (demandSource.getExtraParams() != null) {
                paramsMap.putAll(demandSource.getExtraParams());
            }

            if (!TextUtils.isEmpty(demandSourceName)) {
                paramsMap.put("demandSourceName", demandSourceName);
            }
        } else if (getExtraParamsByProduct(type) != null) {
            paramsMap.putAll(getExtraParamsByProduct(type));
        }

        String params = flatMapToJsonAsString(paramsMap);
        Constants.JSMethods method = Constants.JSMethods.getShowMethodByProduct(type);
        script = generateJSToInject(method.methodName, params, method.successCallbackName, method.failureCallbackName);

        return script;
    }

    private String flatMapToJsonAsString(Map<String, String> params) {
        JSONObject jsObj = new JSONObject();
        if (params != null) {
            Iterator<Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, String> pairs = (Entry) it.next();
                try {
                    jsObj.putOpt((String) pairs.getKey(), SDKUtils.encodeString((String) pairs.getValue()));
                } catch (JSONException e) {
                    Logger.i(this.TAG, "flatMapToJsonAsStringfailed " + e.toString());
                }
                it.remove();
            }
        }

        return jsObj.toString();
    }

    void setMissProduct(SSAEnums.ProductType type, DemandSource demandSource) {
        if ((type == SSAEnums.ProductType.RewardedVideo) || (type == SSAEnums.ProductType.Interstitial)) {
            if (demandSource != null) {
                demandSource.setDemandSourceInitState(1);
            }
        } else if (type == SSAEnums.ProductType.OfferWall) {
            this.mOWmiss = true;
        } else if (type == SSAEnums.ProductType.OfferWallCredits) {
            this.mOWCreditsMiss = true;
        }
        Logger.i(this.TAG, "setMissProduct(" + type + ")");
    }

    private void triggerOnControllerInitProductFail(final String message, final SSAEnums.ProductType type, final String demandSourceName) {
        if (shouldNotifyDeveloper(type.toString())) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if ((SSAEnums.ProductType.RewardedVideo == type) || (SSAEnums.ProductType.Interstitial == type)) {
                        if (TextUtils.isEmpty(demandSourceName)) {
                            return;
                        }
                        DSAdProductListener listener = IronSourceWebView.this.getAdProductListenerByProductType(type);
                        Log.d(IronSourceWebView.this.TAG, "onAdProductInitFailed (message:" + message + ")(" + type + ")");

                        if (listener != null) {
                            listener.onAdProductInitFailed(type, demandSourceName, message);
                        }
                    } else if (SSAEnums.ProductType.OfferWall == type) {
                        IronSourceWebView.this.mOnOfferWallListener.onOfferwallInitFail(message);
                    } else if (SSAEnums.ProductType.OfferWallCredits == type) {
                        IronSourceWebView.this.mOnOfferWallListener.onGetOWCreditsFailed(message);
                    }
                }
            });
        }
    }

    public void showRewardedVideo(JSONObject showParams) {
        String script = createShowProductJSMethod(SSAEnums.ProductType.RewardedVideo, showParams);
        injectJavascript(script);
    }

    public void assetCached(String file, String path) {
        String params = parseToJson("file", file, "path", path, null, null, null, null, null, false);
        String script = generateJSToInject("assetCached", params);
        injectJavascript(script);
    }

    public void assetCachedFailed(String file, String path, String errorMsg) {
        String params = parseToJson("file", file, "path", path, "errMsg", errorMsg, null, null, null, false);
        String script = generateJSToInject("assetCachedFailed", params);
        injectJavascript(script);
    }

    public void enterBackground() {
        if (this.mControllerState == SSAEnums.ControllerState.Ready) {
            String script = generateJSToInject("enterBackground");
            injectJavascript(script);
        }
    }

    public void enterForeground() {
        if (this.mControllerState == SSAEnums.ControllerState.Ready) {
            String script = generateJSToInject("enterForeground");
            injectJavascript(script);
        }
    }

    public void viewableChange(boolean visibility, String webview) {
        String params = parseToJson("webview", webview, null, null, null, null, null, null, "isViewable", visibility);


        String script = generateJSToInject("viewableChange", params);
        injectJavascript(script);
    }

    public void nativeNavigationPressed(String action) {
        String params = parseToJson("action", action, null, null, null, null, null, null, null, false);
        String script = generateJSToInject("nativeNavigationPressed", params);
        injectJavascript(script);
    }

    public void pageFinished() {
        String script = generateJSToInject("pageFinished");
        injectJavascript(script);
    }

    public void interceptedUrlToStore() {
        String script = generateJSToInject("interceptedUrlToStore");
        injectJavascript(script);
    }


    private void injectJavascript(String command, String script) {
        if ((!isControllerStateReady()) && (controllerCommandSupportsQueue(command))) {
            this.mControllerCommandsQueue.add(script);
        } else {
            injectJavascript(script);
        }
    }

    private boolean isControllerStateReady() {
        return SSAEnums.ControllerState.Ready.equals(this.mControllerState);
    }

    private void injectJavascript(String script) {
        if (TextUtils.isEmpty(script)) {
            return;
        }


        String catchClosure = "empty";
        if (getDebugMode() == SSAEnums.DebugMode.MODE_0.getValue()) {
            catchClosure = "console.log(\"JS exeption: \" + JSON.stringify(e));";
        } else if ((getDebugMode() >= SSAEnums.DebugMode.MODE_1.getValue()) &&
                (getDebugMode() <= SSAEnums.DebugMode.MODE_3.getValue())) {
            catchClosure = "console.log(\"JS exeption: \" + JSON.stringify(e));";
        }


        final StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder
                .append("try{")
                .append(script)
                .append("}catch(e){")
                .append(catchClosure)
                .append("}");

        final String url = "javascript:" + scriptBuilder.toString();

        runOnUiThread(new Runnable() {
            public void run() {
                Logger.i(IronSourceWebView.this.TAG, url);
                try {
                    if (IronSourceWebView.this.isKitkatAndAbove != null) {
                        if (IronSourceWebView.this.isKitkatAndAbove.booleanValue()) {
                            IronSourceWebView.this.evaluateJavascriptKitKat(scriptBuilder.toString());
                        } else {
                            IronSourceWebView.this.loadUrl(url);
                        }
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        try {
                            IronSourceWebView.this.evaluateJavascriptKitKat(scriptBuilder.toString());
                            IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(true);
                        } catch (NoSuchMethodError e) {
                            Logger.e(IronSourceWebView.this.TAG, "evaluateJavascrip NoSuchMethodError: SDK version=" + Build.VERSION.SDK_INT + " " + e);
                            IronSourceWebView.this.loadUrl(url);
                            IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(false);
                        } catch (Throwable e) {
                            Logger.e(IronSourceWebView.this.TAG, "evaluateJavascrip Exception: SDK version=" + Build.VERSION.SDK_INT + " " + e);

                            IronSourceWebView.this.loadUrl(url);
                            IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(false);
                        }
                    } else {
                        IronSourceWebView.this.loadUrl(url);
                        IronSourceWebView.this.isKitkatAndAbove = Boolean.valueOf(false);
                    }
                } catch (Throwable t) {
                    Logger.e(IronSourceWebView.this.TAG, "injectJavascript: " + t.toString());
                    new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=injectJavaScript"});
                }
            }
        });
    }

    @SuppressLint({"NewApi"})
    private void evaluateJavascriptKitKat(String script) {
        evaluateJavascript(script, null);
    }

    private boolean controllerCommandSupportsQueue(String command) {
        ArrayList<String> supportedCommand = new ArrayList();
        supportedCommand.add("updateConsentInfo");

        return supportedCommand.contains(command);
    }


    public Context getCurrentActivityContext() {
        MutableContextWrapper mctx = (MutableContextWrapper) this.mCurrentActivityContext;
        return mctx.getBaseContext();
    }

    private String getRequestParameters(JSONObject configObject) {
        DeviceProperties properties = DeviceProperties.getInstance(getContext());

        StringBuilder builder = new StringBuilder();

        String sdkVer = DeviceProperties.getSupersonicSdkVersion();
        if (!TextUtils.isEmpty(sdkVer)) {


            builder.append("SDKVersion").append("=").append(sdkVer).append("&");
        }

        String osType = properties.getDeviceOsType();
        if (!TextUtils.isEmpty(osType)) {

            builder.append("deviceOs").append("=").append(osType);
        }

        String serverControllerUrl = SDKUtils.getControllerUrl();
        Uri downloadUri = Uri.parse(serverControllerUrl);

        if (downloadUri != null) {
            String scheme = downloadUri.getScheme() + ":";
            String host = downloadUri.getHost();
            int port = downloadUri.getPort();
            if (port != -1) {
                host = host + ":" + port;
            }


            builder.append("&").append("protocol").append("=").append(scheme);

            builder.append("&")
                    .append("domain")
                    .append("=")
                    .append(host);

            if (configObject.keys().hasNext()) {
                try {
                    JSONObject conf = new JSONObject(configObject, new String[]{"isSecured", "applicationKey"});


                    String config = conf.toString();
                    if (!TextUtils.isEmpty(config)) {


                        builder.append("&").append("controllerConfig").append("=").append(config);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            builder.append("&").append("debug").append("=").append(getDebugMode());
        }

        return builder.toString();
    }

    private void closeWebView() {
        if (this.mChangeListener != null) {
            this.mChangeListener.onCloseRequested();
        }
    }

    private WebView getWebview() {
        return this;
    }


    private static String JSON_KEY_SUCCESS = "success";
    private static String JSON_KEY_FAIL = "fail";

    private void responseBack(String value, boolean result, String errorMessage, String errorCode) {
        SSAObj ssaObj = new SSAObj(value);
        String success = ssaObj.getString(JSON_KEY_SUCCESS);
        String fail = ssaObj.getString(JSON_KEY_FAIL);

        String funToCall = null;


        if (result) {
            if (!TextUtils.isEmpty(success)) {
                funToCall = success;
            }
        } else if (!TextUtils.isEmpty(fail)) {
            funToCall = fail;
        }


        if (!TextUtils.isEmpty(funToCall)) {
            if (!TextUtils.isEmpty(errorMessage)) {
                try {
                    JSONObject jsObj = new JSONObject(value);
                    value = jsObj.put("errMsg", errorMessage).toString();
                } catch (JSONException localJSONException) {
                }
            }

            if (!TextUtils.isEmpty(errorCode)) {
                try {
                    JSONObject jsObj = new JSONObject(value);
                    value = jsObj.put("errCode", errorCode).toString();
                } catch (JSONException localJSONException1) {
                }
            }

            String script = generateJSToInject(funToCall, value);
            injectJavascript(script);
        }
    }

    private String extractSuccessFunctionToCall(String jsonStr) {
        SSAObj ssaObj = new SSAObj(jsonStr);
        String funToCall = ssaObj.getString(JSON_KEY_SUCCESS);

        return funToCall;
    }

    private String extractFailFunctionToCall(String jsonStr) {
        SSAObj ssaObj = new SSAObj(jsonStr);
        String funToCall = ssaObj.getString(JSON_KEY_FAIL);

        return funToCall;
    }


    private String parseToJson(String key1, String value1, String key2, String value2, String key3, String value3, String key4, String value4, String key5, boolean value5) {
        JSONObject jsObj = new JSONObject();

        try {
            if ((!TextUtils.isEmpty(key1)) && (!TextUtils.isEmpty(value1))) {
                jsObj.put(key1, SDKUtils.encodeString(value1));
            }

            if ((!TextUtils.isEmpty(key2)) && (!TextUtils.isEmpty(value2))) {
                jsObj.put(key2, SDKUtils.encodeString(value2));
            }

            if ((!TextUtils.isEmpty(key3)) && (!TextUtils.isEmpty(value3))) {
                jsObj.put(key3, SDKUtils.encodeString(value3));
            }

            if ((!TextUtils.isEmpty(key4)) && (!TextUtils.isEmpty(value4))) {
                jsObj.put(key4, SDKUtils.encodeString(value4));
            }

            if (!TextUtils.isEmpty(key5)) {
                jsObj.put(key5, value5);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
        }

        return jsObj.toString();
    }

    private String mapToJson(Map<String, String> map) {
        JSONObject jsObj = new JSONObject();

        if ((map != null) && (!map.isEmpty())) {
            for (String key : map.keySet()) {
                String value = (String) map.get(key);
                try {
                    jsObj.put(key, SDKUtils.encodeString(value));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return jsObj.toString();
    }

    private Object[] getDeviceParams(Context context) {
        boolean fail = false;

        DeviceProperties deviceProperties = DeviceProperties.getInstance(context);

        JSONObject jsObj = new JSONObject();


        try {
            jsObj.put("appOrientation", SDKUtils.translateRequestedOrientation(DeviceStatus.getActivityRequestedOrientation(getCurrentActivityContext())));


            String deviceOem = deviceProperties.getDeviceOem();
            if (deviceOem != null) {
                jsObj.put(SDKUtils.encodeString("deviceOEM"), SDKUtils.encodeString(deviceOem));
            }


            String deviceModel = deviceProperties.getDeviceModel();
            if (deviceModel != null) {
                jsObj.put(SDKUtils.encodeString("deviceModel"), SDKUtils.encodeString(deviceModel));
            } else {
                fail = true;
            }


            SDKUtils.loadGoogleAdvertiserInfo(context);
            String advertiserId = SDKUtils.getAdvertiserId();
            Boolean isLAT = Boolean.valueOf(SDKUtils.isLimitAdTrackingEnabled());

            if (!TextUtils.isEmpty(advertiserId)) {
                Logger.i(this.TAG, "add AID and LAT");


                jsObj.put("isLimitAdTrackingEnabled", isLAT);


                StringBuilder aid = new StringBuilder().append("deviceIds").append("[").append("AID").append("]");

                jsObj.put(aid.toString(), SDKUtils.encodeString(advertiserId));
            }


            String deviceOSType = deviceProperties.getDeviceOsType();
            if (deviceOSType != null) {
                jsObj.put(SDKUtils.encodeString("deviceOs"), SDKUtils.encodeString(deviceOSType));
            } else {
                fail = true;
            }


            String deviceOSVersion = deviceProperties.getDeviceOsVersion();
            if (deviceOSVersion != null) {
                deviceOSVersion = deviceOSVersion.replaceAll("[^0-9/.]", "");
                jsObj.put(SDKUtils.encodeString("deviceOSVersion"), deviceOSVersion);
            } else {
                fail = true;
            }


            String deviceApiLevel = String.valueOf(deviceProperties.getDeviceApiLevel());
            if (deviceApiLevel != null) {
                jsObj.put(SDKUtils.encodeString("deviceApiLevel"), deviceApiLevel);
            } else {
                fail = true;
            }


            String ssaSDKVersion = DeviceProperties.getSupersonicSdkVersion();
            if (ssaSDKVersion != null) {
                jsObj.put(SDKUtils.encodeString("SDKVersion"), SDKUtils.encodeString(ssaSDKVersion));
            }


            if ((deviceProperties.getDeviceCarrier() != null) && (deviceProperties.getDeviceCarrier().length() > 0)) {
                jsObj.put(SDKUtils.encodeString("mobileCarrier"), SDKUtils.encodeString(deviceProperties.getDeviceCarrier()));
            }


            String connectionType = ConnectivityService.getConnectionType(context);
            if (!TextUtils.isEmpty(connectionType)) {
                jsObj.put(SDKUtils.encodeString("connectionType"), SDKUtils.encodeString(connectionType));
            } else {
                fail = true;
            }


            String deviceLanguage = context.getResources().getConfiguration().locale.getLanguage();
            if (!TextUtils.isEmpty(deviceLanguage)) {
                jsObj.put(SDKUtils.encodeString("deviceLanguage"), SDKUtils.encodeString(deviceLanguage.toUpperCase()));
            }


            if (SDKUtils.isExternalStorageAvailable()) {
                long freeDiskSize = DeviceStatus.getAvailableMemorySizeInMegaBytes(this.mCacheDirectory);
                jsObj.put(
                        SDKUtils.encodeString("diskFreeSize"),
                        SDKUtils.encodeString(String.valueOf(freeDiskSize)));
            } else {
                fail = true;
            }


            int deviceWidth = DeviceStatus.getDeviceWidth();
            String width = String.valueOf(deviceWidth);
            if (!TextUtils.isEmpty(width)) {
                StringBuilder key = new StringBuilder();
                key.append(SDKUtils.encodeString("deviceScreenSize"))
                        .append("[")
                        .append(SDKUtils.encodeString("width"))
                        .append("]");

                jsObj.put(key.toString(),
                        SDKUtils.encodeString(width));
            } else {
                fail = true;
            }


            int deviceHeigh = DeviceStatus.getDeviceHeight();
            String height = String.valueOf(deviceHeigh);

            StringBuilder key = new StringBuilder();
            key.append(SDKUtils.encodeString("deviceScreenSize"))
                    .append("[")
                    .append(SDKUtils.encodeString("height"))
                    .append("]");

            jsObj.put(key.toString(),
                    SDKUtils.encodeString(height));

            String packageName = ApplicationContext.getPackageName(getContext());
            if (!TextUtils.isEmpty(packageName)) {
                jsObj.put(SDKUtils.encodeString("bundleId"), SDKUtils.encodeString(packageName));
            }

            float deviceScale = DeviceStatus.getDeviceDensity();
            String scaleStr = String.valueOf(deviceScale);
            if (!TextUtils.isEmpty(scaleStr)) {
                jsObj.put(SDKUtils.encodeString("deviceScreenScale"), SDKUtils.encodeString(scaleStr));
            }

            boolean isRoot = DeviceStatus.isRootedDevice();
            String rootStr = String.valueOf(isRoot);
            if (!TextUtils.isEmpty(rootStr)) {
                jsObj.put(SDKUtils.encodeString("unLocked"), SDKUtils.encodeString(rootStr));
            }

            float deviceVolume = DeviceProperties.getInstance(context).getDeviceVolume(context);
            if (!TextUtils.isEmpty(rootStr)) {
                jsObj.put(SDKUtils.encodeString("deviceVolume"), deviceVolume);
            }
            Context ctx = getCurrentActivityContext();
            if ((Build.VERSION.SDK_INT >= 19) && ((ctx instanceof Activity))) {
                jsObj.put(SDKUtils.encodeString("immersiveMode"),
                        DeviceStatus.isImmersiveSupported((Activity) ctx));
            }

            jsObj.put(SDKUtils.encodeString("batteryLevel"), DeviceStatus.getBatteryLevel(ctx));

            jsObj.put(SDKUtils.encodeString("mcc"),
                    ConnectivityService.getNetworkMCC(ctx));
            jsObj.put(SDKUtils.encodeString("mnc"),
                    ConnectivityService.getNetworkMNC(ctx));

            jsObj.put(SDKUtils.encodeString("phoneType"),
                    ConnectivityService.getPhoneType(ctx));
            jsObj.put(SDKUtils.encodeString("simOperator"),
                    SDKUtils.encodeString(ConnectivityService.getSimOperator(ctx)));

            jsObj.put(SDKUtils.encodeString("lastUpdateTime"),
                    ApplicationContext.getLastUpdateTime(ctx));
            jsObj.put(SDKUtils.encodeString("firstInstallTime"),
                    ApplicationContext.getFirstInstallTime(ctx));
            jsObj.put(SDKUtils.encodeString("appVersion"),
                    SDKUtils.encodeString(ApplicationContext.getApplicationVersionName(ctx)));
        } catch (JSONException e) {
            e.printStackTrace();
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e.getStackTrace()[0].getMethodName()});
        }

        Object[] result = new Object[2];
        result[0] = jsObj.toString();
        result[1] = Boolean.valueOf(fail);

        return result;
    }

    private Object[] getApplicationParams(String productType, String demandSourceName) {
        boolean fail = false;

        JSONObject jsObj = new JSONObject();

        String appKey = "";
        String userId = "";

        Map<String, String> productExtraParams = null;


        if (!TextUtils.isEmpty(productType)) {
            SSAEnums.ProductType type = getStringProductTypeAsEnum(productType);
            if ((type == SSAEnums.ProductType.RewardedVideo) || (type == SSAEnums.ProductType.Interstitial)) {
                ProductParameters productParameters = this.mProductParametersCollection.getProductParameters(type);
                appKey = productParameters.appKey;
                userId = productParameters.userId;
                DemandSource demandSource = this.mDemandSourceManager.getDemandSourceByName(type, demandSourceName);
                if (demandSource != null) {
                    productExtraParams = demandSource.getExtraParams();
                    productExtraParams.put("demandSourceName", demandSourceName);
                }
            } else if (type == SSAEnums.ProductType.OfferWall) {
                appKey = this.mOWAppKey;
                userId = this.mOWUserId;
                productExtraParams = this.mOWExtraParameters;
            }
            try {
                jsObj.put("productType", productType);


            } catch (JSONException e) {

                e.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=noProductType"});
            }
        } else {
            fail = true;
        }


        if (!TextUtils.isEmpty(userId)) {
            try {
                jsObj.put(
                        SDKUtils.encodeString("applicationUserId"),
                        SDKUtils.encodeString(userId));
            } catch (JSONException e) {
                e.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=encodeAppUserId"});
            }
        } else {
            fail = true;
        }


        if (!TextUtils.isEmpty(appKey)) {
            try {
                jsObj.put(
                        SDKUtils.encodeString("applicationKey"),
                        SDKUtils.encodeString(appKey));
            } catch (JSONException e) {
                e.printStackTrace();
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=encodeAppKey"});
            }
        } else {
            fail = true;
        }


        if ((productExtraParams != null) && (!productExtraParams.isEmpty())) {
            for (Entry<String, String> entry : productExtraParams.entrySet()) {
                if (((String) entry.getKey()).equalsIgnoreCase("sdkWebViewCache")) {
                    setWebviewCache((String) entry.getValue());
                }
                try {
                    jsObj.put(SDKUtils.encodeString((String) entry.getKey()), SDKUtils.encodeString((String) entry.getValue()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=extraParametersToJson"});
                }
            }
        }

        Object[] result = new Object[2];
        result[0] = jsObj.toString();
        result[1] = Boolean.valueOf(fail);

        return result;
    }

    private Object[] getAppsStatus(String appIds, String requestId) {
        boolean fail = false;

        JSONObject result = new JSONObject();
        try {
            if ((!TextUtils.isEmpty(appIds)) && (!appIds.equalsIgnoreCase("null"))) {
                if ((!TextUtils.isEmpty(requestId)) && (!requestId.equalsIgnoreCase("null"))) {
                    Context ctx = getContext();
                    List<ApplicationInfo> packages = DeviceStatus.getInstalledApplications(ctx);


                    JSONArray appIdsArray = new JSONArray(appIds);

                    JSONObject bundleIds = new JSONObject();

                    for (int i = 0; i < appIdsArray.length(); i++) {
                        String appId = appIdsArray.getString(i).trim();

                        if (!TextUtils.isEmpty(appId)) {
                            JSONObject isInstalled = new JSONObject();
                            boolean found = false;

                            for (ApplicationInfo packageInfo : packages) {
                                if (appId.equalsIgnoreCase(packageInfo.packageName)) {
                                    isInstalled.put(IS_INSTALLED, true);
                                    bundleIds.put(appId, isInstalled);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                isInstalled.put(IS_INSTALLED, false);
                                bundleIds.put(appId, isInstalled);
                            }
                        }
                    }

                    result.put(RESULT, bundleIds);
                    result.put(REQUEST_ID, requestId);
                } else {
                    fail = true;
                    result.put("error", "requestId is null or empty");
                }
            } else {
                fail = true;
                result.put("error", "appIds is null or empty");
            }
        } catch (Exception e) {
            fail = true;
        }

        Object[] finalResult = new Object[2];
        finalResult[0] = result.toString();
        finalResult[1] = Boolean.valueOf(fail);

        return finalResult;
    }


    public void onFileDownloadSuccess(SSAFile file) {
        if (file.getFile().contains("mobileController.html")) {
            load(1);
        } else {
            assetCached(file.getFile(), file.getPath());
        }
    }


    public void onFileDownloadFail(SSAFile file) {
        if (file.getFile().contains("mobileController.html")) {
            this.mGlobalControllerTimer.cancel();


            Collection<DemandSource> demandSources = this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.RewardedVideo);
            for (DemandSource demandSource : demandSources) {
                if (demandSource.getDemandSourceInitState() == 1) {
                    sendProductErrorMessage(SSAEnums.ProductType.RewardedVideo, demandSource.getDemandSourceName());
                }
            }

            demandSources = this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.Interstitial);
            for (DemandSource demandSource : demandSources) {
                if (demandSource.getDemandSourceInitState() == 1) {
                    sendProductErrorMessage(SSAEnums.ProductType.Interstitial, demandSource.getDemandSourceName());
                }
            }

            if (this.mOWmiss) {
                sendProductErrorMessage(SSAEnums.ProductType.OfferWall, null);
            }

            if (this.mOWCreditsMiss) {
                sendProductErrorMessage(SSAEnums.ProductType.OfferWallCredits, null);
            }
        } else {
            assetCachedFailed(file.getFile(), file.getPath(), file.getErrMsg());
        }
    }


    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        Logger.i(this.TAG, url + " " + mimetype);
    }


    private void toastingErrMsg(final String methodName, String value) {
        SSAObj ssaObj = new SSAObj(value);
        final String message = ssaObj.getString("errMsg");

        if (!TextUtils.isEmpty(message)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (IronSourceWebView.this.getDebugMode() == SSAEnums.DebugMode.MODE_3.getValue()) {
                        Toast.makeText(IronSourceWebView.this.getCurrentActivityContext(), methodName + " : " + message, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void setControllerKeyPressed(String value) {
        this.mControllerKeyPressed = value;
    }


    public String getControllerKeyPressed() {
        String keyPressed = this.mControllerKeyPressed;


        setControllerKeyPressed("interrupt");

        return keyPressed;
    }

    public void deviceStatusChanged(String networkType) {
        String params = parseToJson("connectionType", networkType, null, null, null, null, null, null, null, false);
        String script = generateJSToInject("deviceStatusChanged", params);
        injectJavascript(script);
    }

    public void engageEnd(String action) {
        if (action.equals("forceClose")) {
            closeWebView();
        }

        String params = parseToJson("action", action, null, null, null, null, null, null, null, false);
        String script = generateJSToInject("engageEnd", params);
        injectJavascript(script);
    }

    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (IronSourceWebView.this.mControllerState == SSAEnums.ControllerState.Ready) {
                String networkType = "none";
                if (ConnectivityService.isConnectedWifi(context)) {
                    networkType = "wifi";
                } else if (ConnectivityService.isConnectedMobile(context)) {
                    networkType = "3g";
                }

                IronSourceWebView.this.deviceStatusChanged(networkType);
            }
        }
    };
    private OnWebViewChangeListener mChangeListener;

    public void registerConnectionReceiver(Context context) {
        context.registerReceiver(this.mConnectionReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void unregisterConnectionReceiver(Context context) {
        try {
            context.unregisterReceiver(this.mConnectionReceiver);

        } catch (IllegalArgumentException localIllegalArgumentException) {
        } catch (Exception e1) {

            Log.e(this.TAG, "unregisterConnectionReceiver - " + e1);
            new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=" + e1.getStackTrace()[0].getMethodName()});
        }
    }

    public void pause() {
        if (Build.VERSION.SDK_INT > 10) {
            try {
                onPause();
            } catch (Throwable e) {
                Logger.i(this.TAG, "WebViewController: pause() - " + e);
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewPause"});
            }
        }
    }


    public void resume() {
        if (Build.VERSION.SDK_INT > 10) {
            try {
                onResume();
            } catch (Throwable e) {
                Logger.i(this.TAG, "WebViewController: onResume() - " + e);
                new IronSourceAsyncHttpRequestTask().execute(new String[]{"https://www.supersonicads.com/mobile/sdk5/log?method=webviewResume"});
            }
        }
    }


    public void setOnWebViewControllerChangeListener(OnWebViewChangeListener listener) {
        this.mChangeListener = listener;
    }

    public FrameLayout getLayout() {
        return this.mControllerLayout;
    }

    public boolean inCustomView() {
        return this.mCustomView != null;
    }

    public void hideCustomView() {
        this.mWebChromeClient.onHideCustomView();
    }

    public static enum State {
        Display,
        Gone;

        private State() {
        }
    }

    private void setWebviewCache(String value) {
        if (value.equalsIgnoreCase("0")) {
            getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        }
    }

    public boolean handleSearchKeysURLs(String url) throws Exception {
        List<String> searchKeys = IronSourceSharedPrefHelper.getSupersonicPrefHelper().getSearchKeys();
        try {
            if ((searchKeys != null) && (!searchKeys.isEmpty())) {
                for (String key : searchKeys) {
                    if (url.contains(key)) {
                        UrlHandler.openUrl(getCurrentActivityContext(), url);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void setState(State state) {
        this.mState = state;
    }

    public State getState() {
        return this.mState;
    }

    private void sendProductErrorMessage(SSAEnums.ProductType type, String demnadSourceName) {
        String action = "";

        switch (type) {
            case RewardedVideo:
                action = "Init RV";
                break;


            case Interstitial:
                action = "Init IS";
                break;


            case OfferWall:
                action = "Init OW";
                break;


            case OfferWallCredits:
                action = "Show OW Credits";
        }


        triggerOnControllerInitProductFail(
                SDKUtils.createErrorMessage(action, "Initiating Controller"), type, demnadSourceName);
    }


    public void destroy() {
        super.destroy();

        if (this.downloadManager != null) {
            this.downloadManager.release();
        }
        if (this.mConnectionReceiver != null) {
            this.mConnectionReceiver = null;
        }
        this.mUiHandler = null;
        this.mCurrentActivityContext = null;
    }

    private String generateJSToInject(String funToCall) {
        StringBuilder script = new StringBuilder();
        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String parameters) {
        StringBuilder script = new StringBuilder();

        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("?parameters=").append(parameters).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String successFunc, String failFunc) {
        StringBuilder script = new StringBuilder();

        script.append("SSA_CORE.SDKController.runFunction('").append(funToCall).append("','").append(successFunc).append("','")
                .append(failFunc).append("');");
        return script.toString();
    }

    private String generateJSToInject(String funToCall, String parameters, String successFunc, String failFunc) {
        StringBuilder script = new StringBuilder();

        script.append("SSA_CORE.SDKController.runFunction('")
                .append(funToCall).append("?parameters=").append(parameters).append("','")
                .append(successFunc).append("','").append(failFunc).append("');");
        return script.toString();
    }

    public AdUnitsState getSavedState() {
        return this.mSavedState;
    }

    public void restoreState(AdUnitsState state) {
        synchronized (this.mSavedStateLocker) {
            if ((state.shouldRestore()) && (this.mControllerState.equals(SSAEnums.ControllerState.Ready))) {
                Log.d(this.TAG, "restoreState(state:" + state + ")");


                int lastAd = state.getDisplayedProduct();
                if (lastAd != -1) {


                    if (lastAd == SSAEnums.ProductType.RewardedVideo.ordinal()) {
                        Log.d(this.TAG, "onRVAdClosed()");
                        SSAEnums.ProductType type = SSAEnums.ProductType.RewardedVideo;
                        String demandSourceName = state.getDisplayedDemandSourceName();
                        DSAdProductListener listener = getAdProductListenerByProductType(type);
                        if ((listener != null) && (!TextUtils.isEmpty(demandSourceName))) {
                            listener.onAdProductClose(type, demandSourceName);
                        }

                    } else if (lastAd == SSAEnums.ProductType.Interstitial.ordinal()) {
                        Log.d(this.TAG, "onInterstitialAdClosed()");
                        SSAEnums.ProductType type = SSAEnums.ProductType.Interstitial;
                        String demandSourceName = state.getDisplayedDemandSourceName();
                        DSAdProductListener listener = getAdProductListenerByProductType(type);
                        if ((listener != null) && (!TextUtils.isEmpty(demandSourceName))) {
                            listener.onAdProductClose(type, demandSourceName);
                        }

                    } else if (lastAd == SSAEnums.ProductType.OfferWall.ordinal()) {
                        Log.d(this.TAG, "onOWAdClosed()");
                        if (this.mOnOfferWallListener != null) {
                            this.mOnOfferWallListener.onOWAdClosed();
                        }
                    }

                    state.adOpened(-1);
                    state.setDisplayedDemandSourceName(null);
                } else {
                    Log.d(this.TAG, "No ad was opened");
                }


                String appKey = state.getInterstitialAppKey();
                String userId = state.getInterstitialUserId();

                Collection<DemandSource> demandSources = this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.Interstitial);
                for (DemandSource demandSource : demandSources) {
                    if (demandSource.getDemandSourceInitState() == 2) {
                        Log.d(this.TAG, "initInterstitial(appKey:" + appKey + ", userId:" + userId + ", demandSource:" + demandSource.getDemandSourceName() + ")");
                        initInterstitial(appKey, userId, demandSource, this.mDSInterstitialListener);
                    }
                }


                appKey = state.getRVAppKey();
                userId = state.getRVUserId();

                demandSources = this.mDemandSourceManager.getDemandSources(SSAEnums.ProductType.RewardedVideo);
                for (DemandSource demandSource : demandSources) {
                    if (demandSource.getDemandSourceInitState() == 2) {
                        String demandSourceName = demandSource.getDemandSourceName();
                        Log.d(this.TAG, "onRVNoMoreOffers()");
                        this.mDSRewardedVideoListener.onRVNoMoreOffers(demandSourceName);
                        Log.d(this.TAG, "initRewardedVideo(appKey:" + appKey + ", userId:" + userId + ", demandSource:" + demandSourceName + ")");
                        initRewardedVideo(appKey, userId, demandSource, this.mDSRewardedVideoListener);
                    }
                }

                state.setShouldRestore(false);
            }

            this.mSavedState = state;
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            if (!this.mChangeListener.onBackButtonPressed()) {
                return super.onKeyDown(keyCode, event);
            }
            return true;
        }


        return super.onKeyDown(keyCode, event);
    }

    void runOnUiThread(Runnable task) {
        this.mUiHandler.post(task);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/IronSourceWebView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */