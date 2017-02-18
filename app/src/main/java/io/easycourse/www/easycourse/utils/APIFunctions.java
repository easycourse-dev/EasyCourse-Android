package io.easycourse.www.easycourse.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
import io.easycourse.www.easycourse.BuildConfig;


public class APIFunctions {

    public static AsyncHttpClient client = new AsyncHttpClient();
    private static final String URL = BuildConfig.SERVER_URL + "/api";

    private static final String TAG = "APIFunctions";

    //API function for signup to server
    public static void signUp(Context context, String email, String password, String displayName, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("isMobile", "true");
        client.addHeader("Content-Type", "application/json");

        RequestParams params = new RequestParams();
        params.put("isMobile", "true");
        params.put("Content-Type", "application/json");

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);
        jsonParam.put("password", password);
        jsonParam.put("displayName", displayName);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/signup/", body, "application/json", jsonHttpResponseHandler);
    }

    //API function to login to server
    public static void login(Context context, String email, String password, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("isMobile", "true");
        client.addHeader("Content-Type", "application/json");

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);
        jsonParam.put("password", password);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/login", body, "application/json", jsonHttpResponseHandler);
    }

    //API function to logout user
    public static boolean logout(Context context, JsonHttpResponseHandler jsonHttpResponseHandler) {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);
        client.post(context, URL + "/logout", null, jsonHttpResponseHandler);

        return true;
    }

    //API function to login to facebook with accessToken
    public static void facebookLogin(Context context, String accessToken, JsonHttpResponseHandler jsonHttpResponseHandler) {
        client.addHeader("isMobile", "true");
        client.addHeader("Content-Type", "application/json");

        client.get(context, URL + "/facebook/token/?access_token=" + accessToken, jsonHttpResponseHandler);
    }

    //API function to update user's university
    public static boolean updateUser(Context context, String universityID, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("university", universityID);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/user/update", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    //API function to get university list
    public static void getUniversities(Context context, JsonHttpResponseHandler jsonHttpResponseHandler) {
        client.get(context, URL + "/univ", jsonHttpResponseHandler);
    }

    //API function to search course database
    public static void searchCourse(Context context, String searchQuery, int limit, int skip, String universityID, JsonHttpResponseHandler jsonHttpResponseHandler) {
        client.get(context, URL + "/course?q=" + searchQuery + "&limit=" + limit + "&skip=" + skip + "&univ=" + universityID, jsonHttpResponseHandler);
    }

    //API function to search course subrooms database
    public static void searchCourseSubroom(Context context, String courseId, String searchQuery, int limit, int skip, JsonHttpResponseHandler jsonHttpResponseHandler) {
        client.get(context, URL + "/coursesubroom?crs=" + courseId + "&q=" + searchQuery + "&limit=" + limit + "&skip=" + skip, jsonHttpResponseHandler);
    }

    //API function to get language list
    public static void getLanguages(Context context, JsonHttpResponseHandler jsonHttpResponseHandler) {
        client.get(context, URL + "/defaultlanguage", jsonHttpResponseHandler);
    }

    //API function to set courses and languages in  user's profile
    public static boolean setCoursesAndLanguages(Context context, String[] languageCodeArray, String[] courseCodeArray, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);

        JSONObject jsonParam = new JSONObject();
        JSONArray jsonLanguageCodeArray = JSONUtils.getJsonArrayFromStringArray(languageCodeArray);
        JSONArray jsonCourseCodeArray = JSONUtils.getJsonArrayFromStringArray(courseCodeArray);
        jsonParam.put("lang", jsonLanguageCodeArray);
        jsonParam.put("course", jsonCourseCodeArray);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/choosecourse", body, "application/json", jsonHttpResponseHandler);
        return true;
    }


    //API function to turn on or off push notifications for a room
    public static boolean setSilentRoom(Context context, String roomID, boolean silentBoolean, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("room", roomID);
        jsonParam.put("silent", silentBoolean);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/silentRoom", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    //API function to report a user
    public static boolean reportUser(Context context, String targetUser, String reason, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("targetUser", targetUser);
        jsonParam.put("reason", reason);
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/report", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    //API function to save device token
    public static boolean saveDeviceToken(Context context, String userToken, String deviceToken, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
        client.addHeader("auth", userToken);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("deviceToken", deviceToken);
        jsonParam.put("deviceType", "android");
        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/installation", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    public static boolean uploadImage(Context context, File image, String fileName, String uploadType, String roomID, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException, FileNotFoundException {
        String userToken = getUserToken(context);
        //Return false if userToken is not found
        if (userToken.isEmpty())
            return false;

        client.addHeader("auth", userToken);
        client.addHeader("type", uploadType);
        if (uploadType.equals("message"))
            client.addHeader("room", roomID);

        RequestParams params = new RequestParams();
        params.put("img", image);
        params.put("fileName", fileName);
        params.put("mimeType", "image/jpg");

        client.post(context, URL + "/uploadimage", params, jsonHttpResponseHandler);
        return true;
    }

    public static boolean forgetPassword(Context context, String email, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException, FileNotFoundException {
        client.addHeader("Content-Type", "application/json");

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("email", email);

        StringEntity body = new StringEntity(jsonParam.toString());

        client.post(context, URL + "/forgetPassword", body, "application/json", jsonHttpResponseHandler);
        return true;
    }

    static String getUserToken(Context context) {
        //Get userToken from shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences("EasyCourse", Context.MODE_PRIVATE);
        String userToken = sharedPref.getString("userToken", null);

        //Return empty if userToken is not found
        if (userToken == null)
            return "";
        else
            return userToken;
    }
}
