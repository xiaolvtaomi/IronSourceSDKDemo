package com.ironsource.mediationsdk.config;

import java.util.Arrays;

public class ConfigFile {
    private static ConfigFile mInstance;
    private String mPluginType;
    private String mPluginVersion;
    private String mPluginFrameworkVersion;
    private String[] mSupportedPlugins = {"Unity", "AdobeAir", "Xamarin", "Corona", "AdMob", "MoPub"};

    public static synchronized ConfigFile getConfigFile() {
        if (mInstance == null) {
            mInstance = new ConfigFile();
        }
        return mInstance;
    }


    public void setPluginData(String pluginType, String pluginVersion, String pluginFrameworkVersion) {
        if (pluginType != null) {
            if (Arrays.asList(this.mSupportedPlugins).contains(pluginType)) {
                this.mPluginType = pluginType;
            } else {
                this.mPluginType = null;
            }
        }
        if (pluginVersion != null) {
            this.mPluginVersion = pluginVersion;
        }
        if (pluginFrameworkVersion != null) {
            this.mPluginFrameworkVersion = pluginFrameworkVersion;
        }
    }


    public String getPluginType() {
        return this.mPluginType;
    }


    public String getPluginVersion() {
        return this.mPluginVersion;
    }


    public String getPluginFrameworkVersion() {
        return this.mPluginFrameworkVersion;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/config/ConfigFile.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */