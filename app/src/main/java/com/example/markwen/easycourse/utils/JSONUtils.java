package com.example.markwen.easycourse.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by markw on 12/26/2016.
 */

class JSONUtils {

    static final private String TAG = "JSONUtils";

    static JSONArray getJsonArrayFromStringArray(String[] arr) {
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

    static JSONArray getJsonArrayFromIntArray(int[] arr) {
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

    //check if JSON value exists, returns default if not
    static Object checkIfJsonExists(JSONObject obj, String searchQuery, Object defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if (obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        } else
            return defaultObj;
    }
}
