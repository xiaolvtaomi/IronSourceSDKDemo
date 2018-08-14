package com.ironsource.environment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class UrlHandler {
    public static void openUrl(Context context, String url)
            throws Exception {
        if (url == null) {
            throw new Exception("url is null");
        }

        context.startActivity(new Intent("android.intent.action.VIEW")
                .setData(Uri.parse(url)));
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/environment/UrlHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */