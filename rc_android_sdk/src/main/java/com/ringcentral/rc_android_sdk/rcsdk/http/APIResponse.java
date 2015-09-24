package com.ringcentral.rc_android_sdk.rcsdk.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;




//rename to API Response
public class APIResponse {

    protected Response response;
    protected Request request;

    public APIResponse(Request request, Response response){
        this.request= request;
        this.response = response;
    }

    public Request request(){
        return this.response.request();
    }

    public Response respone(){
        return this.response;
    }



    /**
     * Parses authentication Json to return a HashMap used for setting Auth data
     */
    //it reads only plain hashmaps

    public HashMap hashMap() throws IOException{
        Gson gson = new Gson();
        Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
        HashMap<String, String> jsonMap = gson.fromJson(this.text(), mapType);
        return jsonMap;
    }

    /**
     * Returns the response body as a string
     * @return
     * @throws java.io.IOException
     */
    public String text() throws IOException {
        try {
            return response.body().string();
        } catch (IOException e) {
            throw new IOException();
        }
    }

    public ResponseBody body(){
        return this.response.body();
    }

    /**
     * Returns the response body as a JSONObject
     */
    public JSONObject json() throws IOException, JSONException {
        JSONObject object = null;
        try {
            object = new JSONObject(response.body().string());
        } catch (JSONException e) {
            throw new JSONException("Improper JSON type");
        } catch (IOException e) {
           throw new IOException();
        }
        return object;
    }

    /**
     * Checks if the HTTP status code of the response is successful
     */
    public boolean ok(){
        int status = this.response.code();
        return (status >= 200 && status < 300);
    }

    /**
     * Returns an error message with the response code and error message
     */
    public String error(){
        if(this.response == null){
            return null;
        }

        if(this.ok()){
            return null;
        }
        String message = this.response.code() + " " + this.response.message();

        return message;
    }


    public Response response() {
        return response;
    }

}
