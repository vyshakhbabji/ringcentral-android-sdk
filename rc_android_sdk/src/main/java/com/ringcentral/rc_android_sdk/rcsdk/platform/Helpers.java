package com.ringcentral.rc_android_sdk.rcsdk.platform;

import com.squareup.okhttp.Callback;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by andrew.pang on 8/10/15.
 */
public class Helpers extends Platform {
    /**
     * @param appKey
     * @param appSecret
     * @param server    Pass in either "SANDBOX" or "PRODUCTION"
     */
    public Helpers(String appKey, String appSecret, Platform.Server server) {
        super(appKey, appSecret, server);
    }

    /**
     * GET Account Info API call
     *
     * @param c
     */
    public void accountInfo(Callback c) {
        try {
            this.loggedIn();
            HashMap<String, String> headers = new HashMap<>();
            String url = "/restapi/v1.0/account/~";
            this.get(url, headers, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GET Call Log API call
     *
     * @param c
     */
    public void callLog(Callback c) {
        try {
           //this.
            HashMap<String, String> callLogHeaders = new HashMap<>();
            String url = "/restapi/v1.0/account/~/call-log";
            this.get(url, callLogHeaders, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * GET Message Store API call
     *
     * @param c
     */
    public void messageStore(Callback c) {
        try {
            this.loggedIn();
            HashMap<String, String> messageStoreHeaders = new HashMap<>();
            messageStoreHeaders.put("url", "/restapi/v1.0/account/~/extension/~/message-store");
            String url = "/restapi/v1.0/account/~/extension/~/message-store";
            this.get(url, messageStoreHeaders, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RingOut API call using POST request
     *
     * @param to        Phone number calling to
     * @param from      Phone number calling from
     * @param callerId  Phone number used for caller ID
     * @param hasPrompt "True" or "False" states whether a prompt plays before call
     * @param c
     */
    public void ringOut(String to, String from, String callerId, String hasPrompt, Callback c) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("\"to\"", "{\"phoneNumber\": \"" + to + "\"}");
        body.put("\"from\"", "{\"phoneNumber\": \"" + from + "\"}");
        body.put("\"callerId\"", "{\"phoneNumber\": \"" + callerId + "\"}");
        body.put("\"playPrompt\"", hasPrompt);
        HashMap<String, String> headers = new HashMap<>();
        String url = "/restapi/v1.0/account/~/extension/~/ringout";
        headers.put("Content-Type", "application/json");
        this.post(url, body, headers, c);
    }

    /**
     * SMS API call using POST request
     *
     * @param to      Phone number sending SMS to
     * @param from    Phone number sending SMS from
     * @param message SMS text message body
     * @param c
     */
    public void sendSMS(String to, String from, String message, Callback c) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("\"to\"", "[{\"phoneNumber\": \"" + to + "\"}]");
        body.put("\"from\"", "{\"phoneNumber\": \"" + from + "\"}");
        body.put("\"text\"", "\"" + message + "\"");
        HashMap<String, String> headers = new HashMap<>();
        String url = "/restapi/v1.0/account/~/extension/~/sms";
        headers.put("Content-Type", "application/json");
        this.post(url, body, headers, c);
    }

}