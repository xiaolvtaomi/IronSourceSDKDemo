package com.ironsource.sdk.utils;

import android.content.Context;

import com.ironsource.environment.DeviceStatus;


public class DeviceProperties {
    private static DeviceProperties mInstance = null;
    private String mDeviceOem;
    private String mDeviceModel;
    private String mDeviceOsType;
    private String mDeviceOsVersion;
    private int mDeviceApiLevel;
    private String mDeviceCarrier;

    private DeviceProperties(Context context) {
        this.mDeviceOem = DeviceStatus.getDeviceOEM();
        this.mDeviceModel = DeviceStatus.getDeviceModel();
        this.mDeviceOsType = DeviceStatus.getDeviceOs();
        this.mDeviceOsVersion = DeviceStatus.getAndroidOsVersion();
        this.mDeviceApiLevel = DeviceStatus.getAndroidAPIVersion();
        this.mDeviceCarrier = DeviceStatus.getMobileCarrier(context);
    }

    public static DeviceProperties getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DeviceProperties(context);
        }
        return mInstance;
    }

    public String getDeviceOem() {
        return this.mDeviceOem;
    }


    public String getDeviceModel() {
        return this.mDeviceModel;
    }


    public String getDeviceOsType() {
        return this.mDeviceOsType;
    }


    public String getDeviceOsVersion() {
        return this.mDeviceOsVersion;
    }

    public int getDeviceApiLevel() {
        return this.mDeviceApiLevel;
    }

    public String getDeviceCarrier() {
        return this.mDeviceCarrier;
    }

    public static String getSupersonicSdkVersion() {
        return "5.53";
    }

    public static void release() {
        mInstance = null;
    }

    public float getDeviceVolume(Context context) {
        return DeviceStatus.getSystemVolumePercent(context);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/utils/DeviceProperties.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */