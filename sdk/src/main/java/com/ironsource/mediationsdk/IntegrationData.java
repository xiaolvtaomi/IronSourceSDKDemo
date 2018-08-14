package com.ironsource.mediationsdk;

import android.util.Pair;

import java.util.ArrayList;


public class IntegrationData {
    public String name;
    public String version;
    public String[] activities;
    public String sdkName;
    public ArrayList<Pair<String, String>> externalLibs;
    public String[] broadcastReceivers;
    public String[] services;
    public boolean validateWriteExternalStorage;

    public IntegrationData(String name, String version) {
        this.name = name;
        this.version = version;
        this.activities = null;
        this.sdkName = null;
        this.externalLibs = null;
        this.broadcastReceivers = null;
        this.services = null;
        this.validateWriteExternalStorage = false;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/IntegrationData.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */