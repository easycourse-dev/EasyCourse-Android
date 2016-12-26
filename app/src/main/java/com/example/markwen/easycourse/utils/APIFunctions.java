package com.example.markwen.easycourse.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;



public class APIFunctions {

    static AsyncHttpClient client = new AsyncHttpClient();
    static final String URL = "https://zengjintaotest.com/api";
    private static final String TAG = "APIFunctions";

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
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
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

    //API function to update user's university
    public static boolean updateUser(Context context, String universityID, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
            return false;

        client.addHeader("auth",userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("university", universityID);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/user/update", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    //API function to get university list
    public static void getUniversities(Context context, JsonHttpResponseHandler jsonHttpResponseHandler){
        client.get(context, URL+"/univ", jsonHttpResponseHandler);
    }

    //API function to search course database
    public static void searchCourse(Context context, String searchQuery, int limit, int skip, String universityID, JsonHttpResponseHandler jsonHttpResponseHandler){
        client.get(context, URL+"/course?q="+searchQuery+"&limit="+limit+"&skip="+skip+"&univ="+universityID, jsonHttpResponseHandler);
    }

    //API function to get language list
    public static void getLanguages(Context context, JsonHttpResponseHandler jsonHttpResponseHandler){
        client.get(context, URL+"/defaultlanguage", jsonHttpResponseHandler);
    }

    //API function to set courses and languages in  user's profile
    public static boolean setCoursesAndLanguages(Context context, String[] languageCodeArray, String[] courseCodeArray, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
            return false;

        client.addHeader("auth",userToken);

        JSONObject jsonParam = new JSONObject();
        JSONArray jsonLanguageCodeArray = getJsonArrayFromStringArray(languageCodeArray);
        JSONArray jsonCourseCodeArray = getJsonArrayFromStringArray(courseCodeArray);
        jsonParam.put("lang", jsonLanguageCodeArray);
        jsonParam.put("course", jsonCourseCodeArray);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/choosecourse", body, "application/json", jsonHttpResponseHandler);
        return true;
    }



    //API function to turn on or off push notifications for a room
    public static boolean setSilentRoom(Context context, String roomID, boolean silentBoolean, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
            return false;

        client.addHeader("auth",userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("room", roomID);
        jsonParam.put("silentBoolean", silentBoolean);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/silentroom", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    //API function to search room database
    public static void searchRoom(Context context, String searchQuery, int limit, int skip, String universityID, JsonHttpResponseHandler jsonHttpResponseHandler){
        client.get(context, URL+"/room?q="+searchQuery+"&limit="+limit+"&skip="+skip+"&univ="+universityID, jsonHttpResponseHandler);
    }

    //API function to report a user
    public static boolean reportUser(Context context, String targetUser, String reason, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
            return false;

        client.addHeader("auth",userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("targetUser", targetUser);
        jsonParam.put("reason", reason);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL+"/report", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    public static boolean uploadImage(Context context, File image, String fileName, String uploadType, String roomID, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException, FileNotFoundException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if(userToken.isEmpty())
            return false;

        client.addHeader("auth",userToken);
        client.addHeader("type",uploadType);
        if(uploadType.equals("message"))
            client.addHeader("room",roomID);

        RequestParams params = new RequestParams();
        params.put("img", image);
        params.put("fileName", fileName);
        params.put("mimeType", "image/jpg");

        client.post(context, URL+"/uploadimage", params, jsonHttpResponseHandler);
        return true;
    }

    public static String getUserToken(Context context){
        //Get userToken from shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String userToken = sharedPref.getString("userToken", null);

        //Return empty if userToken is not found
        if(userToken==null)
            return "";
        else
            return userToken;
    }

    private static JSONArray getJsonArrayFromIntArray(int[] arr) {
        JSONArray jsonLanguageCodeArray = new JSONArray();

        try {
            for (int i = 0; i < arr.length; i++) {
                jsonLanguageCodeArray.put(i,arr[i]);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return jsonLanguageCodeArray;
    }

    private static JSONArray getJsonArrayFromStringArray(String[] arr) {
        JSONArray jsonLanguageCodeArray = new JSONArray();

        try {
            for (int i = 0; i < arr.length; i++) {
                jsonLanguageCodeArray.put(i,arr[i]);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return jsonLanguageCodeArray;
    }
}
