package com.ringcentral.rc_android_sdk;

import android.test.InstrumentationTestCase;

import com.ringcentral.rc_android_sdk.rcsdk.SDK;
import com.ringcentral.rc_android_sdk.rcsdk.http.APIResponse;
import com.ringcentral.rc_android_sdk.rcsdk.platform.Helpers;
import com.ringcentral.rc_android_sdk.rcsdk.platform.Platform;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by andrew.pang on 7/29/15.
 */
public class APITest extends InstrumentationTestCase {

    public void testApi() throws Exception{
        SDK sdk = new SDK("E0_nOAfbR7GkteYbDv93oA", "UelNnk-1QYK0rHyvjJJ9yQx3Yl6vj3RvGmb0G2SH6ePw", Platform.Server.SANDBOX);
        final Helpers helpers= sdk.getHelpers();
        helpers.login("15856234138", "", "P@ssw0rd",
                new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        APIResponse transaction = new APIResponse(response);
                        HashMap<String, String> responseMap = transaction.hashMap();
                        helpers.setAuthData(responseMap);
                        //Test Authorization
                        assertTrue(transaction.ok());

                        //Test Send SMS
                        helpers.sendSMS("16502823614", "15856234166", "Test Message",
                                new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        APIResponse transaction = new APIResponse(response);
                                        assertTrue(transaction.ok());
                                        try {
                                            JSONObject json = new JSONObject(transaction.toString());
                                            String messageId = json.getString("id");
                                            //Test Delete Message
                                            HashMap<String, String> deleteHeader = new HashMap<>();
                                            deleteHeader.put("method", "DELETE");
                                            String url = "/restapi/v1.0/account/~/extension/~/message-store/" + messageId;
                                            helpers.delete(url, deleteHeader,
                                                    new Callback() {
                                                        @Override
                                                        public void onFailure(Request request, IOException e) {

                                                        }

                                                        @Override
                                                        public void onResponse(Response response) throws IOException {
                                                            APIResponse deleteResponse = new APIResponse(response);
                                                            assertTrue(deleteResponse.ok());
                                                        }
                                                    });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );

                        //Test GET Account Info
                        helpers.accountInfo(
                                new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        APIResponse callLogResponse = new APIResponse(response);
                                        assertTrue(callLogResponse.ok());
                                    }
                                }
                        );

                        //Test GET Call Log
                        helpers.callLog(
                                new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        APIResponse callLogResponse = new APIResponse(response);
                                        assertTrue(callLogResponse.ok());
                                    }
                                }
                        );

                        //Test GET Message Store
                        helpers.messageStore(
                                new Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        APIResponse messageStoreResponse = new APIResponse(response);
                                        assertTrue(messageStoreResponse.ok());
                                    }
                                }
                        );

                    }
                }
        );
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }


}
