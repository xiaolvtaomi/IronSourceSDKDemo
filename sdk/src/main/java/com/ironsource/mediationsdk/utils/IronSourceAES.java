package com.ironsource.mediationsdk.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.ironsource.eventsmodule.EventData;
import com.ironsource.mediationsdk.events.InterstitialEventsManager;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;


public class IronSourceAES {
    private static boolean mDidSendEncryptionFailEventInSession = false;

    public static synchronized String encode(String keyString, String stringToEncode) {
        if (TextUtils.isEmpty(keyString)) {
            return "";
        }
        if (TextUtils.isEmpty(stringToEncode)) {
            return "";
        }
        try {
            SecretKeySpec skeySpec = getKey(keyString);
            byte[] clearText = stringToEncode.getBytes("UTF8");


            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);


            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);

            String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), 0);
            return encrypedValue.replaceAll(System.getProperty("line.separator"), "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static synchronized String decode(String keyString, String stringToDecode) {
        if (TextUtils.isEmpty(keyString)) {
            return "";
        }
        if (TextUtils.isEmpty(stringToDecode)) {
            return "";
        }
        try {
            SecretKey key = getKey(keyString);


            byte[] iv = new byte[16];
            Arrays.fill(iv, (byte) 0);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            byte[] encrypedPwdBytes = Base64.decode(stringToDecode, 0);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(2, key, ivParameterSpec);
            byte[] decrypedValueBytes = cipher.doFinal(encrypedPwdBytes);

            return new String(decrypedValueBytes);

        } catch (Exception e) {
            e.printStackTrace();


            if (!mDidSendEncryptionFailEventInSession) {
                mDidSendEncryptionFailEventInSession = true;
                JSONObject data = IronSourceUtils.getMediationAdditionalData(false);
                try {
                    data.put("status", "false");
                    data.put("reason", 1);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                EventData parseFailedEvent = new EventData(114, data);
                InterstitialEventsManager.getInstance().sendEventToUrl(parseFailedEvent, "https://track.atom-data.io");
            }
        }

        return "";
    }


    private static SecretKeySpec getKey(String key)
            throws UnsupportedEncodingException {
        int keyLength = 256;
        byte[] keyBytes = new byte[keyLength / 8];

        Arrays.fill(keyBytes, (byte) 0);


        byte[] passwordBytes = key.getBytes("UTF-8");
        int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);

        return new SecretKeySpec(keyBytes, "AES");
    }
}


/* Location:              /Users/lml/Downloads/mediationsdk-6.7.10.jar!/com/ironsource/mediationsdk/utils/IronSourceAES.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */