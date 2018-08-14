package com.ironsource.mediationsdk.server;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Pair;

import com.ironsource.environment.ApplicationContext;
import com.ironsource.mediationsdk.config.ConfigFile;
import com.ironsource.mediationsdk.utils.IronSourceAES;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class ServerURL {
    private static String BASE_URL_PREFIX = "https://init.supersonicads.com/sdk/v";
    private static String BASE_URL_SUFFIX = "?request=";

    private static final String PLATFORM_KEY = "platform";
    private static final String APPLICATION_KEY = "applicationKey";
    private static final String APPLICATION_USER_ID = "applicationUserId";
    private static final String SDK_VERSION = "sdkVersion";
    private static final String PLUGIN_TYPE = "pluginType";
    private static final String PLUGIN_VERSION = "pluginVersion";
    private static final String PLUGIN_FW_VERSION = "plugin_fw_v";
    private static final String GAID = "advId";
    private static final String SERR = "serr";
    private static final String APPLICATION_VERSION = "appVer";
    private static final String OS_VERSION = "osVer";
    private static final String DEVICE_MODEL = "devModel";
    private static final String DEVICE_MAKE = "devMake";
    private static final String CONNECTION_TYPE = "connType";
    private static final String MEDIATION_TYPE = "mt";
    private static final String ANDROID = "android";
    private static final String IMPRESSION = "impression";
    private static final String PLACEMENT = "placementId";
    private static final String EQUAL = "=";
    private static final String AMPERSAND = "&";

    public static String getCPVProvidersURL(Context context, String applicationKey, String applicationUserId, String gaid, String mediationType, Vector<Pair<String, String>> segmentParamVector)
            throws UnsupportedEncodingException {
        Vector<Pair<String, String>> array = new Vector();

        array.add(new Pair("platform", "android"));
        array.add(new Pair("applicationKey", applicationKey));
        array.add(new Pair("applicationUserId", applicationUserId));
        array.add(new Pair("sdkVersion", IronSourceUtils.getSDKVersion()));
        if (IronSourceUtils.getSerr() == 0)
            array.add(new Pair("serr", String.valueOf(IronSourceUtils.getSerr())));
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginType()))
            array.add(new Pair("pluginType", ConfigFile.getConfigFile().getPluginType()));
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginVersion()))
            array.add(new Pair("pluginVersion", ConfigFile.getConfigFile().getPluginVersion()));
        if (!TextUtils.isEmpty(ConfigFile.getConfigFile().getPluginFrameworkVersion()))
            array.add(new Pair("plugin_fw_v", ConfigFile.getConfigFile().getPluginFrameworkVersion()));
        if (!TextUtils.isEmpty(gaid))
            array.add(new Pair("advId", gaid));
        if (!TextUtils.isEmpty(mediationType)) {
            array.add(new Pair("mt", mediationType));
        }
        String appVersion = ApplicationContext.getPublisherApplicationVersion(context, context.getPackageName());
        if (!TextUtils.isEmpty(appVersion)) {
            array.add(new Pair("appVer", appVersion));
        }
        int osVersion = VERSION.SDK_INT;
        array.add(new Pair("osVer", osVersion + ""));
        String make = Build.MANUFACTURER;
        array.add(new Pair("devMake", make));
        String model = Build.MODEL;
        array.add(new Pair("devModel", model));

        String connection = IronSourceUtils.getConnectionType(context);
        if (!TextUtils.isEmpty(connection)) {
            array.add(new Pair("connType", connection));
        }
        if (segmentParamVector != null) {
            array.addAll(segmentParamVector);
        }
        String params = createURLParams(array);


        String encryptedParams = IronSourceAES.encode("C38FB23A402222A0C17D34A92F971D1F", params);
        String encodedEncryptedParams = URLEncoder.encode(encryptedParams, "UTF-8");
        return getBaseUrl(IronSourceUtils.getSDKVersion()) + encodedEncryptedParams;
    }


    public static String getRequestURL(String requestUrl, boolean hit, int placementId)
            throws UnsupportedEncodingException {
        Vector<Pair<String, String>> array = new Vector();

        array.add(new Pair("impression", Boolean.toString(hit)));
        array.add(new Pair("placementId", Integer.toString(placementId)));

        String params = createURLParams(array);
        return requestUrl + "&" + params;
    }

    private static String createURLParams(Vector<Pair<String, String>> array) throws UnsupportedEncodingException {
        String str = "";
        for (Pair<String, String> pair : array) {
            if (str.length() > 0)
                str = str + "&";
            str = str + (String) pair.first + "=" + URLEncoder.encode((String) pair.second, "UTF-8");
        }
        return str;
    }

    private static String getBaseUrl(String sdkVersion) {
        return BASE_URL_PREFIX + sdkVersion + BASE_URL_SUFFIX;
    }

    private static String getConnectionTypeForInit(Context c) {
        String CONNECTION_WIFI = "WIFI";
        String CONNECTION_CELLULAR = "MOBILE";
        String CONNECTION_TYPE_WIFI = "wifi";
        String CONNECTION_TYPE_CELLULAR = "cellular";

        if (c == null) {
            return "cellular";
        }

        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return "cellular";
        }

        NetworkInfo info = cm.getActiveNetworkInfo();

        if ((info != null) && (info.isConnected()) &&
                (info.getTypeName().equalsIgnoreCase("WIFI"))) {
            return "wifi";
        }


        return "cellular";
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/server/ServerURL.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */