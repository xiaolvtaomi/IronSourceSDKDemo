package com.ironsource.sdk.data;

public class SSAEnums {
    public static enum DebugMode {
        MODE_0(0),
        MODE_1(1),
        MODE_2(2),
        MODE_3(3);

        private int value;

        private DebugMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public static enum ProductType {
        OfferWall,
        Interstitial,
        OfferWallCredits,
        RewardedVideo;


        private ProductType() {
        }
    }


    public static enum BackButtonState {
        None,
        Device,
        Controller;

        private BackButtonState() {
        }
    }

    public static enum ControllerState {
        None,
        FailedToDownload,
        FailedToLoad,

        Loaded,
        Ready,
        Failed;

        private ControllerState() {
        }
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/sdk/data/SSAEnums.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */