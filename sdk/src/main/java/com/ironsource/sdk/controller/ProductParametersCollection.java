package com.ironsource.sdk.controller;

import com.ironsource.sdk.data.ProductParameters;
import com.ironsource.sdk.data.SSAEnums;
import com.ironsource.sdk.data.SSAEnums.ProductType;

import java.util.HashMap;
import java.util.Map;


public class ProductParametersCollection {
    private Map<SSAEnums.ProductType, ProductParameters> mProductParameters;

    public ProductParametersCollection() {
        this.mProductParameters = new HashMap();
    }

    public ProductParameters setProductParameters(SSAEnums.ProductType type, String appKey, String userId) {
        ProductParameters productParameters = new ProductParameters(appKey, userId);
        this.mProductParameters.put(type, productParameters);
        return productParameters;
    }

    public ProductParameters getProductParameters(SSAEnums.ProductType type) {
        ProductParameters productParameters = null;
        if (type != null) {
            productParameters = (ProductParameters) this.mProductParameters.get(type);
        }
        return productParameters;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/controller/ProductParametersCollection.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */