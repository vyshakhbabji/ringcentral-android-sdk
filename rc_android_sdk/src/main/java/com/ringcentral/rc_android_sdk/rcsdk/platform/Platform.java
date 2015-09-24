package com.ringcentral.rc_android_sdk.rcsdk.platform;

import android.util.Base64;

import com.ringcentral.rc_android_sdk.rcsdk.http.APIResponse;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ringcentral.rc_android_sdk.rcsdk.subscription.Subscription;



public class Platform implements Serializable {

    public Server server;
    public Auth auth;
    String appKey;
    String appSecret;
    String account_id = "~";
    String URL_PREFIX ="/restapi";
    String TOKEN_ENDPOINT ="/restapi/oauth/token";
    String REVOKE_ENDPOINT = "/restapi/oauth/revoke";
    String ACCESS_TOKEN_TTL = "3600";
    String REFRESH_TOKEN_TTL = "604800";

    Subscription subscription;

    //user-agent
    public Platform(String appKey, String appSecret, Server server) {
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.auth = new Auth();
        this.server = server;
    }

    public void setAuthData(HashMap<String, String> authData) {
        this.auth.setData(authData);
    }

    public Auth auth() {
        return auth.getData();
    }

    //FIXME Remove
    public Subscription getSubscription() {
        return subscription;
    }


    protected String getAccessToken() {
        return this.auth.getAccessToken();
    }

    protected String encodeAPICredentialsToBase64() throws IOException{
        String keySec = appKey + ":" + appSecret;
        byte[] message = new byte[0];
        try {
            message = keySec.getBytes("UTF-8");
        }
        catch (IOException e){
            throw new IOException();
        }
        String encoded = Base64.encodeToString(message, Base64.DEFAULT);
        //When encoding with Android's Base64 API,  '\n' is automatically added, so it needs to be removed
        String apiKey = (encoded).replace("\n", "");
        return apiKey;
    }


    public String getAuthHeader() {
        return this.auth.getTokenType() + " " + this.getAccessToken();
    }

    /**
     * Takes in part of a URL along with options to return the endpoint for the API call
     *
     * @return
     */
    public String createURL(String url, HashMap<String, String> options) {
        String builtUrl = "";
        boolean has_http = url.contains("http://") || url.contains("https://");
        if (options.containsKey("addServer") && !has_http) {
            builtUrl += this.server.value;
        }
        if (!(url.contains(URL_PREFIX)) && !has_http) {
            builtUrl += URL_PREFIX + "/" + "v1.0";
        }

        if (url.contains("/account/")) {
            builtUrl = builtUrl.replace("/account/" + "~", "/account/" + this.account_id);
        }

        builtUrl += url;

        if (options.containsKey("addMethod")) {
            if (builtUrl.contains("?")) {
                builtUrl += "&";
            } else {
                builtUrl += "?";
            }
            builtUrl += "_method=" + options.get("addMethod");
        }

        if (options.containsKey("addToken")) {
            if (builtUrl.contains("?")) {
                builtUrl += "&";
            } else {
                builtUrl += "?";
            }
            builtUrl += "access_token=" + this.auth.getAccessToken();
        }

        return builtUrl;
    }

    /**
     * Takes the body and prepares it to be passed in the HTTP request as a string, based on the MediaType of the body
     *
     * @return
     */
    protected String getBodyString(HashMap<String, String> body, MediaType mediaType) {
        String bodyString = "";

        //Pass in "body" key

        try {
            StringBuilder data = new StringBuilder();
            int count = 0;
            if (!(mediaType == MediaType.parse(ContentTypeSelection.FORM_TYPE_MARKDOWN.value))) {
                data.append("{ ");
            }
            //Iterate through the HashMap
            for (Map.Entry<String, String> entry : body.entrySet()) {
                //If the MediaType is 'x-www-form-urlencoded', then encode the body
                if (mediaType == MediaType.parse(ContentTypeSelection.FORM_TYPE_MARKDOWN.value)) {
                    if (count != 0) {
                        data.append("&");
                    }
                    data.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
                    count++;
                } else {
                    if (count != 0) {
                        data.append(", ");
                    }
                    data.append(entry.getKey());
                    data.append(": ");
                    data.append(entry.getValue());
                    count++;
                }
            }
            if (!(mediaType == MediaType.parse(ContentTypeSelection.FORM_TYPE_MARKDOWN.value))) {
                data.append(" }");
            }
            bodyString = data.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bodyString;
    }

    /**
     * Checks if the access token is valid, and if not refreshes the token
     *
     * @throws Exception
     */
    public void loggedIn() throws Exception {
        if (!this.auth.isAccessTokenValid()) {
            this.refresh();
        }
        //If after calling a refresh, the accessToken is still not valid, throw exception
        if (!this.auth.isAccessTokenValid()) {
            throw new Exception("Access token is expired");
        }
    }

    /**
     * Method used for API calls, with the request type, body, headers, and callback as parameters.
     */
    public void sendRequest(String method, String url, LinkedHashMap<String, String> body, HashMap<String, String> headerMap, Callback callback) {
        try {

            OkHttpClient client = new OkHttpClient();
            //Check if the Platform is authorized, and add the authorization header
            this.loggedIn();

            headerMap.put("Authorization", this.getAuthHeader());
            //Generate the proper url to be passed into the request


            HashMap<String, String> options = new HashMap<>();
            options.put("addServer", "true");
            String apiUrl = createURL(url, options);

            Request.Builder requestBuilder = new Request.Builder();
            //Add all the headers to the Request.Builder from the headerMap

            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = null;
            if (method.toUpperCase().equals("GET")) {
                request = requestBuilder
                        .url(apiUrl)
                        .build();
            } else if (method.toUpperCase().equals("DELETE")) {
                request = requestBuilder
                        .url(apiUrl)
                        .delete()
                        .build();
            } else {
                //For POST and PUT requests, find and set what MediaType the body is
                MediaType mediaType;
                if (headerMap.containsValue("application/json")) {
                    mediaType = MediaType.parse(ContentTypeSelection.JSON_TYPE_MARKDOWN.value);
                } else if (headerMap.containsValue("multipart/mixed")) {
                    mediaType = MediaType.parse(ContentTypeSelection.MULTIPART_TYPE_MARKDOWN.value);
                } else {
                    mediaType = MediaType.parse(ContentTypeSelection.FORM_TYPE_MARKDOWN.value);
                }
                String bodyString = getBodyString(body, mediaType);
                if (method.toUpperCase().equals("POST")) {
                    request = requestBuilder
                            .url(apiUrl)
                            .post(RequestBody.create(mediaType, bodyString))
                            .build();
                } else if (method.toUpperCase().equals("PUT")) {
                    request = requestBuilder
                            .url(apiUrl)
                            .put(RequestBody.create(mediaType, bodyString))
                            .build();
                }
            }
            //Make OKHttp request call, that returns response to the callback
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes in parameters used for authorization and makes an auth call
     */
    public void login(String username, String extension, String password, Callback callback) throws IOException{
        LinkedHashMap<String, String> body = new LinkedHashMap<>();

        //Body
        body.put("grant_type", "password");
        body.put("username", username);
        body.put("extension", extension);
        body.put("password", password);
        body.put("access_token_ttl",ACCESS_TOKEN_TTL);
        body.put("refresh_token_ttl",REFRESH_TOKEN_TTL);

        //Header
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Basic " + this.encodeAPICredentialsToBase64());
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        this.authCall(TOKEN_ENDPOINT, body, headerMap, callback);
    }

    /**
     * POST request set up for making authorization calls
     */

    public void authCall(String url, LinkedHashMap<String, String> body, HashMap<String, String> headerMap, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = null;
        MediaType mediaType = MediaType.parse(ContentTypeSelection.FORM_TYPE_MARKDOWN.toString());
        String bodyString = getBodyString(body, mediaType);
        HashMap<String, String> options = new HashMap<>();
        options.put("addServer", "true");
        String apiUrl = createURL(url, options);
        request = requestBuilder
                .url(apiUrl)
                .post(RequestBody.create(mediaType, bodyString))
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * Uses the refresh token to refresh authentication
     *
     * @throws Exception
     */
    public  void refresh() throws Exception {
        if (!this.auth.isRefreshTokenValid()) {
            throw new Exception("Refresh token is expired");
        } else
{
            LinkedHashMap<String, String> body = new LinkedHashMap<>();

            //Body
            body.put("grant_type", "refresh_token");
            body.put("refresh_token", this.auth.getRefreshToken());
            body.put("access_token_ttl",ACCESS_TOKEN_TTL);
            body.put("refresh_token_ttl",REFRESH_TOKEN_TTL);
            //Header
            HashMap<String, String> headerMap = new HashMap<>();
            headerMap.put("method", "POST");
            //Makes an auth call with the refresh token and sets the Auth data with the new response
            this.authCall(TOKEN_ENDPOINT, body, headerMap,
                    new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                         //define me
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            if (!response.isSuccessful())
                                throw new IOException("Unexpected code " + response);
                            APIResponse transaction = new APIResponse(response.request(),response);
                            HashMap<String, String> responseMap = transaction.hashMap();
                            setAuthData(responseMap);
                            System.out.println("refresh");
                        }
                    });
        //}
    }}

    /**
     * Revokes access for current access token
     */
    public void logout(Callback callback) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        body.put("token", this.getAccessToken());
        HashMap<String, String> headerMap = new HashMap<>();

        headerMap.put("method", "POST");
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        this.authCall(REVOKE_ENDPOINT, body, headerMap, callback);
        this.auth.reset();
    }

    /**
     * Sets the header and body to make a GET request
     */
    public void get(String url, HashMap<String, String> headerMap, Callback callback) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        this.sendRequest("GET", url, body, headerMap, callback);
    }

    /**
     * Sets the header and body to make a POST request
     */
    public void post(String url, LinkedHashMap<String, String> body, HashMap<String, String> headerMap, Callback callback) {
        this.sendRequest("POST", url, body, headerMap, callback);
    }

    /**
     * Makes a call to the POST Subscription api, and with the response, creates a Pubnub subscription
     */
    public void subscribe(Callback callback){
        LinkedHashMap<String, String> body = new LinkedHashMap<>();

        //Pass in customized body hashmap

        body.put("\"eventFilters\"", "[ \n" +
                "    \"/restapi/v1.0/account/~/extension/~/presence\", \n" +
                "    \"/restapi/v1.0/account/~/extension/~/message-store\" \n" +
                "  ]");
        body.put("\"deliveryMode\"", "{\"transportType\": \"PubNub\",\"encryption\": \"false\"}");
        HashMap<String, String> headers = new HashMap<>();
        String url = "/restapi/v1.0/subscription";
        headers.put("Content-Type", "application/json");
        //Makes a POST request to the RingCentral API to receive PubNub info
        this.post(url, body, headers, callback);
    }

    /**
     * Makes a DELETE API call for the current subscription, and unsubscribes with Pubnub
     */
    public void removeSubscription() {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        HashMap<String, String> headers = new HashMap<>();
        String url =  "/restapi/v1.0/subscription" + subscription.id;
        this.delete(url, headers, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                APIResponse transaction = new APIResponse(response.request(),response);
                subscription.unsubscribe();
            }
        });
    }

    /**
     * Sets up body and header for a PUT request
     */
    public void put(String url, LinkedHashMap<String, String> body, HashMap<String, String> headerMap, Callback callback) {
        this.sendRequest("PUT", url, body, headerMap, callback);
    }

    /**
     * Sets up body and headers for a DELETE request
     */
    public void delete(String url, HashMap<String, String> headerMap, Callback callback) {
        LinkedHashMap<String, String> body = new LinkedHashMap<>();
        this.sendRequest("DELETE", url, body, headerMap, callback);
    }

    public enum Server {

        PRODUCTION("https://platform.ringcentral.com"),
        SANDBOX("https://platform.devtest.ringcentral.com");

        private String value;

        private Server(String url) {
            this.value = url;
        }
    }

    public enum ContentTypeSelection {
        FORM_TYPE_MARKDOWN("application/x-www-form-urlencoded"),
        JSON_TYPE_MARKDOWN("application/json"),
        MULTIPART_TYPE_MARKDOWN("multipart/mixed; boundary=Boundary_1_14413901_1361871080888");

        private String value;

        private ContentTypeSelection(String contentType) {
            this.value = contentType;
        }
    }

}
