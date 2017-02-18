package io.easycourse.www.easycourse.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by markw on 12/26/2016.
 */

public class JSONUtils {

    static final private String TAG = "JSONUtils";

    public static JSONArray getJsonArrayFromStringArray(String[] arr) throws JSONException {
        JSONArray jsonLanguageCodeArray = new JSONArray();

        for (int i = 0; i < arr.length; i++) {
            jsonLanguageCodeArray.put(i, arr[i]);
        }
        return jsonLanguageCodeArray;
    }

    public static JSONArray getJsonArrayFromIntArray(int[] arr) throws JSONException {
        JSONArray jsonLanguageCodeArray = new JSONArray();

        for (int i = 0; i < arr.length; i++) {
            jsonLanguageCodeArray.put(i, arr[i]);
        }

        return jsonLanguageCodeArray;
    }

    //check if JSON value exists, returns default if not
    public static Object checkIfJsonExists(JSONObject obj, String searchQuery, Object defaultObj) throws JSONException {
        if (obj.has(searchQuery)) {
            if (obj.get(searchQuery) instanceof String || obj.get(searchQuery) instanceof Integer)
                return obj.getString(searchQuery);
            else
                return obj.get(searchQuery);
        } else
            return defaultObj;
    }
}
