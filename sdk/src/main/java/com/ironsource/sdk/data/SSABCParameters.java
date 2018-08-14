package com.ironsource.sdk.data;

public class SSABCParameters extends SSAObj {
    private String CONNECTION_RETRIES = "connectionRetries";

    private String mConnectionRetries;


    public SSABCParameters() {
    }

    public SSABCParameters(String value) {
        super(value);

        if (containsKey(this.CONNECTION_RETRIES)) {
            setConnectionRetries(getString(this.CONNECTION_RETRIES));
        }
    }

    public String getConnectionRetries() {
        return this.mConnectionRetries;
    }

    public void setConnectionRetries(String connectionRetries) {
        this.mConnectionRetries = connectionRetries;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/SSABCParameters.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */