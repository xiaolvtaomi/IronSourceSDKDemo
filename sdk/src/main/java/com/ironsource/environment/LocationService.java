package com.ironsource.environment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.security.Permission;
import java.util.List;


public class LocationService {
    private static String TAG = LocationService.class.getSimpleName();


    public static Location getLastLocation(Context context) {
        Location bestLocation = null;
        long bestLocationTime = Long.MIN_VALUE;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager != null ? locationManager.getAllProviders() : null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission Not Granted (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION)");
            return bestLocation;
        }
        if (providers != null) {
            for (String provider : providers) {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    long currentTime = location.getTime();
                    if (currentTime > bestLocationTime) {
                        bestLocation = location;
                    }
                }
            }
        }

        return bestLocation;
    }

    @SuppressLint({"MissingPermission"})
    public static void getPreciseLocation(Context context, final ISLocationListener isLocationListener) {
        if (!ApplicationContext.isPermissionGranted(context, "android.permission.ACCESS_FINE_LOCATION")) {
            Log.d(TAG, "Location Permission Not Granted (ACCESS_FINE_LOCATION)");
            if (isLocationListener != null) {
                isLocationListener.onLocationChanged(null);
                return;
            }
        }
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if ((isLocationListener != null) && (!locationManager.isProviderEnabled("gps"))) {
                Log.d(TAG, "GPS Provider is turned off");
                isLocationListener.onLocationChanged(null);
                return;
            }

            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    Log.d("LocationService", "onLocationChanged " + location.getProvider());
                    if (isLocationListener != null) {
                        isLocationListener.onLocationChanged(location);
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d("LocationService", "onStatusChanged " + provider);
                }

                public void onProviderEnabled(String provider) {
                    Log.d("LocationService", "onProviderEnabled " + provider);
                }

                public void onProviderDisabled(String provider) {
                    Log.d("LocationService", "onProviderDisabled " + provider);
                }

            };
            Criteria c = new Criteria();
            locationManager.requestSingleUpdate(c, locationListener, Looper.myLooper());
            return;
        } catch (Exception localException) {
            if (isLocationListener != null) {
                isLocationListener.onLocationChanged(null);
            }
        }
    }

    public static boolean locationServicesEnabled(Context context) {
        try {
            if ((!ApplicationContext.isPermissionGranted(context, "android.permission.ACCESS_FINE_LOCATION")) &&
                    (!ApplicationContext.isPermissionGranted(context, "android.permission.ACCESS_COARSE_LOCATION"))) {
                Log.d(TAG, "Location Permission Not Granted (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION)");
                return false;
            }

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            boolean network_enabled = false;
            try {
                gps_enabled = locationManager.isProviderEnabled("gps");
            } catch (Exception localException) {
            }

            try {
                network_enabled = locationManager.isProviderEnabled("network");
            } catch (Exception localException1) {
            }


            return (gps_enabled) || (network_enabled);
        } catch (Exception localException2) {
        }

        return false;
    }

    public static abstract interface ISLocationListener {
        public abstract void onLocationChanged(Location paramLocation);
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/environment/LocationService.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */