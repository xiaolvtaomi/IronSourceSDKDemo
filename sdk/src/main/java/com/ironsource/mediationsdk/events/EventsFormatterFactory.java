package com.ironsource.mediationsdk.events;

import com.ironsource.mediationsdk.logger.IronSourceLogger;
import com.ironsource.mediationsdk.logger.IronSourceLoggerManager;


class EventsFormatterFactory {
    static final String TYPE_IRONBEAST = "ironbeast";
    static final String TYPE_OUTCOME = "outcome";
    static final int AD_UNIT_REWARDED_VIDEO = 3;
    static final int AD_UNIT_INTERSTITIAL = 2;

    static AbstractEventsFormatter getFormatter(String type, int adUnit) {
        if ("ironbeast".equals(type))
            return new IronbeastEventsFormatter(adUnit);
        if ("outcome".equals(type)) {
            return new OutcomeEventsFormatter(adUnit);
        }
        if (adUnit == 2)
            return new IronbeastEventsFormatter(adUnit);
        if (adUnit == 3) {
            return new OutcomeEventsFormatter(adUnit);
        }


        IronSourceLoggerManager.getLogger().log(IronSourceLogger.IronSourceTag.NATIVE, "EventsFormatterFactory failed to instantiate a formatter (type: " + type + ", adUnit: " + adUnit + ")", 2);


        return null;
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/events/EventsFormatterFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */