package com.ironsource.sdk.controller;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;

import com.ironsource.environment.DeviceStatus;
import com.ironsource.sdk.handlers.BackButtonHandler;
import com.ironsource.sdk.listeners.OnWebViewChangeListener;

public class ControllerView
        extends FrameLayout
        implements OnWebViewChangeListener {
    private Context mContext;
    private IronSourceWebView mWebViewController;

    public ControllerView(Context context) {
        super(context);

        this.mContext = context;

        setClickable(true);
    }


    public void showInterstitial(IronSourceWebView webView) {
        this.mWebViewController = webView;
        this.mWebViewController.setOnWebViewControllerChangeListener(this);
        this.mWebViewController.requestFocus();

        this.mContext = this.mWebViewController.getCurrentActivityContext();

        setPaddingByOrientation(getStatusBarPadding(), getNavigationBarPadding());

        addViewToWindow();
    }


    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWebViewController.resume();
        this.mWebViewController.viewableChange(true, "main");
    }


    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWebViewController.pause();
        this.mWebViewController.viewableChange(false, "main");

        if (this.mWebViewController != null) {
            this.mWebViewController.setState(IronSourceWebView.State.Gone);
            this.mWebViewController.removeVideoEventsListener();
        }

        removeAllViews();
    }


    private void addViewToWindow() {
        Activity activity = (Activity) this.mContext;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup decorView = ControllerView.this.getWindowDecorViewGroup();
                if (decorView != null) {
                    decorView.addView(ControllerView.this);
                }
            }
        });
    }


    private void removeViewFromWindow() {
        Activity activity = (Activity) this.mContext;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup decorView = ControllerView.this.getWindowDecorViewGroup();
                if (decorView != null) {
                    decorView.removeView(ControllerView.this);
                }
            }
        });
    }

    private ViewGroup getWindowDecorViewGroup() {
        Activity activity = (Activity) this.mContext;

        if (activity != null) {
            return (ViewGroup) activity.getWindow().getDecorView();
        }
        return null;
    }

    private void setPaddingByOrientation(int statusBarHeight, int navigationBarSize) {
        try {
            if (this.mContext != null) {
                int orientation = DeviceStatus.getDeviceOrientation(this.mContext);
                if (orientation == 1) {
                    setPadding(0, statusBarHeight, 0, navigationBarSize);
                } else if (orientation == 2) {
                    setPadding(0, statusBarHeight, navigationBarSize, 0);
                }
            }
        } catch (Exception localException) {
        }
    }

    private int getStatusBarPadding() {
        Activity activity = (Activity) this.mContext;

        boolean isFullScreen = (activity.getWindow().getAttributes().flags & 0x400) != 0;
        if (isFullScreen) {
            return 0;
        }
        int top = getStatusBarHeight();
        return top > 0 ? top : 0;
    }


    private int getStatusBarHeight() {
        int result = 0;
        try {
            if (this.mContext != null) {
                int resourceId = this.mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    result = this.mContext.getResources().getDimensionPixelSize(resourceId);
                }
            }
        } catch (Exception localException) {
        }
        return result;
    }


    private int getNavigationBarPadding() {
        Activity activity = (Activity) this.mContext;
        try {
            if (Build.VERSION.SDK_INT > 9) {
                Rect screenRect = new Rect();
                activity.getWindow().getDecorView().getDrawingRect(screenRect);

                Rect visibleFrame = new Rect();
                activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);


                int orientation = DeviceStatus.getDeviceOrientation(activity);
                if (orientation == 1) {
                    return screenRect.bottom - visibleFrame.bottom > 0 ? screenRect.bottom - visibleFrame.bottom : 0;
                }
                return screenRect.right - visibleFrame.right > 0 ? screenRect.right - visibleFrame.right : 0;
            }
        } catch (Exception localException) {
        }

        return 0;
    }


    public void onCloseRequested() {
        removeViewFromWindow();
    }


    public void onOrientationChanged(String orientation, int rotation) {
    }


    public boolean onBackButtonPressed() {
        return BackButtonHandler.getInstance().handleBackButton((Activity) this.mContext);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/ControllerView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */