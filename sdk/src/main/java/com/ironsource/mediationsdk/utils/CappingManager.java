package com.ironsource.mediationsdk.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ironsource.mediationsdk.model.BannerPlacement;
import com.ironsource.mediationsdk.model.InterstitialPlacement;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.model.PlacementAvailabilitySettings;
import com.ironsource.mediationsdk.model.PlacementCappingType;

import java.util.Calendar;
import java.util.TimeZone;


public class CappingManager {
    private static final String IS_DELIVERY_ENABLED = "CappingManager.IS_DELIVERY_ENABLED";
    private static final String IS_CAPPING_ENABLED = "CappingManager.IS_CAPPING_ENABLED";
    private static final String IS_PACING_ENABLED = "CappingManager.IS_PACING_ENABLED";
    private static final String MAX_NUMBER_OF_SHOWS = "CappingManager.MAX_NUMBER_OF_SHOWS";
    private static final String CAPPING_TYPE = "CappingManager.CAPPING_TYPE";
    private static final String SECONDS_BETWEEN_SHOWS = "CappingManager.SECONDS_BETWEEN_SHOWS";
    private static final String CURRENT_NUMBER_OF_SHOWS = "CappingManager.CURRENT_NUMBER_OF_SHOWS";
    private static final String CAPPING_TIME_THRESHOLD = "CappingManager.CAPPING_TIME_THRESHOLD";
    private static final String TIME_OF_THE_PREVIOUS_SHOW = "CappingManager.TIME_OF_THE_PREVIOUS_SHOW";

    public static synchronized void addCappingInfo(Context context, InterstitialPlacement placement) {
        if ((context == null) || (placement == null)) {
            return;
        }
        PlacementAvailabilitySettings availabilitySettings = placement.getPlacementAvailabilitySettings();
        if (availabilitySettings == null) {
            return;
        }
        addCappingInfo(context, "Interstitial", placement
                .getPlacementName(), availabilitySettings);
    }

    public static synchronized void addCappingInfo(Context context, Placement placement) {
        if ((context == null) || (placement == null)) {
            return;
        }
        PlacementAvailabilitySettings availabilitySettings = placement.getPlacementAvailabilitySettings();
        if (availabilitySettings == null) {
            return;
        }
        addCappingInfo(context, "Rewarded Video", placement
                .getPlacementName(), availabilitySettings);
    }

    public static synchronized void addCappingInfo(Context context, BannerPlacement placement) {
        if ((context == null) || (placement == null)) {
            return;
        }
        PlacementAvailabilitySettings availabilitySettings = placement.getPlacementAvailabilitySettings();
        if (availabilitySettings == null) {
            return;
        }
        addCappingInfo(context, "Banner", placement
                .getPlacementName(), availabilitySettings);
    }

    public static synchronized ECappingStatus isPlacementCapped(Context context, InterstitialPlacement placement) {
        if ((context == null) || (placement == null) || (placement.getPlacementAvailabilitySettings() == null)) {
            return ECappingStatus.NOT_CAPPED;
        }
        return isPlacementCapped(context, "Interstitial", placement.getPlacementName());
    }

    public static synchronized boolean isBnPlacementCapped(Context context, String placementName) {
        return isPlacementCapped(context, "Banner", placementName) != ECappingStatus.NOT_CAPPED;
    }

    public static synchronized ECappingStatus isPlacementCapped(Context context, Placement placement) {
        if ((context == null) || (placement == null) || (placement.getPlacementAvailabilitySettings() == null)) {
            return ECappingStatus.NOT_CAPPED;
        }
        return isPlacementCapped(context, "Rewarded Video", placement.getPlacementName());
    }

    public static synchronized void incrementShowCounter(Context context, InterstitialPlacement placement) {
        if (placement != null) {
            incrementShowCounter(context, "Interstitial", placement.getPlacementName());
        }
    }

    public static synchronized void incrementShowCounter(Context context, Placement placement) {
        if (placement != null) {
            incrementShowCounter(context, "Rewarded Video", placement.getPlacementName());
        }
    }

    public static synchronized void incrementBnShowCounter(Context context, String placementName) {
        if (!TextUtils.isEmpty(placementName)) {
            incrementShowCounter(context, "Banner", placementName);
        }
    }


    private static String constructSharedPrefsKey(String adUnit, String baseConst, String placementName) {
        return adUnit + "_" + baseConst + "_" + placementName;
    }

    private static ECappingStatus isPlacementCapped(Context context, String adUnit, String placementName) {
        long currentTime = System.currentTimeMillis();

        String deliveryKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_DELIVERY_ENABLED", placementName);
        boolean isDeliveryEnabled = IronSourceUtils.getBooleanFromSharedPrefs(context, deliveryKey, true);
        if (!isDeliveryEnabled) {
            return ECappingStatus.CAPPED_PER_DELIVERY;
        }


        String isPacingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_PACING_ENABLED", placementName);
        boolean isPacingEnabled = IronSourceUtils.getBooleanFromSharedPrefs(context, isPacingEnabledKey, false);
        if (isPacingEnabled) {
            String timeOfPreviousShowKey = constructSharedPrefsKey(adUnit, "CappingManager.TIME_OF_THE_PREVIOUS_SHOW", placementName);
            long timeOfPreviousShow = IronSourceUtils.getLongFromSharedPrefs(context, timeOfPreviousShowKey, 0L);


            String secondsBetweenShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.SECONDS_BETWEEN_SHOWS", placementName);
            int secondsBetweenShows = IronSourceUtils.getIntFromSharedPrefs(context, secondsBetweenShowsKey, 0);


            if (currentTime - timeOfPreviousShow < secondsBetweenShows * 1000) {
                return ECappingStatus.CAPPED_PER_PACE;
            }
        }


        String isCappingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_CAPPING_ENABLED", placementName);
        boolean isCappingEnabled = IronSourceUtils.getBooleanFromSharedPrefs(context, isCappingEnabledKey, false);
        if (isCappingEnabled) {
            String maxNumberOfShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.MAX_NUMBER_OF_SHOWS", placementName);
            int maxNumberOfShows = IronSourceUtils.getIntFromSharedPrefs(context, maxNumberOfShowsKey, 0);


            String currentNumberOfShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.CURRENT_NUMBER_OF_SHOWS", placementName);
            int currentNumberOfShows = IronSourceUtils.getIntFromSharedPrefs(context, currentNumberOfShowsKey, 0);


            String timeThresholdKey = constructSharedPrefsKey(adUnit, "CappingManager.CAPPING_TIME_THRESHOLD", placementName);
            long timeThreshold = IronSourceUtils.getLongFromSharedPrefs(context, timeThresholdKey, 0L);


            if (currentTime >= timeThreshold) {
                IronSourceUtils.saveIntToSharedPrefs(context, currentNumberOfShowsKey, 0);
                IronSourceUtils.saveLongToSharedPrefs(context, timeThresholdKey, 0L);
            } else if (currentNumberOfShows >= maxNumberOfShows) {
                return ECappingStatus.CAPPED_PER_COUNT;
            }
        }


        return ECappingStatus.NOT_CAPPED;
    }

