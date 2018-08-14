package com.ironsource.mediationsdk.config;

import com.ironsource.mediationsdk.logger.IronSourceError;


public class ConfigValidationResult {
    private boolean mIsValid;
    private IronSourceError mIronSourceError;

    public ConfigValidationResult() {
        this.mIsValid = true;
        this.mIronSourceError = null;
    }

    public void setInvalid(IronSourceError error) {
        this.mIsValid = false;
        this.mIronSourceError = error;
    }

    public void setValid() {
        this.mIsValid = true;
        this.mIronSourceError = null;
    }

    public boolean isValid() {
        return this.mIsValid;
    }

    public IronSourceError getIronSourceError() {
        return this.mIronSourceError;
    }

    public String toString() {
        if (isValid()) {
            return "valid:" + this.mIsValid;
        }
        return "valid:" + this.mIsValid + ", IronSourceError:" + this.mIronSourceError;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/config/ConfigValidationResult.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */