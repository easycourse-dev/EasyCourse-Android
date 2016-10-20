package com.example.markwen.easycourse;

import android.content.Context;
import android.util.Log;

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

    public static void login(Context context, String email, String password, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("isMobile","true");
        client.addHeader("Content-Type", "application/json");

        Log.e("com.example.easycourse", "email : "+email);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);
        jsonParam.put("password", password);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/login", body, "application/json", jsonHttpResponseHandler);
    }
}
