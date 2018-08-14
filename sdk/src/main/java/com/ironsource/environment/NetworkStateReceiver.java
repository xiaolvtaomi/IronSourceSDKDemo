package com.ironsource.environment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkStateReceiver
        extends BroadcastReceiver {
    private ConnectivityManager mManager;
    private NetworkStateReceiverListener mListener;
    private boolean mConnected;

    public NetworkStateReceiver(Context context, NetworkStateReceiverListener listener) {
        this.mListener = listener;
        this.mManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        checkAndSetState();
    }

    public void onReceive(Context context, Intent intent) {
        if ((intent == null) || (intent.getExtras() == null)) {
            return;
        }
        if (checkAndSetState())
            notifyState();
    }

    private boolean checkAndSetState() {
        boolean prev = this.mConnected;
        NetworkInfo activeNetwork = this.mManager.getActiveNetworkInfo();
        this.mConnected = ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting()));
        return prev != this.mConnected;
    }

    private void notifyState() {
        if (this.mListener != null) {
            if (this.mConnected) {
                this.mListener.onNetworkAvailabilityChanged(true);
            } else {
                this.mListener.onNetworkAvailabilityChanged(false);
            }
        }
    }

    public static abstract interface NetworkStateReceiverListener {
        public abstract void onNetworkAvailabilityChanged(boolean paramBoolean);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/environment/NetworkStateReceiver.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */