package com.example.markwen.easycourse.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.markwen.easycourse.MainActivity;
import com.example.markwen.easycourse.R;
import com.example.markwen.easycourse.components.SignupChooseLanguageAdapter;
import com.example.markwen.easycourse.models.Language;
import com.example.markwen.easycourse.utils.APIFunctions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class SignupChooseLanguage extends Fragment {

    private static final String TAG = "SignupChooseLanguage";

    RecyclerView languageRecyclerView;
    SignupChooseLanguageAdapter languageAdapter;
    LinearLayoutManager languageLayoutManager;

    ArrayList<Language> languageList;


    public SignupChooseLanguage() {
    }

    //TODO: Save and post data


    public static SignupChooseLanguage newInstance() {
        return new SignupChooseLanguage();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        languageList = new ArrayList<>();
        languageAdapter = new SignupChooseLanguageAdapter(languageList);

        fetchLanguages();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.signup_choose_language, container, false);

        languageLayoutManager = new LinearLayoutManager(getContext());
        languageLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);


        languageRecyclerView = (RecyclerView) v.findViewById(R.id.choose_languages_recycler_view);
        languageRecyclerView.setLayoutManager(languageLayoutManager);
        languageRecyclerView.setAdapter(languageAdapter);

        return v;
    }


    public void fetchLanguages() {

        APIFunctions.getLanguages(getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, response.toString());
                    languageList.clear();
                    Iterator languages = response.keys();
                    while (languages.hasNext()) {
                        String key = (String) languages.next();
                        JSONObject obj = response.getJSONObject(key);
                        String name = obj.getString("name");
                        int code = obj.getInt("code");
                        Language language = new Language(name, code);
                        Log.d(TAG, language.getName() + language.getCode());
                        languageList.add(language);
                    }
                    languageAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Log.e(TAG, "status failure " + statusCode);
                Log.e(TAG, res);
            }
        });
    }

    // Call this function when going to mainActivity, maybe call getActivity.finish();???
    public void gotoMain() {
        Intent mainActivityIntent = new Intent(getContext(), MainActivity.class);
        startActivity(mainActivityIntent);
    }

    // Call this function when going back to SignupChooseUniversity
    public void goBackSignupChooseCourses() {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.activity_signuplogin_container, SignupChooseCourses.newInstance());
        transaction.commit();
    }

}