    private static void incrementShowCounter(Context context, String adUnit, String placementName) {
        String isPacingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_PACING_ENABLED", placementName);
        boolean isPacingEnabled = IronSourceUtils.getBooleanFromSharedPrefs(context, isPacingEnabledKey, false);
        if (isPacingEnabled) {
            long currentTime = System.currentTimeMillis();
            String timeOfPreviousShowKey = constructSharedPrefsKey(adUnit, "CappingManager.TIME_OF_THE_PREVIOUS_SHOW", placementName);
            IronSourceUtils.saveLongToSharedPrefs(context, timeOfPreviousShowKey, currentTime);
        }


        String isCappingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_CAPPING_ENABLED", placementName);
        boolean isCappingEnabled = IronSourceUtils.getBooleanFromSharedPrefs(context, isCappingEnabledKey, false);
        if (isCappingEnabled) {
            String maxNumberOfShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.MAX_NUMBER_OF_SHOWS", placementName);
            int maxNumberOfShows = IronSourceUtils.getIntFromSharedPrefs(context, maxNumberOfShowsKey, 0);


            String currentNumberOfShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.CURRENT_NUMBER_OF_SHOWS", placementName);
            int currentNumberOfShows = IronSourceUtils.getIntFromSharedPrefs(context, currentNumberOfShowsKey, 0);


            boolean isFirstShow = currentNumberOfShows == 0;

            if (isFirstShow) {
                String cappingTypeKey = constructSharedPrefsKey(adUnit, "CappingManager.CAPPING_TYPE", placementName);
                String cappingTypeString = IronSourceUtils.getStringFromSharedPrefs(context, cappingTypeKey, PlacementCappingType.PER_DAY.toString());
                PlacementCappingType cappingType = null;
                for (PlacementCappingType type : PlacementCappingType.values()) {
                    if (type.value.equals(cappingTypeString)) {
                        cappingType = type;
                        break;
                    }
                }

                long timeThreshold = initTimeThreshold(cappingType);


                String timeThresholdKey = constructSharedPrefsKey(adUnit, "CappingManager.CAPPING_TIME_THRESHOLD", placementName);
                IronSourceUtils.saveLongToSharedPrefs(context, timeThresholdKey, timeThreshold);
            }


            currentNumberOfShows++;


            IronSourceUtils.saveIntToSharedPrefs(context, currentNumberOfShowsKey, currentNumberOfShows);
        }
    }

    private static long initTimeThreshold(PlacementCappingType cappingType) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        switch (cappingType) {
            case PER_DAY:
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case PER_HOUR:

                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        return calendar.getTimeInMillis();
    }

    private static void addCappingInfo(Context context, String adUnit, String placementName, PlacementAvailabilitySettings availabilitySettings) {
        boolean isDeliveryEnabled = availabilitySettings.isDeliveryEnabled();
        String deliveryKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_DELIVERY_ENABLED", placementName);
        IronSourceUtils.saveBooleanToSharedPrefs(context, deliveryKey, isDeliveryEnabled);
        if (!isDeliveryEnabled) {
            return;
        }


        boolean isCappingEnabled = availabilitySettings.isCappingEnabled();
        String isCappingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_CAPPING_ENABLED", placementName);
        IronSourceUtils.saveBooleanToSharedPrefs(context, isCappingEnabledKey, isCappingEnabled);
        if (isCappingEnabled) {
            int maxNumberOfShows = availabilitySettings.getCappingValue();
            String maxNumberOfShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.MAX_NUMBER_OF_SHOWS", placementName);
            IronSourceUtils.saveIntToSharedPrefs(context, maxNumberOfShowsKey, maxNumberOfShows);


            PlacementCappingType cappingType = availabilitySettings.getCappingType();
            String cappingTypeKey = constructSharedPrefsKey(adUnit, "CappingManager.CAPPING_TYPE", placementName);
            IronSourceUtils.saveStringToSharedPrefs(context, cappingTypeKey, cappingType.toString());
        }


        boolean isPacingEnabled = availabilitySettings.isPacingEnabled();
        String isPacingEnabledKey = constructSharedPrefsKey(adUnit, "CappingManager.IS_PACING_ENABLED", placementName);
        IronSourceUtils.saveBooleanToSharedPrefs(context, isPacingEnabledKey, isPacingEnabled);
        if (isPacingEnabled) {
            int secondsBetweenShows = availabilitySettings.getPacingValue();
            String secondsBetweenShowsKey = constructSharedPrefsKey(adUnit, "CappingManager.SECONDS_BETWEEN_SHOWS", placementName);
            IronSourceUtils.saveIntToSharedPrefs(context, secondsBetweenShowsKey, secondsBetweenShows);
        }
    }

    public static enum ECappingStatus {
        CAPPED_PER_DELIVERY, CAPPED_PER_COUNT, CAPPED_PER_PACE, NOT_CAPPED;

        private ECappingStatus() {
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/CappingManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */