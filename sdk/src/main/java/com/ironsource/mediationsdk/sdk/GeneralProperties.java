 package com.ironsource.mediationsdk.sdk;

 import java.util.Map;
 import org.json.JSONObject;



 public class GeneralProperties
 {
   public static final String ALLOW_LOCATION_SHARED_PREFS_KEY = "GeneralProperties.ALLOW_LOCATION_SHARED_PREFS_KEY";
   public static final String USER_ID_TYPE = "userIdType";
   private static GeneralProperties mInstance;
   private JSONObject mProperties;

   private GeneralProperties()
   {
     this.mProperties = new JSONObject();
   }

   public static synchronized GeneralProperties getProperties()
   {
     if (mInstance == null) {
       mInstance = new GeneralProperties();
     }
     return mInstance;
   }

   public synchronized void putKeys(Map<String, Object> properties)
   {
     if (properties != null)
       for (String key : properties.keySet())
         putKey(key, properties.get(key));
   }

   public synchronized void putKey(String key, Object value) {
     try {
       this.mProperties.put(key, value);
     }
     catch (Exception localException) {}
   }

   public synchronized String get(String key) {
     return this.mProperties.optString(key);
   }

   public synchronized JSONObject toJSON() {
     return this.mProperties;
   }
 }


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/sdk/GeneralProperties.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */