package com.ironsource.sdk.controller;

import android.text.TextUtils;

import com.ironsource.sdk.data.DemandSource;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ProductType;
import com.ironsource.sdk.listeners.OnAdProductListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class DemandSourceManager {
    private Map<String, DemandSource> mRewardedVideoDemandSourceMap;
    private Map<String, DemandSource> mInterstitialDemandSourceMap;

    public DemandSourceManager() {
        this.mRewardedVideoDemandSourceMap = new LinkedHashMap();
        this.mInterstitialDemandSourceMap = new LinkedHashMap();
    }

    private Map<String, DemandSource> getMapByProductType(SSAEnums.ProductType productType) {
        if (productType.name().equalsIgnoreCase(SSAEnums.ProductType.RewardedVideo.name()))
            return this.mRewardedVideoDemandSourceMap;
        if (productType.name().equalsIgnoreCase(SSAEnums.ProductType.Interstitial.name())) {
            return this.mInterstitialDemandSourceMap;
        }
        return null;
    }

    public Collection<DemandSource> getDemandSources(SSAEnums.ProductType productType) {
        Map<String, DemandSource> productDemandMap = getMapByProductType(productType);
        if (productDemandMap != null) {
            return productDemandMap.values();
        }
        return new ArrayList();
    }

    public DemandSource getDemandSourceByName(SSAEnums.ProductType productType, String demandSourceName) {
        if (!TextUtils.isEmpty(demandSourceName)) {
            Map<String, DemandSource> productDemandMap = getMapByProductType(productType);
            if (productDemandMap != null) {
                return (DemandSource) productDemandMap.get(demandSourceName);
            }
        }
        return null;
    }

    private void put(SSAEnums.ProductType productType, String demandSourceName, DemandSource demandSource) {
        if ((!TextUtils.isEmpty(demandSourceName)) && (demandSource != null)) {
            Map<String, DemandSource> productDemandMap = getMapByProductType(productType);
            if (productDemandMap != null) {
                productDemandMap.put(demandSourceName, demandSource);
            }
        }
    }

    public DemandSource createDemandSource(SSAEnums.ProductType type, String demandSourceName, Map<String, String> demandExtParam, OnAdProductListener listener) {
        DemandSource demandSource = new DemandSource(demandSourceName, demandExtParam, listener);
        put(type, demandSourceName, demandSource);
        return demandSource;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/DemandSourceManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */