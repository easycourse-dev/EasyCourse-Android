package com.example.markwen.easycourse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

public class APIFunctions {

    static AsyncHttpClient client = new AsyncHttpClient();
    static final String URL = "http://zengjintaotest.com/api";

    //API function for signup to server
    public static void signUp(Context context, String email, String password, String displayName, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("isMobile","true");
        client.addHeader("Content-Type", "application/json");

        RequestParams params = new RequestParams();
        params.put("isMobile","true");
        params.put("Content-Type", "application/json");

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);
        jsonParam.put("password", password);
        jsonParam.put("displayName", displayName);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/signup/", body, "application/json", jsonHttpResponseHandler);
    }

    //API function to login to server
    public static void login(Context context, String email, String password, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("isMobile","true");
        client.addHeader("Content-Type", "application/json");

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);
        jsonParam.put("password", password);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/login", body, "application/json", jsonHttpResponseHandler);
    }

    //API function to logout user
    public static boolean logout(Context context, JsonHttpResponseHandler jsonHttpResponseHandler){
        //Get userToken from shared preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String userToken = sharedPref.getString("userToken", null);

        //Return false if userToken is not found
        if(userToken==null)
            return false;

        client.addHeader("auth",userToken);
        client.post(context, URL+"/logout", null, jsonHttpResponseHandler);

        return true;
    }

    //API function to login to facebook with accessToken
    public static void facebookLogin(Context context, String accessToken, JsonHttpResponseHandler jsonHttpResponseHandler){
        client.addHeader("isMobile","true");
        client.addHeader("Content-Type", "application/json");

        client.get(context, URL+"/facebook/token/?access_token="+accessToken, jsonHttpResponseHandler);
    }
}
