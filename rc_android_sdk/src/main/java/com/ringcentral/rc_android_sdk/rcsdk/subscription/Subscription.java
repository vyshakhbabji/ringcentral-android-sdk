package com.ringcentral.rc_android_sdk.rcsdk.subscription;

import android.util.Base64;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.ringcentral.rc_android_sdk.rcsdk.platform.Platform;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by andrew.pang on 7/15/15.
 */
public class Subscription{

    public Pubnub pubnub;
    ArrayList<String> eventFilters = new ArrayList<>();
    String expirationTime = "";
    int expiresIn = 0;
    public IDeliveryMode deliveryMode = new IDeliveryMode();
    public String id = "";
    String creationTime = "";
    String status = "";
    String uri = "";

    public class IDeliveryMode {
        public String transportType = "Pubnub";
        public boolean encryption = false;
        public String address = "";
        public String subscriberKey = "";
        public String secretKey = "";
        public String encryptionKey = "";
    }

    public Subscription(){
    }

    public void updateSubscription(JSONObject responseJson) throws JSONException{
        id = responseJson.getString("id");
        JSONObject deliveryMode = responseJson.getJSONObject("deliveryMode");
        this.deliveryMode.encryptionKey = deliveryMode.getString("encryptionKey");
        this.deliveryMode.address = deliveryMode.getString("address");
        this.deliveryMode.subscriberKey = deliveryMode.getString("subscriberKey");
        this.deliveryMode.secretKey = deliveryMode.getString("secretKey");
    }

    public void setEncryptionKey(String encryptionKey) {
        this.deliveryMode.encryptionKey = encryptionKey;
    }

    public Pubnub getPubnub() {
        return pubnub;
    }

    public void subscribe(JSONObject subscriptionResponse, Callback c) {
        try {
            updateSubscription(subscriptionResponse);
            pubnub = new Pubnub("", deliveryMode.subscriberKey, deliveryMode.secretKey);
            pubnub.subscribe(this.deliveryMode.address, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEvents(String[] events) {
        for(String event:events){
            this.eventFilters.add(event);
        }
    }

    public void setEvents(String[] events){
        this.eventFilters = new ArrayList<String>(Arrays.asList(events));
    }

    private ArrayList getFullEventFilters(){
        return this.eventFilters;
    }

    boolean isSubscribed(){
        return !(this.deliveryMode.subscriberKey.equals("") && this.deliveryMode.address.equals(""));
    }

    public void unsubscribe() {
        if((this.pubnub != null) && this.isSubscribed())
            this.pubnub.unsubscribe(deliveryMode.address);
    }

    public String notify(String message, String encryptionKey){
        byte[] key = Base64.decode(encryptionKey, Base64.NO_WRAP);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        byte[] data = Base64.decode(message, Base64.NO_WRAP);
        String decryptedString = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(data);
            decryptedString = new String(decrypted);
        } catch(Exception e){
            e.printStackTrace();
        }
        return decryptedString;
    }

}